package dev.gaphunter.jsonpathbuildercompanion.model

sealed class JsonPathParseResult {
    data class Success(val segments: List<PathSegment>) : JsonPathParseResult()
    data class Error(val message: String) : JsonPathParseResult()
}
