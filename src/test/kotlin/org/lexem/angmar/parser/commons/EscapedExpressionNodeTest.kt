package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*

internal class EscapedExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${EscapedExpressionNode.startToken}${ExpressionsCommonsTest.testExpression}${EscapedExpressionNode.endToken}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is EscapedExpressionNode, "The node is not a EscapedExpressionNode")
            node as EscapedExpressionNode

            ExpressionsCommonsTest.checkTestExpression(node.expression)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse correct escaped expression`(expression: String) {
        val text = "${EscapedExpressionNode.startToken}$expression${EscapedExpressionNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = EscapedExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as EscapedExpressionNode

        ExpressionsCommonsTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect escaped expression with no expression`() {
        TestUtils.assertParserException {
            val text = EscapedExpressionNode.startToken
            val parser = LexemParser(CustomStringReader.from(text))
            EscapedExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse incorrect escaped expression with no endToken`(expression: String) {
        TestUtils.assertParserException {
            val text = "${EscapedExpressionNode.startToken}$expression"
            val parser = LexemParser(CustomStringReader.from(text))
            EscapedExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = EscapedExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
