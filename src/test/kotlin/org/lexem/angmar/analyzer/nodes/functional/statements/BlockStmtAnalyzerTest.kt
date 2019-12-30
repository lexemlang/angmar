package org.lexem.angmar.analyzer.nodes.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
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
        val variableName = "testVariable"
        val tagName = "tag"
        val text =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $variableName ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName, LxmString.from(tagName))

        TestUtils.processAndCheckEmpty(analyzer)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val contextName =
                context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.HiddenContextTag) as LxmString

        Assertions.assertEquals(tagName, contextName.primitive, "The contextName is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(variableName, AnalyzerCommons.Identifiers.HiddenContextTag))
    }

    @Test
    fun `test block without tag`() {
        val varName = "test"
        val value = LxmInteger.Num10
        val text =
                "${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $value ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

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
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(value, result, "The result is incorrect")

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val contextName = finalContext.getPropertyValue(analyzer.memory,
                AnalyzerCommons.Identifiers.HiddenContextTag) as LxmString

        Assertions.assertEquals(tagName, contextName.primitive, "The contextName is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName, AnalyzerCommons.Identifiers.HiddenContextTag))
    }

    @Test
    fun `test return control signals`() {
        val keyword = ControlWithExpressionStmtNode.returnKeyword
        val value = LxmInteger.Num1
        val text = let {
            val control = "$keyword $value"
            "${BlockStmtNode.startToken} $control ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)

    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without expression`(keyword: String) {
        val text = let {
            val control = keyword
            "${BlockStmtNode.startToken} $control ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag and without expression`(keyword: String) {
        val tagName = "tag"
        val text = let {
            val control = "$keyword${BlockStmtNode.tagPrefix}$tagName"
            "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $control ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagName, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenContextTag))
    }
}
