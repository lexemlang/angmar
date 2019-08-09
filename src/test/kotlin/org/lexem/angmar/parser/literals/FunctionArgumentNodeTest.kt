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

internal class FunctionArgumentNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val correctFunctionArgument = IdentifierNodeTest.testExpression
        const val correctFunctionArgumentWithValue =
                "${IdentifierNodeTest.testExpression}${FunctionArgumentNode.assignOperator}${ExpressionsCommonsTest.testExpression}"
        const val correctFunctionArgumentWithValueAndWS =
                "${IdentifierNodeTest.testExpression}  ${FunctionArgumentNode.assignOperator}  ${ExpressionsCommonsTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, hasExpressionNode: Boolean) {
            Assertions.assertTrue(node is FunctionArgumentNode, "The node is not a FunctionArgumentNode")
            node as FunctionArgumentNode

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
    @ValueSource(strings = [correctFunctionArgument])
    fun `parse correct function argument`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionArgumentNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionArgumentNode

        checkTestExpression(res, false)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [correctFunctionArgumentWithValue])
    fun `parse correct function argument with values`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionArgumentNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionArgumentNode

        checkTestExpression(res, true)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [correctFunctionArgumentWithValueAndWS])
    fun `parse correct function argument with values and whites`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionArgumentNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionArgumentNode

        checkTestExpression(res, true)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect function argument with no expression`() {
        assertParserException {
            val text = "${IdentifierNodeTest.testExpression}${FunctionArgumentNode.assignOperator}"
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionArgumentNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionArgumentNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
