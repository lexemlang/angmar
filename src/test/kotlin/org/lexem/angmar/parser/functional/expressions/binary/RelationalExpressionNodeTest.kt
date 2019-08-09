package org.lexem.angmar.parser.functional.expressions.binary

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class RelationalExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${LogicalExpressionNodeTest.testExpression}${RelationalExpressionNode.identityOperator}${LogicalExpressionNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectExpression(): Stream<Arguments> {
            val sequence = sequence {
                for (op in RelationalExpressionNode.operators) {
                    for (i in 1..3) {
                        val list = List(i) { LogicalExpressionNodeTest.testExpression }

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
            Assertions.assertTrue(node is RelationalExpressionNode, "The node is not a RelationalExpressionNode")
            node as RelationalExpressionNode

            Assertions.assertEquals(1, node.operators.size, "The number of operators is incorrect")
            Assertions.assertEquals(2, node.expressions.size, "The number of expressions is incorrect")

            Assertions.assertEquals(RelationalExpressionNode.identityOperator, node.operators.first(),
                    "The operator[0] is incorrect")

            for (exp in node.expressions) {
                LogicalExpressionNodeTest.checkTestExpression(exp)
            }
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectExpression")
    fun `parse correct relational expression`(text: String, operator: String, numExpressions: Int) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = RelationalExpressionNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        if (numExpressions == 1) {
            LogicalExpressionNodeTest.checkTestExpression(res!!)
        } else {
            res as RelationalExpressionNode

            Assertions.assertEquals(numExpressions - 1, res.operators.size, "The number of operators is incorrect")
            Assertions.assertEquals(numExpressions, res.expressions.size, "The number of expressions is incorrect")

            for (op in res.operators.withIndex()) {
                Assertions.assertEquals(operator, op.value, "The operator[${op.index}] is incorrect")
            }

            for (exp in res.expressions) {
                LogicalExpressionNodeTest.checkTestExpression(exp)
            }
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect relational expression without operand after the operator`() {
        assertParserException {
            val text = "${LogicalExpressionNodeTest.testExpression}${RelationalExpressionNode.identityOperator}"
            val parser = LexemParser(CustomStringReader.from(text))
            RelationalExpressionNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = RelationalExpressionNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
