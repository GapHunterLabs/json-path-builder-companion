package dev.gaphunter.jsonpathbuildercompanion.toolwindow

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Deliberately light -- the real parsing/evaluation logic is
 * exhaustively covered by `JsonPathParserTest`/`JsonPathEvaluatorTest`
 * without any Swing involved. This just confirms the panel wires that
 * logic to the UI correctly, same split as `RegexPreviewPanelTest`.
 */
class JsonPathBuilderPanelTest : BasePlatformTestCase() {

    fun `test a valid expression against valid sample JSON highlights each match`() {
        val panel = JsonPathBuilderPanel(project)
        panel.setSampleTextForTest("""{"a": [1, 2, 3]}""")
        panel.setPathTextForTest("$.a[*]")
        panel.update()

        assertEquals(3, panel.highlightCountForTest())
        assertEquals("3 match(es)", panel.statusTextForTest())
    }

    fun `test an invalid expression shows the real error instead of crashing`() {
        val panel = JsonPathBuilderPanel(project)
        panel.setSampleTextForTest("""{"a": 1}""")
        panel.setPathTextForTest("a.b")
        panel.update()

        assertEquals(0, panel.highlightCountForTest())
        assertTrue(panel.statusTextForTest().startsWith("Invalid JSONPath:"))
    }

    fun `test invalid sample JSON shows an honest error instead of a stack trace`() {
        val panel = JsonPathBuilderPanel(project)
        panel.setSampleTextForTest("not valid json at all {{{")
        panel.setPathTextForTest("$.a")
        panel.update()

        assertEquals(0, panel.highlightCountForTest())
        assertEquals("Sample is not valid JSON", panel.statusTextForTest())
    }

    fun `test an empty expression highlights nothing without showing an error`() {
        val panel = JsonPathBuilderPanel(project)
        panel.setSampleTextForTest("""{"a": 1}""")
        panel.setPathTextForTest("")
        panel.update()

        assertEquals(0, panel.highlightCountForTest())
        assertFalse(panel.statusTextForTest().startsWith("Invalid"))
    }

    fun `test changing the sample text recomputes matches`() {
        val panel = JsonPathBuilderPanel(project)
        panel.setPathTextForTest("$.items[*]")
        panel.setSampleTextForTest("""{"items": []}""")
        panel.update()
        assertEquals(0, panel.highlightCountForTest())

        panel.setSampleTextForTest("""{"items": [1, 2]}""")
        panel.update()
        assertEquals(2, panel.highlightCountForTest())
    }
}
