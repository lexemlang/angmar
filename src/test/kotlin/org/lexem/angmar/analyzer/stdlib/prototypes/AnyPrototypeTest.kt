package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class AnyPrototypeTest {

    // PARAMETERS -------------------------------------------------------------

    companion object {

        @JvmStatic
        private fun provideNotOptions(): Stream<Arguments> {
            val result = sequence {
                for (option in listOf(LxmNil, LxmInteger.Num0, LxmString.Empty, LxmFloat.from(0.1f))) {
                    yield(Arguments.of(option))
                }

                yield(Arguments.of(null))
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideGenericOptions(): Stream<Arguments> {
            val result = sequence {
                for (option in listOf(0, 1, 2, 3)) {
                    yield(Arguments.of(option))
                }
            }

            return result.asStream()
        }


        @JvmStatic
        private fun provideGenericOptionsWithOk(): Stream<Arguments> {
            val result = sequence {
                for (option in listOf(0, 1, 2, 3)) {
                    for (isOk in listOf(false, true)) {
                        yield(Arguments.of(option, isOk))
                    }
                }
            }

            return result.asStream()
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideGenericOptions")
    fun `test toString`(option: Int) {
        val valueTxt = when (option) {
            0 -> LxmNil.toString()
            1 -> LxmLogic.True.toString()
            2 -> "${FunctionNode.keyword}${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
            else -> "${ObjectNode.startToken}${ObjectNode.endToken}"
        }
        val fnCall =
                "${ParenthesisExpressionNode.startToken}$valueTxt${ParenthesisExpressionNode.endToken}${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}"
        val fnArgs = ""
        val grammar = "$fnCall${FunctionCallNode.startToken}$fnArgs${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be a LxmString")
            val expected = when (option) {
                2 -> "[Function <Anonymous function> at ??:2:10]"
                3 -> LxmString.ObjectToString.primitive
                else -> valueTxt
            }
            Assertions.assertEquals(expected, result.primitive, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @MethodSource("provideGenericOptionsWithOk")
    fun `test is`(option: Int, isOk: Boolean) {
        val valueTxt = when (option) {
            0 -> LxmNil.toString()
            1 -> LxmInteger.Num0.toString()
            2 -> "${FunctionNode.keyword}${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
            else -> "${ObjectNode.startToken}${ObjectNode.endToken}"
        }
        val fnCall =
                "${ParenthesisExpressionNode.startToken}$valueTxt${ParenthesisExpressionNode.endToken}${AccessExplicitMemberNode.accessToken}${AnyPrototype.Is}"
        val fnArgs = if (isOk) {
            when (option) {
                0 -> NilType.TypeName
                1 -> IntegerType.TypeName
                2 -> FunctionType.TypeName
                else -> ObjectType.TypeName
            }
        } else {
            StringType.TypeName
        }
        val grammar = "$fnCall${FunctionCallNode.startToken}$fnArgs${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @MethodSource("provideGenericOptionsWithOk")
    fun `test isAny`(option: Int, isOk: Boolean) {
        val valueTxt = when (option) {
            0 -> LxmNil.toString()
            1 -> LxmInteger.Num0.toString()
            2 -> "${FunctionNode.keyword}${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
            else -> "${ObjectNode.startToken}${ObjectNode.endToken}"
        }
        val fnCall =
                "${ParenthesisExpressionNode.startToken}$valueTxt${ParenthesisExpressionNode.endToken}${AccessExplicitMemberNode.accessToken}${AnyPrototype.IsAny}"
        val fnArgs = if (isOk) {
            listOf(NilType.TypeName, IntegerType.TypeName, FunctionType.TypeName, ObjectType.TypeName).joinToString(
                    FunctionCallNode.argumentSeparator)
        } else {
            StringType.TypeName
        }
        val grammar = "$fnCall${FunctionCallNode.startToken}$fnArgs${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")
        }
    }

    @Test
    fun `test isAny - empty`() {
        val valueTxt = LxmNil.toString()
        val fnCall =
                "${ParenthesisExpressionNode.startToken}$valueTxt${ParenthesisExpressionNode.endToken}${AccessExplicitMemberNode.accessToken}${AnyPrototype.IsAny}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.False, result, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @MethodSource("provideNotOptions")
    fun `test not`(option: LexemPrimitive?) {
        val optionTxt = when (option) {
            null -> {
                "${ListNode.startToken}${ListNode.endToken}"
            }
            is LxmString -> {
                "${StringNode.startToken}${option.primitive}${StringNode.endToken}"
            }
            else -> option.toString()
        }
        val isTrue = !RelationalFunctions.isTruthy(option ?: LxmLogic.True)
        val grammar = "${PrefixOperatorNode.notOperator}$optionTxt"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isTrue), result, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @MethodSource("provideGenericOptions")
    fun `test add`(option: Int) {
        val valueTxt = when (option) {
            0 -> LxmNil.toString()
            1 -> LxmLogic.True.toString()
            2 -> "${FunctionNode.keyword}${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
            else -> "${ObjectNode.startToken}${ObjectNode.endToken}"
        }
        val suffix = "avvc"
        val grammar =
                "$valueTxt ${AdditiveExpressionNode.additionOperator} ${StringNode.startToken}$suffix${StringNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be a LxmString")
            val expected = when (option) {
                0 -> LxmNil.toString()
                1 -> LxmLogic.True.toString()
                2 -> "[Function <Anonymous function> at ??:2:9]"
                else -> LxmString.ObjectToString.primitive
            }
            Assertions.assertEquals(expected + suffix, result.primitive, "The result is incorrect")
        }
    }
}
