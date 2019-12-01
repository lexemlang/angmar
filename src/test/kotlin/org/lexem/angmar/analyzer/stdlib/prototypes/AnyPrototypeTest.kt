package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.*
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
        val varName = "test"
        val fnCall =
                "${ParenthesisExpressionNode.startToken}$valueTxt${ParenthesisExpressionNode.endToken}${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}"
        val fnArgs = ""
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}$fnArgs${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val result = context.getPropertyValue(analyzer.memory, varName) as? LxmString ?: throw Error(
                "The result must be a LxmString")
        val expected = when (option) {
            2 -> "[Function ?? at ??:1:26]"
            3 -> LxmString.ObjectToString.primitive
            else -> valueTxt
        }
        Assertions.assertEquals(expected, result.primitive, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
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
        val varName = "test"
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
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}$fnArgs${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        Assertions.assertEquals(LxmLogic.from(isOk), context.getPropertyValue(analyzer.memory, varName),
                "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
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
        val varName = "test"
        val fnCall =
                "${ParenthesisExpressionNode.startToken}$valueTxt${ParenthesisExpressionNode.endToken}${AccessExplicitMemberNode.accessToken}${AnyPrototype.IsAny}"
        val fnArgs = if (isOk) {
            listOf(NilType.TypeName, IntegerType.TypeName, FunctionType.TypeName, ObjectType.TypeName).joinToString(
                    FunctionCallNode.argumentSeparator)
        } else {
            StringType.TypeName
        }
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}$fnArgs${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        Assertions.assertEquals(LxmLogic.from(isOk), context.getPropertyValue(analyzer.memory, varName),
                "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test isAny - empty`() {
        val valueTxt = LxmNil.toString()
        val varName = "test"
        val fnCall =
                "${ParenthesisExpressionNode.startToken}$valueTxt${ParenthesisExpressionNode.endToken}${AccessExplicitMemberNode.accessToken}${AnyPrototype.IsAny}"
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        Assertions.assertEquals(LxmLogic.False, context.getPropertyValue(analyzer.memory, varName),
                "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
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
        val varName = "test"
        val fnCall = "${PrefixOperatorNode.notOperator}$optionTxt"
        val grammar = "$varName ${AssignOperatorNode.assignOperator} $fnCall"
        val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        Assertions.assertEquals(LxmLogic.from(isTrue), context.getPropertyValue(analyzer.memory, varName),
                "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
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
        val varName = "test"
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $valueTxt ${AdditiveExpressionNode.additionOperator} ${StringNode.startToken}$suffix${StringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val result = context.getPropertyValue(analyzer.memory, varName) as? LxmString ?: throw Error(
                "The result must be a LxmString")
        val expected = when (option) {
            0 -> LxmNil.toString()
            1 -> LxmLogic.True.toString()
            2 -> "[Function ?? at ??:1:22]"
            else -> LxmString.ObjectToString.primitive
        }
        Assertions.assertEquals(expected + suffix, result.primitive, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }
}
