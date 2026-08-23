package dev.gaphunter.jsonpathbuildercompanion.parse

import dev.gaphunter.jsonpathbuildercompanion.model.JsonPathParseResult
import dev.gaphunter.jsonpathbuildercompanion.model.PathSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonPathParserTest {

    private fun parseOk(path: String): List<PathSegment> {
        val result = JsonPathParser.parse(path)
        assertTrue("expected Success for '$path' but got $result", result is JsonPathParseResult.Success)
        return (result as JsonPathParseResult.Success).segments
    }

    @Test
    fun `root alone parses to an empty segment list`() {
        assertEquals(emptyList<PathSegment>(), parseOk("$"))
    }

    @Test
    fun `dot member access`() {
        assertEquals(listOf(PathSegment.Member("store")), parseOk("$.store"))
    }

    @Test
    fun `chained dot member access`() {
        assertEquals(
            listOf(PathSegment.Member("store"), PathSegment.Member("book")),
            parseOk("$.store.book"),
        )
    }

    @Test
    fun `bracket member access with single or double quotes`() {
        assertEquals(listOf(PathSegment.Member("store")), parseOk("$['store']"))
        assertEquals(listOf(PathSegment.Member("store")), parseOk("$[\"store\"]"))
    }

    @Test
    fun `array index access`() {
        assertEquals(
            listOf(PathSegment.Member("book"), PathSegment.Index(0)),
            parseOk("$.book[0]"),
        )
    }

    @Test
    fun `dot wildcard and bracket wildcard are equivalent`() {
        assertEquals(listOf(PathSegment.Wildcard), parseOk("$.*"))
        assertEquals(listOf(PathSegment.Wildcard), parseOk("$[*]"))
    }

    @Test
    fun `recursive descent member`() {
        assertEquals(listOf(PathSegment.RecursiveMember("author")), parseOk("$..author"))
    }

    @Test
    fun `a realistic mixed expression`() {
        assertEquals(
            listOf(PathSegment.Member("store"), PathSegment.Member("book"), PathSegment.Wildcard, PathSegment.Member("author")),
            parseOk("$.store.book[*].author"),
        )
    }

    @Test
    fun `missing leading dollar sign is an error`() {
        val result = JsonPathParser.parse("store.book")
        assertTrue(result is JsonPathParseResult.Error)
    }

    @Test
    fun `empty expression is an error`() {
        assertTrue(JsonPathParser.parse("") is JsonPathParseResult.Error)
        assertTrue(JsonPathParser.parse("   ") is JsonPathParseResult.Error)
    }

    @Test
    fun `unclosed bracket is an error, not a crash`() {
        assertTrue(JsonPathParser.parse("$.book[0") is JsonPathParseResult.Error)
    }

    @Test
    fun `a filter expression is a documented v0-1 error, not silently ignored`() {
        val result = JsonPathParser.parse("$.book[?(@.price < 10)]")
        assertTrue(result is JsonPathParseResult.Error)
    }

    @Test
    fun `an array slice is a documented v0-1 error, not silently ignored`() {
        val result = JsonPathParser.parse("$.book[0:2]")
        assertTrue(result is JsonPathParseResult.Error)
    }

    @Test
    fun `recursive wildcard is a documented v0-1 error, not silently ignored`() {
        val result = JsonPathParser.parse("$..*")
        assertTrue(result is JsonPathParseResult.Error)
    }
}
