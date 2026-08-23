package dev.gaphunter.jsonpathbuildercompanion.model

/**
 * One segment of a parsed JSONPath expression, v0.1 subset (see
 * `JsonPathParser` for exactly what's recognized). A full expression is
 * a `List<PathSegment>` applied left to right against a JSON document's
 * root value.
 */
sealed class PathSegment {
    /** `.key` or `['key']`/`["key"]` -- a single named child of an object. */
    data class Member(val key: String) : PathSegment()

    /** `[n]` -- a single indexed element of an array. */
    data class Index(val index: Int) : PathSegment()

    /** `.*` or `[*]` -- every direct child (object values or array elements). */
    object Wildcard : PathSegment()

    /** `..key` -- every value named [key] found at any depth below this point (deep scan). */
    data class RecursiveMember(val key: String) : PathSegment()
}
