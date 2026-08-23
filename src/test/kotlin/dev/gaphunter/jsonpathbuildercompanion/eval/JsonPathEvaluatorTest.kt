package dev.gaphunter.jsonpathbuildercompanion.eval

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.gaphunter.jsonpathbuildercompanion.model.PathSegment

class JsonPathEvaluatorTest : BasePlatformTestCase() {

    private val sample = """
        {
          "store": {
            "book": [
              { "title": "Sayings of the Century", "author": "Nigel Rees" },
              { "title": "Sword of Honour", "author": "Evelyn Waugh" }
            ],
            "bicycle": { "author": "Not a book but has an author field too" }
          }
        }
    """.trimIndent()

    private fun rootOf(text: String) = (myFixture.configureByText("sample.json", text) as JsonFile).topLevelValue!!

    fun `test dot member access resolves a nested object`() {
        val root = rootOf(sample)
        val matches = JsonPathEvaluator.evaluate(root, listOf(PathSegment.Member("store"), PathSegment.Member("bicycle")))
        assertEquals(1, matches.size)
    }

    fun `test array index resolves one element`() {
        val root = rootOf(sample)
        val matches = JsonPathEvaluator.evaluate(
            root,
            listOf(PathSegment.Member("store"), PathSegment.Member("book"), PathSegment.Index(1)),
        )
        assertEquals(1, matches.size)
        val title = (matches[0] as com.intellij.json.psi.JsonObject).findProperty("title")?.value as? JsonStringLiteral
        assertEquals("Sword of Honour", title?.value)
    }

    fun `test wildcard over an array matches every element`() {
        val root = rootOf(sample)
        val matches = JsonPathEvaluator.evaluate(
            root,
            listOf(PathSegment.Member("store"), PathSegment.Member("book"), PathSegment.Wildcard),
        )
        assertEquals(2, matches.size)
    }

    fun `test wildcard over an object matches every property value`() {
        val root = rootOf("""{ "a": 1, "b": 2, "c": 3 }""")
        val matches = JsonPathEvaluator.evaluate(root, listOf(PathSegment.Wildcard))
        assertEquals(3, matches.size)
    }

    fun `test recursive descent finds a key at every depth, not just the first hit`() {
        val root = rootOf(sample)
        val matches = JsonPathEvaluator.evaluate(root, listOf(PathSegment.RecursiveMember("author")))
        // 2 books each with "author", plus "bicycle"'s own "author" field -- 3 total.
        assertEquals(3, matches.size)
    }

    fun `test a member access on a missing key produces no matches, not a crash`() {
        val root = rootOf(sample)
        val matches = JsonPathEvaluator.evaluate(root, listOf(PathSegment.Member("doesNotExist")))
        assertTrue(matches.isEmpty())
    }

    fun `test an index out of bounds produces no matches, not a crash`() {
        val root = rootOf(sample)
        val matches = JsonPathEvaluator.evaluate(
            root,
            listOf(PathSegment.Member("store"), PathSegment.Member("book"), PathSegment.Index(99)),
        )
        assertTrue(matches.isEmpty())
    }

    fun `test an index applied to an object (not an array) produces no matches, not a crash`() {
        val root = rootOf(sample)
        val matches = JsonPathEvaluator.evaluate(root, listOf(PathSegment.Member("store"), PathSegment.Index(0)))
        assertTrue(matches.isEmpty())
    }
}
