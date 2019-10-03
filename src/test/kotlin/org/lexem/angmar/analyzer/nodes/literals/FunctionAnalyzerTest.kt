package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class FunctionAnalyzerTest {
    @Test
    fun `test create literal`() {
        val text =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        analyzer.memory.popStack().dereference(analyzer.memory) as? LxmFunction ?: throw Error(
                "The result must be a LxmFunction")

        // Remove the function cyclic reference.
        analyzer.memory.spatialGarbageCollect()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test call without argument list`() {
        val varName = "test"
        val fnName = "fn"
        val value = LxmInteger.Num10
        val text = let {
            val callFn = "$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
            val body =
                    "${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $value ${BlockStmtNode.endToken}"
            val fn = "${FunctionNode.keyword} $body"
            val assign =
                    "${VarDeclarationStmtNode.variableKeyword} $fnName ${VarDeclarationStmtNode.assignOperator} $fn"
            "${BlockStmtNode.startToken} $assign \n $callFn ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare the context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        Assertions.assertEquals(value, finalContext.getPropertyValue(analyzer.memory, varName),
                "The $varName is incorrect")

        // Remove the function cyclic reference.
        analyzer.memory.spatialGarbageCollect()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test call with argument list`() {
        val varName = "test"
        val fnName = "fn"
        val paramName = "param"
        val value = LxmInteger.Num10
        val text = let {
            val callFn = "$fnName${FunctionCallNode.startToken} $value ${FunctionCallNode.endToken}"
            val body =
                    "${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $paramName ${BlockStmtNode.endToken}"
            val fn =
                    "${FunctionNode.keyword} ${FunctionParameterListNode.startToken} $paramName ${FunctionParameterListNode.endToken} $body"
            val assign =
                    "${VarDeclarationStmtNode.variableKeyword} $fnName ${VarDeclarationStmtNode.assignOperator} $fn"
            "${BlockStmtNode.startToken} $assign \n $callFn ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare the context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        Assertions.assertEquals(value, finalContext.getPropertyValue(analyzer.memory, varName),
                "The $varName is incorrect")

        // Remove the function cyclic reference.
        analyzer.memory.spatialGarbageCollect()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without expression`(keyword: String) {
        TestUtils.assertAnalyzerException {
            val fnName = "fn"
            val text = let {
                val callFn = "$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
                val body = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
                val fn = "${FunctionNode.keyword} $body"
                val assign =
                        "${VarDeclarationStmtNode.variableKeyword} $fnName ${VarDeclarationStmtNode.assignOperator} $fn"
                "${BlockStmtNode.startToken} $assign \n $callFn ${BlockStmtNode.endToken}"
            }
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
            TestUtils.checkEmptyStackAndContext(analyzer)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag and without expression`(keyword: String) {
        TestUtils.assertAnalyzerException {
            val fnName = "fn"
            val tagName = "tag"
            val text = let {
                val callFn = "$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
                val body =
                        "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
                val fn = "${FunctionNode.keyword} $body"
                val assign =
                        "${VarDeclarationStmtNode.variableKeyword} $fnName ${VarDeclarationStmtNode.assignOperator} $fn"
                "${BlockStmtNode.startToken} $assign \n $callFn ${BlockStmtNode.endToken}"
            }
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
            TestUtils.checkEmptyStackAndContext(analyzer)
        }
    }

    @Test
    fun `test return control signals`() {
        val keyword = ControlWithExpressionStmtNode.returnKeyword
        val varName = "test"
        val fnName = "fn"
        val value = LxmInteger.Num1
        val text = let {
            val callFn = "$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
            val body = "${BlockStmtNode.startToken} $keyword $value ${BlockStmtNode.endToken}"
            val fn = "${FunctionNode.keyword} $body"
            val assign =
                    "${VarDeclarationStmtNode.variableKeyword} $fnName ${VarDeclarationStmtNode.assignOperator} $fn"
            "${BlockStmtNode.startToken} $assign \n $varName ${AssignOperatorNode.assignOperator} $callFn ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare the context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        Assertions.assertEquals(value, finalContext.getPropertyValue(analyzer.memory, varName),
                "The $varName is incorrect")

        // Remove the function cyclic reference.
        analyzer.memory.spatialGarbageCollect()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }
}
