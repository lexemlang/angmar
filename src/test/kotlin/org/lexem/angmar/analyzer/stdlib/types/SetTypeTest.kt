package org.lexem.angmar.analyzer.stdlib.types

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class SetTypeTest {
    @Test
    fun `test newFrom`() {
        val varName = "test"
        val fnCallArguments = listOf(LxmNil, LxmInteger.Num10, LxmLogic.True)
        val fnCall = "${SetType.TypeName}${AccessExplicitMemberNode.accessToken}${SetType.NewFrom}"
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
        val set = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmSet
                ?: throw Error("The result must be a LxmSet")

        var size = 0
        for ((_, propList) in set.getAllValues()) {
            for (prop in propList) {
                Assertions.assertTrue(prop.value in fnCallArguments, "The result[${prop.value}] is incorrect")

                size += 1
            }
        }
        Assertions.assertEquals(fnCallArguments.size, size, "The size of the result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test newFrom - empty`() {
        val varName = "test"
        val fnCallArguments = listOf<LexemPrimitive>()
        val fnCall = "${SetType.TypeName}${AccessExplicitMemberNode.accessToken}${SetType.NewFrom}"
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
        val set = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmSet
                ?: throw Error("The result must be a LxmSet")

        var size = 0
        for ((_, propList) in set.getAllValues()) {
            for (prop in propList) {
                Assertions.assertTrue(prop.value in fnCallArguments, "The result[${prop.value}] is incorrect")

                size += 1
            }
        }
        Assertions.assertEquals(fnCallArguments.size, size, "The size of the result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test join`() {
        val varName = "test"
        val list1 = listOf(LxmNil, LxmLogic.True)
        val list2 = listOf(LxmInteger.Num0)
        val fnCallArguments = listOf("${SetNode.macroName}${ListNode.startToken}${list1.joinToString(
                ListNode.elementSeparator)}${ListNode.endToken}",
                "${SetNode.macroName}${ListNode.startToken}${list2.joinToString(
                        ListNode.elementSeparator)}${ListNode.endToken}")
        val fnCall = "${SetType.TypeName}${AccessExplicitMemberNode.accessToken}${SetType.Join}"
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
        val set = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmSet
                ?: throw Error("The result must be a LxmSet")

        val listResult = list1 + list2
        var size = 0
        for ((_, propList) in set.getAllValues()) {
            for (prop in propList) {
                Assertions.assertTrue(prop.value in listResult, "The result[${prop.value}] is incorrect")

                size += 1
            }
        }
        Assertions.assertEquals(listResult.size, size, "The size of the result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test join - empty`() {
        val varName = "test"
        val fnCallArguments = listOf<LexemPrimitive>()
        val fnCall = "${SetType.TypeName}${AccessExplicitMemberNode.accessToken}${SetType.Join}"
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
        val set = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmSet
                ?: throw Error("The result must be a LxmSet")

        var size = 0
        for ((_, propList) in set.getAllValues()) {
            for (prop in propList) {
                size += 1
            }
        }
        Assertions.assertEquals(fnCallArguments.size, size, "The size of the result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    @Incorrect
    fun `test join - incorrect value type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val varName = "test"
            val list1 = listOf(LxmNil, LxmLogic.True)
            val fnCallArguments =
                    listOf("${ListNode.startToken}${list1.joinToString(ListNode.elementSeparator)}${ListNode.endToken}",
                            3)
            val fnCall = "${SetType.TypeName}${AccessExplicitMemberNode.accessToken}${SetType.Join}"
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
