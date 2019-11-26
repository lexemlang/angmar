package org.lexem.angmar.parser.functional.expressions.binary

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class MultiplicativeExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${PrefixExpressionNodeTest.testExpression}${MultiplicativeExpressionNode.multiplicationOperator}${PrefixExpressionNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectExpression(): Stream<Arguments> {
            val sequence = sequence {
                for (op in MultiplicativeExpressionNode.operators) {
                    for (i in 1..3) {
                        val list = List(i) { PrefixExpressionNodeTest.testExpression }

                        var text = list.joinToString(" $op ")
                        yield(Arguments.of(text, op, i))

                        text = list.joinToString(op)
                        yield(Arguments.of(text, op, i))
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideCorrectOperators(): Stream<Arguments> {
            val list = listOf(MultiplicativeExpressionNode.operators, AdditiveExpressionNode.operators,
                    ShiftExpressionNode.operators, LogicalExpressionNode.operators, RelationalExpressionNode.operators)

            val result = sequence {
                for (operators in list) {
                    for (op in operators) {
                        yield(Arguments.of(op, operators))
                    }
                }
            }

            return result.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is MultiplicativeExpressionNode,
                    "The node is not a MultiplicativeExpressionNode")
            node as MultiplicativeExpressionNode

            Assertions.assertEquals(1, node.operators.size, "The number of operators is incorrect")
            Assertions.assertEquals(2, node.expressions.size, "The number of expressions is incorrect")

            Assertions.assertEquals(MultiplicativeExpressionNode.multiplicationOperator, node.operators.first(),
                    "The operator[0] is incorrect")

            for (exp in node.expressions) {
                PrefixExpressionNodeTest.checkTestExpression(exp)
            }
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectExpression")
    fun `parse correct multiplicative expression`(text: String, operator: String, numExpressions: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = MultiplicativeExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        if (numExpressions == 1) {
            PrefixExpressionNodeTest.checkTestExpression(res!!)
        } else {
            res as MultiplicativeExpressionNode

            Assertions.assertEquals(numExpressions - 1, res.operators.size, "The number of operators is incorrect")
            Assertions.assertEquals(numExpressions, res.expressions.size, "The number of expressions is incorrect")

            for (op in res.operators.withIndex()) {
                Assertions.assertEquals(operator, op.value, "The operator[${op.index}] is incorrect")
            }

            for (exp in res.expressions) {
                PrefixExpressionNodeTest.checkTestExpression(exp)
            }
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect multiplicative expression without operand after the operator`() {
        TestUtils.assertParserException(
                AngmarParserExceptionType.MultiplicativeExpressionWithoutExpressionAfterOperator) {
            val text =
                    "${PrefixExpressionNodeTest.testExpression}${MultiplicativeExpressionNode.multiplicationOperator}"
            val parser = LexemParser(IOStringReader.from(text))
            MultiplicativeExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = MultiplicativeExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

