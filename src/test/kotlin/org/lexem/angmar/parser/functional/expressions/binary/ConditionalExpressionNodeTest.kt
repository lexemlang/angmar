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

internal class ConditionalExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${RelationalExpressionNodeTest.testExpression} ${ConditionalExpressionNode.andOperator} ${RelationalExpressionNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectExpression(): Stream<Arguments> {
            val sequence = sequence {
                for (op in ConditionalExpressionNode.operators) {
                    for (i in 1..3) {
                        val list = List(i) { RelationalExpressionNodeTest.testExpression }

                        val text = list.joinToString(" $op ")
                        yield(Arguments.of(text, op, i))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ConditionalExpressionNode, "The node is not a ConditionalExpressionNode")
            node as ConditionalExpressionNode

            Assertions.assertEquals(1, node.operators.size, "The number of operators is incorrect")
            Assertions.assertEquals(2, node.expressions.size, "The number of expressions is incorrect")

            Assertions.assertEquals(ConditionalExpressionNode.andOperator, node.operators.first(),
                    "The operator[0] is incorrect")

            for (exp in node.expressions) {
                RelationalExpressionNodeTest.checkTestExpression(exp)
            }
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectExpression")
    fun `parse correct conditional expression`(text: String, operator: String, numExpressions: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ConditionalExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        if (numExpressions == 1) {
            RelationalExpressionNodeTest.checkTestExpression(res!!)
        } else {
            res as ConditionalExpressionNode

            Assertions.assertEquals(numExpressions - 1, res.operators.size, "The number of operators is incorrect")
            Assertions.assertEquals(numExpressions, res.expressions.size, "The number of expressions is incorrect")

            for (op in res.operators.withIndex()) {
                Assertions.assertEquals(operator, op.value, "The operator[${op.index}] is incorrect")
            }

            for (exp in res.expressions) {
                RelationalExpressionNodeTest.checkTestExpression(exp)
            }
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect conditional expression without operand after the operator`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ConditionalExpressionWithoutExpressionAfterOperator) {
            val text = "${RelationalExpressionNodeTest.testExpression} ${ConditionalExpressionNode.andOperator}"
            val parser = LexemParser(IOStringReader.from(text))
            val res = ConditionalExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode)

            res.hashCode()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ConditionalExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
