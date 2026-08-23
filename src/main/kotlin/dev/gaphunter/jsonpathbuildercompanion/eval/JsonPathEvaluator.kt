package dev.gaphunter.jsonpathbuildercompanion.eval

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonValue
import dev.gaphunter.jsonpathbuildercompanion.model.PathSegment

/**
 * Evaluates a parsed JSONPath expression ([List] of [PathSegment])
 * against real JSON PSI ([JsonValue]) -- no hand-rolled JSON parser, no
 * separate in-memory JSON model. Same "don't reinvent a parser for a
 * format the platform already parses correctly" principle already
 * proven by `json-schema-companion`/`asyncapi-companion`/`openapi-companion`'s
 * `JsonPointer` (RFC 6901), which this evaluator's tree-navigation style
 * mirrors directly -- the difference is a JSONPath step can match zero,
 * one, or many nodes (wildcards, recursive descent), so this always
 * works over a `List<JsonValue>` of "current matches" instead of a
 * single current node.
 */
object JsonPathEvaluator {

    fun evaluate(root: JsonValue, segments: List<PathSegment>): List<JsonValue> {
        var current = listOf(root)
        for (segment in segments) {
            current = current.flatMap { applySegment(it, segment) }
        }
        return current
    }

    private fun applySegment(value: JsonValue, segment: PathSegment): List<JsonValue> = when (segment) {
        is PathSegment.Member -> {
            val property = (value as? JsonObject)?.findProperty(segment.key)
            property?.value?.let { listOf(it) } ?: emptyList()
        }
        is PathSegment.Index -> {
            val items = (value as? JsonArray)?.valueList
            items?.getOrNull(segment.index)?.let { listOf(it) } ?: emptyList()
        }
        PathSegment.Wildcard -> when (value) {
            is JsonObject -> value.propertyList.mapNotNull { it.value }
            is JsonArray -> value.valueList
            else -> emptyList()
        }
        is PathSegment.RecursiveMember -> {
            val out = mutableListOf<JsonValue>()
            collectRecursiveMembers(value, segment.key, out)
            out
        }
    }

    /** Visits every node in the subtree rooted at [value] (objects and arrays, at any depth), collecting the value of every property named [key] wherever it appears -- a real deep scan, not just the first match. */
    private fun collectRecursiveMembers(value: JsonValue, key: String, out: MutableList<JsonValue>) {
        when (value) {
            is JsonObject -> {
                value.findProperty(key)?.value?.let { out += it }
                for (property in value.propertyList) {
                    property.value?.let { collectRecursiveMembers(it, key, out) }
                }
            }
            is JsonArray -> {
                for (item in value.valueList) collectRecursiveMembers(item, key, out)
            }
            else -> Unit
        }
    }
}
