package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*

internal class PropertyStyleObjectNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${PropertyStyleObjectNode.startToken}${PropertyStyleObjectBlockNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is PropertyStyleObjectNode, "The node is not a PropertyStyleObjectNode")
            node as PropertyStyleObjectNode

            Assertions.assertFalse(node.isConstant, "The isConstant property is incorrect")
            PropertyStyleObjectBlockNodeTest.checkTestExpression(node.block)
        }
    }


    // TESTS ------------------------------------------------------------------

    @Test
    fun `parse correct prop-style object`() {
        val text = "${PropertyStyleObjectNode.startToken}${PropertyStyleObjectBlockNodeTest.testExpression}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PropertyStyleObjectNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyStyleObjectNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        PropertyStyleObjectBlockNodeTest.checkTestExpression(res.block)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct constant prop-style object`() {
        val text =
                "${PropertyStyleObjectNode.startToken}${PropertyStyleObjectNode.constantToken}${PropertyStyleObjectBlockNodeTest.testExpression}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PropertyStyleObjectNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyStyleObjectNode

        Assertions.assertTrue(res.isConstant, "The isConstant property is incorrect")
        PropertyStyleObjectBlockNodeTest.checkTestExpression(res.block)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect prop-style object with no block`() {
        assertParserException {
            val text = PropertyStyleObjectNode.startToken
            val parser = LexemParser(CustomStringReader.from(text))
            PropertyStyleObjectNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PropertyStyleObjectNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
