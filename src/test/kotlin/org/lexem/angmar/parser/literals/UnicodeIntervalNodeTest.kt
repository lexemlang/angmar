package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*

internal class UnicodeIntervalNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${UnicodeIntervalNode.macroName}${UnicodeIntervalAbbrNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is UnicodeIntervalNode, "The node is not an UnicodeIntervalNode")
            node as UnicodeIntervalNode

            UnicodeIntervalAbbrNodeTest.checkTestExpression(node.node)
        }
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `parse correct unicode interval`() {
        val text = "${UnicodeIntervalNode.macroName}${UnicodeIntervalAbbrNodeTest.testExpression}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnicodeIntervalNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeIntervalNode

        UnicodeIntervalAbbrNodeTest.checkTestExpression(res.node)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect unicode interval without abbreviation`() {
        TestUtils.assertParserException {
            val text = UnicodeIntervalNode.macroName
            val parser = LexemParser(CustomStringReader.from(text))
            UnicodeIntervalNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "\n"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnicodeIntervalNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

