package org.lexem.angmar.analyzer.nodes.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.utils.*

internal class BlockStmtAnalyzerTest {
    @Test
    fun `test empty block without tag`() {
        val text = "${BlockStmtNode.startToken} ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty block with tag`() {
        val tagName = "tag"
        val text = "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val contextName =
                context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.HiddenContextTag) as LxmString

        Assertions.assertEquals(tagName, contextName.primitive, "The contextName is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenContextTag))
    }

    @Test
    fun `test block without tag`() {
        val varName = "test"
        val value = LxmInteger.Num10
        val text =
                "${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $value ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(value, result, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test block with tag`() {
        val tagName = "tag"
        val varName = "test"
        val value = LxmInteger.Num10
        val text =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AssignOperatorNode.assignOperator} $value ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(value, result, "The result is incorrect")

        val contextFinal = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val contextName = contextFinal.getPropertyValue(analyzer.memory,
                AnalyzerCommons.Identifiers.HiddenContextTag) as LxmString

        Assertions.assertEquals(tagName, contextName.primitive, "The contextName is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName, AnalyzerCommons.Identifiers.HiddenContextTag))
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
            val fn = "${FunctionStmtNode.keyword} $fnName $body"
            "${BlockStmtNode.startToken} $fn \n $varName ${AssignOperatorNode.assignOperator} $callFn ${BlockStmtNode.endToken}"
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

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName, fnName))
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
                val fn = "${FunctionStmtNode.keyword} $fnName $body"
                "${BlockStmtNode.startToken} $fn \n $callFn ${BlockStmtNode.endToken}"
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
                val fn = "${FunctionStmtNode.keyword} $fnName $body"
                "${BlockStmtNode.startToken} $fn \n $callFn ${BlockStmtNode.endToken}"
            }
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
            TestUtils.checkEmptyStackAndContext(analyzer)
        }
    }
}
