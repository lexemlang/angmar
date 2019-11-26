package org.lexem.angmar.parser.functional.expressions

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*

internal class ParenthesisExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ParenthesisExpressionNode.startToken}${ExpressionsCommonsTest.testExpression}${ParenthesisExpressionNode.endToken}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ParenthesisExpressionNode, "The node is not a ParenthesisExpressionNode")
            node as ParenthesisExpressionNode

            ExpressionsCommonsTest.checkTestExpression(node.expression)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse correct parenthesis expression`(expression: String) {
        val text = "${ParenthesisExpressionNode.startToken}$expression${ParenthesisExpressionNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = ParenthesisExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParenthesisExpressionNode

        ExpressionsCommonsTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse correct parenthesis expression with whites`(expression: String) {
        val text = "${ParenthesisExpressionNode.startToken}  $expression  ${ParenthesisExpressionNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = ParenthesisExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParenthesisExpressionNode

        ExpressionsCommonsTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect parenthesis expression with no expression`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ParenthesisExpressionWithoutExpression) {
            val text = ParenthesisExpressionNode.startToken
            val parser = LexemParser(IOStringReader.from(text))
            ParenthesisExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse incorrect parenthesis expression with no endToken`(expression: String) {
        TestUtils.assertParserException(AngmarParserExceptionType.ParenthesisExpressionWithoutEndToken) {
            val text = "${ParenthesisExpressionNode.startToken}$expression"
            val parser = LexemParser(IOStringReader.from(text))
            ParenthesisExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ParenthesisExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
