package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
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

            Assertions.assertEquals("n", node.value, "The value property is not correct")
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["a", "v", "t", "\n", "'", "\"", WhitespaceNode.windowsEndOfLine])
    fun `parse correct escape`(escapeLetter: String) {
        val text = "${EscapeNode.startToken}$escapeLetter"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = EscapeNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as EscapeNode

        Assertions.assertEquals(escapeLetter, res.value, "The value property is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect escape`() {
        assertParserException {
            val text = EscapeNode.startToken
            EscapeNode.parse(LexemParser(CustomStringReader.from(text)))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = EscapeNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
