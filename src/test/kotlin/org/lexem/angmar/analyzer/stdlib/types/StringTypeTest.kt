package org.lexem.angmar.analyzer.stdlib.types

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import kotlin.streams.*

internal class StringTypeTest {
    @Test
    fun `test join`() {
        val fnCallArguments = listOf(LxmNil, LxmInteger.Num10, LxmLogic.True)
        val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.Join}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be a LxmString")
            Assertions.assertEquals(fnCallArguments.joinToString(""), result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test join - empty`() {
        val fnCallArguments = listOf<String>()
        val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.Join}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be a LxmString")
            Assertions.assertEquals("", result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test joinBy`() {
        val separator = "--"
        val varargs = listOf(LxmNil, LxmInteger.Num10, LxmLogic.True)
        val fnCallArguments = mutableListOf<Any>("${StringNode.startToken}$separator${StringNode.endToken}")
        fnCallArguments.addAll(varargs)
        val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.JoinBy}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be a LxmString")
            Assertions.assertEquals(varargs.joinToString(separator), result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test joinBy - empty`() {
        val separator = "--"
        val fnCallArguments = mutableListOf<Any>("${StringNode.startToken}$separator${StringNode.endToken}")
        val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.JoinBy}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be a LxmString")
            Assertions.assertEquals("", result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test fromUnicodePoints`() {
        val value = "this is a test"
        val fnCallArguments = value.codePoints().toList()
        val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.FromUnicodePoints}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be a LxmString")
            Assertions.assertEquals(value, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test joinBy - incorrect separator type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val fnCallArguments = mutableListOf<Any>(3)
            val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.JoinBy}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test fromUnicodePoints - incorrect point type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val fnCallArguments = listOf(3, LxmNil)
            val fnCall = "${StringType.TypeName}${AccessExplicitMemberNode.accessToken}${StringType.FromUnicodePoints}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }
}
