package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LogicPrototypeTest {

    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun provideSingleCombinations(): Stream<Arguments> {
            val result = sequence {
                for (value in listOf(false, true)) {
                    val str = if (value) {
                        LogicNode.trueLiteral
                    } else {
                        LogicNode.falseLiteral
                    }

                    yield(Arguments.of(str, value))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideCombinations(): Stream<Arguments> {
            val result = sequence {
                for (left in listOf(false, true)) {
                    for (right in listOf(false, true)) {
                        val leftStr = if (left) {
                            LogicNode.trueLiteral
                        } else {
                            LogicNode.falseLiteral
                        }

                        val rightStr = if (right) {
                            LogicNode.trueLiteral
                        } else {
                            LogicNode.falseLiteral
                        }

                        yield(Arguments.of(leftStr, rightStr, left, right))
                    }
                }
            }

            return result.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideSingleCombinations")
    fun `test not`(str: String, value: Boolean) {
        val fnCall = "${PrefixOperatorNode.notOperator}$str"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(!value), result, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @MethodSource("provideCombinations")
    fun `test and`(leftStr: String, rightStr: String, leftValue: Boolean, rightValue: Boolean) {
        val fnCall = "$leftStr ${LogicalExpressionNode.andOperator} $rightStr"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(leftValue and rightValue), result, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @MethodSource("provideCombinations")
    fun `test or`(leftStr: String, rightStr: String, leftValue: Boolean, rightValue: Boolean) {
        val fnCall = "$leftStr ${LogicalExpressionNode.orOperator} $rightStr"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(leftValue or rightValue), result, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @MethodSource("provideCombinations")
    fun `test xor`(leftStr: String, rightStr: String, leftValue: Boolean, rightValue: Boolean) {
        val fnCall = "$leftStr ${LogicalExpressionNode.xorOperator} $rightStr"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(leftValue xor rightValue), result, "The result is incorrect")
        }
    }
}
