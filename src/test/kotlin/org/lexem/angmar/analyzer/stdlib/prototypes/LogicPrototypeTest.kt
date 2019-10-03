package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LogicPrototypeTest {

    // PARAMETERS -------------------------------------------------------------

    companion object {

        @JvmStatic
        private fun provideCombinations(): Stream<Arguments> {
            val result = sequence {
                for (left in 0..1) {
                    for (right in 0..1) {
                        var leftValue = false
                        val leftStr = if (left == 1) {
                            leftValue = true
                            LogicNode.trueLiteral
                        } else {
                            LogicNode.falseLiteral
                        }

                        var rightValue = false
                        val rightStr = if (right == 1) {
                            rightValue = true
                            LogicNode.trueLiteral
                        } else {
                            LogicNode.falseLiteral
                        }

                        yield(Arguments.of(leftStr, rightStr, leftValue, rightValue))
                    }
                }
            }

            return result.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCombinations")
    fun `test and`(leftStr: String, rightStr: String, leftValue: Boolean, rightValue: Boolean) {
        val text = "$leftStr ${LogicalExpressionNode.andOperator} $rightStr"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(if (leftValue and rightValue) {
            LxmLogic.True
        } else {
            LxmLogic.False
        }, analyzer.memory.popStack(), "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideCombinations")
    fun `test or`(leftStr: String, rightStr: String, leftValue: Boolean, rightValue: Boolean) {
        val text = "$leftStr ${LogicalExpressionNode.orOperator} $rightStr"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicalExpressionNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(if (leftValue or rightValue) {
            LxmLogic.True
        } else {
            LxmLogic.False
        }, analyzer.memory.popStack(), "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideCombinations")
    fun `test xor`(leftStr: String, rightStr: String, leftValue: Boolean, rightValue: Boolean) {
        val text = "$leftStr ${LogicalExpressionNode.xorOperator} $rightStr"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(if (leftValue xor rightValue) {
            LxmLogic.True
        } else {
            LxmLogic.False
        }, analyzer.memory.popStack(), "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
