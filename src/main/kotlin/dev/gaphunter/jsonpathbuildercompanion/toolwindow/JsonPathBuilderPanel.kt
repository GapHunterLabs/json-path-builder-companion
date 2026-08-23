package dev.gaphunter.jsonpathbuildercompanion.toolwindow

import com.intellij.json.JsonLanguage
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import dev.gaphunter.jsonpathbuildercompanion.eval.JsonPathEvaluator
import dev.gaphunter.jsonpathbuildercompanion.model.JsonPathParseResult
import dev.gaphunter.jsonpathbuildercompanion.parse.JsonPathParser
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultHighlighter

/**
 * The whole plugin's UI, self-contained -- same "live recompute on every
 * keystroke, no debouncing needed" design as `regex-preview-companion`'s
 * `RegexPreviewPanel` (the sample JSON is typically short, and parsing
 * it into a throwaway in-memory [JsonFile] is the same cost class as the
 * regex engine's `Pattern.compile`/`Matcher.find`, not a genuinely
 * expensive operation). PSI creation and reads happen inside
 * [ApplicationManager.runReadAction] (same pattern already proven
 * elsewhere in this catalog, e.g. `json-to-code-companion`'s
 * `GenerateClassFromJsonAction`) even though this runs on the EDT --
 * PSI access always needs the read lock, thread notwithstanding.
 */
class JsonPathBuilderPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val pathField = JBTextField()
    private val sampleJsonArea = JBTextArea(14, 60).apply { lineWrap = true; wrapStyleWord = true }
    private val statusLabel = JBLabel(" ")
    private val highlightPainter = DefaultHighlighter.DefaultHighlightPainter(JBColor(Color(255, 235, 59, 120), Color(255, 235, 59, 90)))

    init {
        val topPanel = JPanel(BorderLayout())
        topPanel.add(JBLabel("JSONPath: "), BorderLayout.WEST)
        topPanel.add(pathField, BorderLayout.CENTER)
        topPanel.border = BorderFactory.createEmptyBorder(8, 8, 4, 8)

        sampleJsonArea.border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
        statusLabel.border = BorderFactory.createEmptyBorder(4, 8, 8, 8)

        add(topPanel, BorderLayout.NORTH)
        add(JBScrollPane(sampleJsonArea), BorderLayout.CENTER)
        add(statusLabel, BorderLayout.SOUTH)

        val listener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = update()
            override fun removeUpdate(e: DocumentEvent) = update()
            override fun changedUpdate(e: DocumentEvent) = update()
        }
        pathField.document.addDocumentListener(listener)
        sampleJsonArea.document.addDocumentListener(listener)

        update()
    }

    /** Exposed for tests -- runs the exact same path the UI listeners trigger, without needing a real keystroke event. */
    fun update() {
        sampleJsonArea.highlighter.removeAllHighlights()

        val pathText = pathField.text
        if (pathText.isBlank()) {
            statusLabel.text = " "
            return
        }

        when (val parseResult = JsonPathParser.parse(pathText)) {
            is JsonPathParseResult.Error -> {
                statusLabel.text = "Invalid JSONPath: ${parseResult.message}"
                statusLabel.foreground = JBColor.RED
            }
            is JsonPathParseResult.Success -> {
                val root = parseSampleRoot()
                if (root == null) {
                    statusLabel.text = "Sample is not valid JSON"
                    statusLabel.foreground = JBColor.RED
                    return
                }
                val matches = ApplicationManager.getApplication().runReadAction<List<JsonValue>> {
                    JsonPathEvaluator.evaluate(root, parseResult.segments)
                }
                highlightMatches(matches)
                statusLabel.text = "${matches.size} match(es)"
                statusLabel.foreground = JBColor.foreground()
            }
        }
    }

    private fun highlightMatches(matches: List<JsonValue>) {
        for (match in matches) {
            val range = match.textRange
            if (range.startOffset >= range.endOffset) continue
            try {
                sampleJsonArea.highlighter.addHighlight(range.startOffset, range.endOffset, highlightPainter)
            } catch (_: BadLocationException) {
                // Offsets came from PSI parsed moments ago against this exact text -- a
                // concurrent edit could theoretically race this; skipping this one
                // highlight is harmless, the next keystroke recomputes everything.
            }
        }
    }

    /**
     * The JSON PSI parser is error-tolerant -- malformed input still
     * produces a [JsonFile] with a non-null [JsonFile.topLevelValue]
     * (a best-effort partial tree), so a null check alone doesn't
     * detect "this isn't valid JSON" (found live: a first version of
     * this method silently returned "0 match(es)" for garbage input
     * instead of an honest error). The real signal is whether the tree
     * contains any [PsiErrorElement] at all, same check already proven
     * in this catalog's `InMemoryValidator` (bean-copy/test-scaffold/
     * turbo-log/json-to-code-companion).
     */
    private fun parseSampleRoot(): JsonValue? = ApplicationManager.getApplication().runReadAction<JsonValue?> {
        val file = PsiFileFactory.getInstance(project)
            .createFileFromText("__json_path_builder_sample.json", JsonLanguage.INSTANCE, sampleJsonArea.text, false, true) as? JsonFile
            ?: return@runReadAction null
        if (PsiTreeUtil.findChildOfType(file, PsiErrorElement::class.java) != null) return@runReadAction null
        file.topLevelValue
    }

    // Exposed for tests only.
    fun setPathTextForTest(text: String) { pathField.text = text }
    fun setSampleTextForTest(text: String) { sampleJsonArea.text = text }
    fun statusTextForTest(): String = statusLabel.text
    fun highlightCountForTest(): Int = sampleJsonArea.highlighter.highlights.size
}
