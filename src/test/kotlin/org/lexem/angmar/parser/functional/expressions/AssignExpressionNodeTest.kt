package org.lexem.angmar.parser.functional.expressions

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class AssignExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ExpressionsCommonsTest.testLeftExpression}${AssignOperatorNodeTest.testExpression}${ExpressionsCommonsTest.testRightExpression}"

        @JvmStatic
        private fun provideAssignCorrectExpression(): Stream<Arguments> {
            val sequence = sequence {
                var text =
                        "${ExpressionsCommonsTest.testLeftExpression} ${AssignOperatorNode.assignOperator} ${ExpressionsCommonsTest.testRightExpression}"
                yield(Arguments.of(text, AssignOperatorNode.assignOperator))

                text =
                        "${ExpressionsCommonsTest.testLeftExpression}${AssignOperatorNode.assignOperator}${ExpressionsCommonsTest.testRightExpression}"
                yield(Arguments.of(text, AssignOperatorNode.assignOperator))

                for (op in AssignOperatorNode.operators) {
                    text =
                            "${ExpressionsCommonsTest.testLeftExpression} $op ${ExpressionsCommonsTest.testRightExpression}"
                    yield(Arguments.of(text, op))

                    if (op.first() in ExpressionsCommons.operatorCharacters) {
                        text =
                                "${ExpressionsCommonsTest.testLeftExpression}$op${ExpressionsCommonsTest.testRightExpression}"
                        yield(Arguments.of(text, op))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is AssignExpressionNode, "The node is not a AssignExpressionNode")
            node as AssignExpressionNode

            AssignOperatorNodeTest.checkTestExpression(node.operator)
            ExpressionsCommonsTest.checkTestLeftExpression(node.left)
            ExpressionsCommonsTest.checkTestRightExpression(node.right)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideAssignCorrectExpression")
    fun `parse correct assign expression`(text: String, operator: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = AssignExpressionNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AssignExpressionNode

        Assertions.assertEquals(operator.substring(0, operator.length - AssignOperatorNode.assignOperator.length),
                res.operator.operator, "The operator property is incorrect")
        ExpressionsCommonsTest.checkTestLeftExpression(res.left)
        ExpressionsCommonsTest.checkTestRightExpression(res.right)
    }

    @Test
    @Incorrect
    fun `parse incorrect assign expression without right operand`() {
        assertParserException {
            val text = "${ExpressionsCommonsTest.testLeftExpression} ${AssignOperatorNode.assignOperator} "
            val parser = LexemParser(CustomStringReader.from(text))
            AssignExpressionNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = AssignExpressionNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

