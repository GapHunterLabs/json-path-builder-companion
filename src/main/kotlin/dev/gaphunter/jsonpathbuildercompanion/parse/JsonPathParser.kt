package dev.gaphunter.jsonpathbuildercompanion.parse

import dev.gaphunter.jsonpathbuildercompanion.model.JsonPathParseResult
import dev.gaphunter.jsonpathbuildercompanion.model.PathSegment

/**
 * Hand-rolled tokenizer/parser for a JSONPath expression, same class of
 * technique as `DockerfileParser`/`NginxLexer` -- no external JSONPath
 * library, no bundled dependency.
 *
 * **v0.1 syntax subset recognized, stated honestly:** `$` root, `.key`,
 * `['key']`/`["key"]`, `[n]` (array index), `.*`/`[*]` (wildcard, every
 * direct child), `..key` (recursive descent -- every value named `key`
 * at any depth). **Not supported in v0.1** (real, documented
 * limitations, not bugs): filter expressions (`[?(...)]`), array slices
 * (`[start:end:step]`), union syntax (`['a','b']`), script expressions,
 * and recursive wildcard (`..*`) -- the common goessner.net/RFC 9535
 * subset that covers the large majority of real-world JSONPath usage
 * (navigating and deep-scanning a document), not the full spec.
 */
object JsonPathParser {

    fun parse(path: String): JsonPathParseResult {
        val text = path.trim()
        if (text.isEmpty()) return JsonPathParseResult.Error("Empty expression")
        if (!text.startsWith("$")) return JsonPathParseResult.Error("Expression must start with \$")

        val segments = mutableListOf<PathSegment>()
        var i = 1

        while (i < text.length) {
            when {
                text.startsWith("..", i) -> {
                    val afterDots = i + 2
                    if (afterDots < text.length && text[afterDots] == '*') {
                        return JsonPathParseResult.Error("Recursive wildcard '..*' is not supported in v0.1")
                    }
                    val identifier = readIdentifier(text, afterDots)
                        ?: return JsonPathParseResult.Error("Expected a key after '..' at position $afterDots")
                    segments += PathSegment.RecursiveMember(identifier.first)
                    i = identifier.second
                }
                text[i] == '.' -> {
                    val afterDot = i + 1
                    if (afterDot < text.length && text[afterDot] == '*') {
                        segments += PathSegment.Wildcard
                        i = afterDot + 1
                    } else {
                        val identifier = readIdentifier(text, afterDot)
                            ?: return JsonPathParseResult.Error("Expected a key after '.' at position $afterDot")
                        segments += PathSegment.Member(identifier.first)
                        i = identifier.second
                    }
                }
                text[i] == '[' -> {
                    val closeIdx = text.indexOf(']', i)
                    if (closeIdx == -1) return JsonPathParseResult.Error("Unclosed '[' at position $i")
                    val inner = text.substring(i + 1, closeIdx).trim()
                    when {
                        inner == "*" -> segments += PathSegment.Wildcard
                        isQuoted(inner, '\'') || isQuoted(inner, '"') ->
                            segments += PathSegment.Member(inner.substring(1, inner.length - 1))
                        inner.toIntOrNull() != null -> segments += PathSegment.Index(inner.toInt())
                        else -> return JsonPathParseResult.Error(
                            "Unsupported bracket expression '[$inner]' -- v0.1 supports ['key'], [n], and [*] only",
                        )
                    }
                    i = closeIdx + 1
                }
                else -> return JsonPathParseResult.Error("Unexpected character '${text[i]}' at position $i")
            }
        }

        return JsonPathParseResult.Success(segments)
    }

    private fun isQuoted(text: String, quote: Char): Boolean =
        text.length >= 2 && text.first() == quote && text.last() == quote

    /** Reads a bareword identifier ([A-Za-z0-9_]+) starting at [start]. Returns null if there's nothing to read there. */
    private fun readIdentifier(text: String, start: Int): Pair<String, Int>? {
        var end = start
        while (end < text.length && (text[end].isLetterOrDigit() || text[end] == '_')) end++
        if (end == start) return null
        return text.substring(start, end) to end
    }
}
