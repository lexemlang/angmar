package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*

internal class EscapeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${EscapeNode.startToken}n"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is EscapeNode, "The node is not an EscapeNode")
            node as EscapeNode

            Assertions.assertEquals("n", node.value, "The value property is incorrect")
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["a", "v", "t", "\n", "'", "\"", WhitespaceNode.windowsEndOfLine])
    fun `parse correct escape`(escapeLetter: String) {
        val text = "${EscapeNode.startToken}$escapeLetter"
        val parser = LexemParser(IOStringReader.from(text))
        val res = EscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as EscapeNode

        Assertions.assertEquals(escapeLetter, res.value, "The value property is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect escape`() {
        TestUtils.assertParserException(AngmarParserExceptionType.EscapeWithoutCharacter) {
            val text = EscapeNode.startToken
            val parser = LexemParser(IOStringReader.from(text))
            EscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = EscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
