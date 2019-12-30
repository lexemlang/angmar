package org.lexem.angmar.analyzer.nodes.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ConditionalStmtAnalyzerTest {
    @Test
    fun `test correct if`() {
        val tagName = "tag"
        val varName = "test"
        val initialValue = 0
        val thenValue = 4
        val text =
                "${ConditionalStmtNode.ifKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AssignOperatorNode.assignOperator} $thenValue ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(thenValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test correct if wrong`() {
        val text =
                "${ConditionalStmtNode.ifKeyword} ${LogicNode.falseLiteral} ${BlockStmtNode.startToken} ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test correct else of if`() {
        val tagName = "tag"
        val varName = "test"
        val initialValue = 0
        val thenValue = 4
        val elseValue = 5
        val text =
                "${ConditionalStmtNode.ifKeyword} ${LogicNode.falseLiteral} ${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $thenValue ${BlockStmtNode.endToken} ${ConditionalStmtNode.elseKeyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AssignOperatorNode.assignOperator} $elseValue ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(elseValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test correct unless`() {
        val tagName = "tag"
        val varName = "test"
        val initialValue = 0
        val thenValue = 4
        val text =
                "${ConditionalStmtNode.unlessKeyword} ${LogicNode.falseLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AssignOperatorNode.assignOperator} $thenValue ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(thenValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test correct unless wrong`() {
        val text =
                "${ConditionalStmtNode.unlessKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test correct else of unless`() {
        val tagName = "tag"
        val varName = "test"
        val initialValue = 0
        val thenValue = 4
        val elseValue = 5
        val text =
                "${ConditionalStmtNode.unlessKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $thenValue ${BlockStmtNode.endToken} ${ConditionalStmtNode.elseKeyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AssignOperatorNode.assignOperator} $elseValue ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(elseValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test exit control signal without tag`() {
        val thenBody = ControlWithoutExpressionStmtNode.exitKeyword
        val text =
                "${ConditionalStmtNode.ifKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} $thenBody ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, ControlWithoutExpressionStmtNode.exitKeyword, null,
                null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag matching`() {
        val tagName = "tag"
        val thenBody = "${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagName"
        val text =
                "${ConditionalStmtNode.ifKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $thenBody ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag not matching`() {
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val thenBody = "${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagNameControl"
        val text =
                "${ConditionalStmtNode.ifKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $thenBody ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, ControlWithoutExpressionStmtNode.exitKeyword,
                tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without tag`(keyword: String) {
        val thenValue = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val text =
                "${ConditionalStmtNode.ifKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} $thenValue ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag`(keyword: String) {
        val tagName = "tag"
        val thenValue = "$keyword${BlockStmtNode.tagPrefix}$tagName"
        val text =
                "${ConditionalStmtNode.ifKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} $thenValue ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagName, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test return control signal`(keyword: String) {
        val value = LxmInteger.Num10
        val thenValue = "$keyword $value"
        val text =
                "${ConditionalStmtNode.ifKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} $thenValue ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
