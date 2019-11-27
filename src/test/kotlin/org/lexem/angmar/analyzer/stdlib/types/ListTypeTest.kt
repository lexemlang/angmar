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

internal class ListTypeTest {
    @Test
    fun `test new`() {
        val varName = "test"
        val size = 5
        val value = LxmInteger.Num10
        val fnCallArguments = listOf(size, value)
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.New}"
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
        val list = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmList
                ?: throw Error("The result must be a LxmList")
        Assertions.assertEquals(size, list.listSize, "The size of the result is incorrect")
        for ((i, v) in list.getAllCells().withIndex()) {
            Assertions.assertEquals(value, v, "The result[$i] is incorrect")
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test new - no initial value`() {
        val varName = "test"
        val size = 5
        val value = LxmNil
        val fnCallArguments = listOf(size)
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.New}"
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
        val list = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmList
                ?: throw Error("The result must be a LxmList")
        Assertions.assertEquals(size, list.listSize, "The size of the result is incorrect")
        for ((i, v) in list.getAllCells().withIndex()) {
            Assertions.assertEquals(value, v, "The result[$i] is incorrect")
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test newFrom`() {
        val varName = "test"
        val fnCallArguments = listOf(LxmNil, LxmInteger.Num10, LxmLogic.True)
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.NewFrom}"
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
        val list = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmList
                ?: throw Error("The result must be a LxmList")
        Assertions.assertEquals(fnCallArguments.size, list.listSize, "The size of the result is incorrect")
        for ((i, v) in list.getAllCells().zip(fnCallArguments).withIndex()) {
            val (res, expected) = v
            Assertions.assertEquals(expected, res, "The result[$i] is incorrect")
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test newFrom - empty`() {
        val varName = "test"
        val fnCallArguments = listOf<LexemPrimitive>()
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.NewFrom}"
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
        val list = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmList
                ?: throw Error("The result must be a LxmList")
        Assertions.assertEquals(fnCallArguments.size, list.listSize, "The size of the result is incorrect")
        for ((i, v) in list.getAllCells().zip(fnCallArguments).withIndex()) {
            val (res, expected) = v
            Assertions.assertEquals(expected, res, "The result[$i] is incorrect")
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test concat`() {
        val varName = "test"
        val list1 = listOf(LxmNil, LxmLogic.True)
        val list2 = listOf(LxmInteger.Num0)
        val fnCallArguments =
                listOf("${ListNode.startToken}${list1.joinToString(ListNode.elementSeparator)}${ListNode.endToken}",
                        "${ListNode.startToken}${list2.joinToString(ListNode.elementSeparator)}${ListNode.endToken}")
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.Concat}"
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
        val list = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmList
                ?: throw Error("The result must be a LxmList")
        Assertions.assertEquals(list1.size + list2.size, list.listSize, "The size of the result is incorrect")
        for ((i, v) in list.getAllCells().zip(list1 + list2).withIndex()) {
            val (res, expected) = v
            Assertions.assertEquals(expected, res, "The result[$i] is incorrect")
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test concat - empty`() {
        val varName = "test"
        val fnCallArguments = listOf<LexemPrimitive>()
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.Concat}"
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
        val list = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmList
                ?: throw Error("The result must be a LxmList")
        Assertions.assertEquals(fnCallArguments.size, list.listSize, "The size of the result is incorrect")
        for ((i, v) in list.getAllCells().zip(fnCallArguments).withIndex()) {
            val (res, expected) = v
            Assertions.assertEquals(expected, res, "The result[$i] is incorrect")
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    @Incorrect
    fun `test new - incorrect size type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val varName = "test"
            val size = LxmNil
            val value = LxmInteger.Num10
            val fnCallArguments = listOf(size, value)
            val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.New}"
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
    fun `test new - size lower than 0`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val varName = "test"
            val size = -1
            val value = LxmInteger.Num10
            val fnCallArguments = listOf(size, value)
            val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.New}"
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
    fun `test concat - incorrect value type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val varName = "test"
            val list1 = listOf(LxmNil, LxmLogic.True)
            val fnCallArguments =
                    listOf("${ListNode.startToken}${list1.joinToString(ListNode.elementSeparator)}${ListNode.endToken}",
                            3)
            val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.Concat}"
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
