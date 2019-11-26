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

internal class AdditiveExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${MultiplicativeExpressionNodeTest.testExpression}${AdditiveExpressionNode.additionOperator}${MultiplicativeExpressionNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectExpression(): Stream<Arguments> {
            val sequence = sequence {
                for (op in AdditiveExpressionNode.operators) {
                    for (i in 1..3) {
                        val list = List(i) { MultiplicativeExpressionNodeTest.testExpression }

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
            Assertions.assertTrue(node is AdditiveExpressionNode, "The node is not a AdditiveExpressionNode")
            node as AdditiveExpressionNode

            Assertions.assertEquals(1, node.operators.size, "The number of operators is incorrect")
            Assertions.assertEquals(2, node.expressions.size, "The number of expressions is incorrect")

            Assertions.assertEquals(AdditiveExpressionNode.additionOperator, node.operators.first(),
                    "The operator[0] is incorrect")

            for (exp in node.expressions) {
                MultiplicativeExpressionNodeTest.checkTestExpression(exp)
            }
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectExpression")
    fun `parse correct additive expression`(text: String, operator: String, numExpressions: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AdditiveExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        if (numExpressions == 1) {
            MultiplicativeExpressionNodeTest.checkTestExpression(res!!)
        } else {
            res as AdditiveExpressionNode

            Assertions.assertEquals(numExpressions - 1, res.operators.size, "The number of operators is incorrect")
            Assertions.assertEquals(numExpressions, res.expressions.size, "The number of expressions is incorrect")

            for (op in res.operators.withIndex()) {
                Assertions.assertEquals(operator, op.value, "The operator[${op.index}] is incorrect")
            }

            for (exp in res.expressions) {
                MultiplicativeExpressionNodeTest.checkTestExpression(exp)
            }
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect additive expression without operand after the operator`() {
        TestUtils.assertParserException(AngmarParserExceptionType.AdditiveExpressionWithoutExpressionAfterOperator) {
            val text = "${MultiplicativeExpressionNodeTest.testExpression}${AdditiveExpressionNode.additionOperator}"
            val parser = LexemParser(IOStringReader.from(text))
            AdditiveExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AdditiveExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

