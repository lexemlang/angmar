package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*

internal class FunctionParameterNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val correctFunctionParameter = IdentifierNodeTest.testExpression
        const val correctFunctionParameterWithValue =
                "${IdentifierNodeTest.testExpression}${FunctionParameterNode.assignOperator}${ExpressionsCommonsTest.testExpression}"
        const val correctFunctionParameterWithValueAndWS =
                "${IdentifierNodeTest.testExpression}  ${FunctionParameterNode.assignOperator}  ${ExpressionsCommonsTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, hasExpressionNode: Boolean) {
            Assertions.assertTrue(node is FunctionParameterNode, "The node is not a FunctionParameterNode")
            node as FunctionParameterNode

            IdentifierNodeTest.checkTestExpression(node.identifier)

            if (hasExpressionNode) {
                ExpressionsCommonsTest.checkTestExpression(node.expression!!)
            } else {
                Assertions.assertNull(node.expression, "The expression property must be null")
            }
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [correctFunctionParameter])
    fun `parse correct function parameter`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionParameterNode

        checkTestExpression(res, false)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [correctFunctionParameterWithValue])
    fun `parse correct function parameter with values`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionParameterNode

        checkTestExpression(res, true)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [correctFunctionParameterWithValueAndWS])
    fun `parse correct function parameter with values and whites`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionParameterNode

        checkTestExpression(res, true)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect function parameter with no expression`() {
        TestUtils.assertParserException {
            val text = "${IdentifierNodeTest.testExpression}${FunctionParameterNode.assignOperator}"
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionParameterNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
