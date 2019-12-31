package org.lexem.angmar.analyzer.stdlib.types

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ListTypeTest {
    @Test
    fun `test new`() {
        val size = 5
        val value = LxmInteger.Num10
        val fnCallArguments = listOf(size, value)
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.New}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            val list = result?.dereference(analyzer.memory, toWrite = false) as? LxmList ?: throw Error(
                    "The result must be a LxmList")
            Assertions.assertEquals(size, list.size, "The size of the result is incorrect")

            for ((i, v) in list.getAllCells().withIndex()) {
                Assertions.assertEquals(value, v, "The result[$i] is incorrect")
            }
        }
    }

    @Test
    fun `test new - no initial value`() {
        val size = 5
        val value = LxmNil
        val fnCallArguments = listOf(size)
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.New}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            val list = result?.dereference(analyzer.memory, toWrite = false) as? LxmList ?: throw Error(
                    "The result must be a LxmList")
            Assertions.assertEquals(size, list.size, "The size of the result is incorrect")

            for ((i, v) in list.getAllCells().withIndex()) {
                Assertions.assertEquals(value, v, "The result[$i] is incorrect")
            }
        }
    }

    @Test
    fun `test newFrom`() {
        val fnCallArguments = listOf(LxmNil, LxmInteger.Num10, LxmLogic.True)
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.NewFrom}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            val list = result?.dereference(analyzer.memory, toWrite = false) as? LxmList ?: throw Error(
                    "The result must be a LxmList")
            Assertions.assertEquals(fnCallArguments.size, list.size, "The size of the result is incorrect")

            for ((i, v) in list.getAllCells().toList().zip(fnCallArguments).withIndex()) {
                val (res, expected) = v
                Assertions.assertEquals(expected, res, "The result[$i] is incorrect")
            }
        }
    }

    @Test
    fun `test newFrom - empty`() {
        val fnCallArguments = listOf<LexemPrimitive>()
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.NewFrom}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            val list = result?.dereference(analyzer.memory, toWrite = false) as? LxmList ?: throw Error(
                    "The result must be a LxmList")
            Assertions.assertEquals(fnCallArguments.size, list.size, "The size of the result is incorrect")

            for ((i, v) in list.getAllCells().toList().zip(fnCallArguments).withIndex()) {
                val (res, expected) = v
                Assertions.assertEquals(expected, res, "The result[$i] is incorrect")
            }
        }
    }

    @Test
    fun `test concat`() {
        val list1 = listOf(LxmNil, LxmLogic.True)
        val list2 = listOf(LxmInteger.Num0)
        val fnCallArguments =
                listOf("${ListNode.startToken}${list1.joinToString(ListNode.elementSeparator)}${ListNode.endToken}",
                        "${ListNode.startToken}${list2.joinToString(ListNode.elementSeparator)}${ListNode.endToken}")
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.Concat}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            val list = result?.dereference(analyzer.memory, toWrite = false) as? LxmList ?: throw Error(
                    "The result must be a LxmList")
            Assertions.assertEquals(list1.size + list2.size, list.size, "The size of the result is incorrect")

            for ((i, v) in list.getAllCells().toList().zip(list1 + list2).withIndex()) {
                val (res, expected) = v
                Assertions.assertEquals(expected, res, "The result[$i] is incorrect")
            }
        }
    }

    @Test
    fun `test concat - empty`() {
        val fnCallArguments = listOf<LexemPrimitive>()
        val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.Concat}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            val list = result?.dereference(analyzer.memory, toWrite = false) as? LxmList ?: throw Error(
                    "The result must be a LxmList")
            Assertions.assertEquals(fnCallArguments.size, list.size, "The size of the result is incorrect")

            for ((i, v) in list.getAllCells().toList().zip(fnCallArguments).withIndex()) {
                val (res, expected) = v
                Assertions.assertEquals(expected, res, "The result[$i] is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test new - incorrect size type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val size = LxmNil
            val value = LxmInteger.Num10
            val fnCallArguments = listOf(size, value)
            val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.New}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test new - size lower than 0`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val size = -1
            val value = LxmInteger.Num10
            val fnCallArguments = listOf(size, value)
            val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.New}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test concat - incorrect value type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list1 = listOf(LxmNil, LxmLogic.True)
            val fnCallArguments =
                    listOf("${ListNode.startToken}${list1.joinToString(ListNode.elementSeparator)}${ListNode.endToken}",
                            3)
            val fnCall = "${ListType.TypeName}${AccessExplicitMemberNode.accessToken}${ListType.Concat}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }
}
