package org.lexem.angmar.parser.functional.expressions.binary

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LogicalExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ShiftExpressionNodeTest.testExpression}${LogicalExpressionNode.andOperator}${ShiftExpressionNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectExpression(): Stream<Arguments> {
            val sequence = sequence {
                for (op in LogicalExpressionNode.operators) {
                    for (i in 1..3) {
                        val list = List(i) { ShiftExpressionNodeTest.testExpression }

                        var text = list.joinToString(" $op ")
                        yield(Arguments.of(text, op, i))

                        text = list.joinToString(op)
                        yield(Arguments.of(text, op, i))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is LogicalExpressionNode, "The node is not a LogicalExpressionNode")
            node as LogicalExpressionNode

            Assertions.assertEquals(1, node.operators.size, "The number of operators is incorrect")
            Assertions.assertEquals(2, node.expressions.size, "The number of expressions is incorrect")

            Assertions.assertEquals(LogicalExpressionNode.andOperator, node.operators.first(),
                    "The operator[0] is incorrect")

            for (exp in node.expressions) {
                ShiftExpressionNodeTest.checkTestExpression(exp)
            }
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectExpression")
    fun `parse correct logical expression`(text: String, operator: String, numExpressions: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = LogicalExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        if (numExpressions == 1) {
            ShiftExpressionNodeTest.checkTestExpression(res!!)
        } else {
            res as LogicalExpressionNode

            Assertions.assertEquals(numExpressions - 1, res.operators.size, "The number of operators is incorrect")
            Assertions.assertEquals(numExpressions, res.expressions.size, "The number of expressions is incorrect")

            for (op in res.operators.withIndex()) {
                Assertions.assertEquals(operator, op.value, "The operator[${op.index}] is incorrect")
            }

            for (exp in res.expressions) {
                ShiftExpressionNodeTest.checkTestExpression(exp)
            }
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect logical expression without operand after the operator`() {
        TestUtils.assertParserException(AngmarParserExceptionType.LogicalExpressionWithoutExpressionAfterOperator) {
            val text = "${ShiftExpressionNodeTest.testExpression}${LogicalExpressionNode.andOperator}"
            val parser = LexemParser(IOStringReader.from(text))
            LogicalExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = LogicalExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
