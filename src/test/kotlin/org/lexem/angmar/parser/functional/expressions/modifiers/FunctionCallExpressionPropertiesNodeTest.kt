package org.lexem.angmar.parser.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class FunctionCallExpressionPropertiesNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${FunctionCallExpressionPropertiesNode.relationalToken}${PropertyStyleObjectBlockNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is FunctionCallExpressionPropertiesNode,
                    "The node is not a FunctionCallExpressionPropertiesNode")
            node as FunctionCallExpressionPropertiesNode

            PropertyStyleObjectBlockNodeTest.checkTestExpression(node.value)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(
            strings = ["${FunctionCallExpressionPropertiesNode.relationalToken}${PropertyStyleObjectBlockNodeTest.testExpression}"])
    fun `parse correct function call expression properties`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionCallExpressionPropertiesNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionCallExpressionPropertiesNode

        PropertyStyleObjectBlockNodeTest.checkTestExpression(res.value)
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [FunctionCallExpressionPropertiesNode.relationalToken])
    fun `parse incorrect function call expression properties without prop-style object`(text: String) {
        assertParserException {
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionCallExpressionPropertiesNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionCallExpressionPropertiesNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

