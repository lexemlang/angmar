package org.lexem.angmar.analyzer.stdlib.types

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import kotlin.streams.*

internal class StringTypeTest {
    @Test
    fun `test join`() {
        val varName = "test"
        val fnCallArguments = listOf(LxmNil, LxmInteger.Num10, LxmLogic.True)
        val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.Join}"
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                        FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"
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

        Assertions.assertEquals(fnCallArguments.joinToString(""), result.primitive, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test join - empty`() {
        val varName = "test"
        val fnCallArguments = listOf<String>()
        val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.Join}"
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                        FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"
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

        Assertions.assertEquals("", result.primitive, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test joinBy`() {
        val varName = "test"
        val separator = "--"
        val varargs = listOf(LxmNil, LxmInteger.Num10, LxmLogic.True)
        val fnCallArguments = mutableListOf<Any>("${StringNode.startToken}$separator${StringNode.endToken}")
        fnCallArguments.addAll(varargs)
        val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.JoinBy}"
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                        FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"
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

        Assertions.assertEquals(varargs.joinToString(separator), result.primitive, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test joinBy - empty`() {
        val varName = "test"
        val separator = "--"
        val fnCallArguments = mutableListOf<Any>("${StringNode.startToken}$separator${StringNode.endToken}")
        val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.JoinBy}"
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                        FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"
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

        Assertions.assertEquals("", result.primitive, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test fromUnicodePoints`() {
        val varName = "test"
        val value = "this is a test"
        val fnCallArguments = value.codePoints().toList()
        val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.FromUnicodePoints}"
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                        FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"
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

        Assertions.assertEquals(value, result.primitive, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    @Incorrect
    fun `test joinBy - incorrect separator type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val varName = "test"
            val fnCallArguments = mutableListOf<Any>(3)
            val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.JoinBy}"
            val grammar =
                    "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                            FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test fromUnicodePoints - incorrect point type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val varName = "test"
            val fnCallArguments = listOf(3, LxmNil)
            val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.FromUnicodePoints}"
            val grammar =
                    "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                            FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
