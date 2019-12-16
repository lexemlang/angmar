package org.lexem.angmar.analyzer.nodes.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.selective.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class SelectiveStmtAnalyzerTest {
    @Test
    fun `test without condition`() {
        val varName = "test"
        val block1 =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 100 ${BlockStmtNode.endToken}"
        val case1 = "${ElsePatternSelectiveStmtNode.elseKeyword} $block1"
        val block2 =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 10 ${BlockStmtNode.endToken}"
        val case2 = "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.falseLiteral} $block2"
        val text =
                "${SelectiveStmtNode.keyword} ${SelectiveStmtNode.startToken} $case2 \n $case1 ${SelectiveStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveStmtNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.Num10)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val variable = finalContext.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be LxmInteger")

        Assertions.assertEquals(110, variable.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test with condition`() {
        val varName = "test"
        val value = LxmInteger.Num2
        val block1 =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 100 ${BlockStmtNode.endToken}"
        val case1 = "541 $block1"
        val block2 =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 10 ${BlockStmtNode.endToken}"
        val case2 = "$value $block2"
        val text =
                "${SelectiveStmtNode.keyword} ${ParenthesisExpressionNode.startToken} $value ${ParenthesisExpressionNode.endToken} ${SelectiveStmtNode.startToken} $case2 \n $case1 ${SelectiveStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveStmtNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.Num10)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val variable = finalContext.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be LxmInteger")

        Assertions.assertEquals(20, variable.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test exit control signal without tag`() {
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val block = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val case = "${ElsePatternSelectiveStmtNode.elseKeyword} $block"
        val text = "${SelectiveStmtNode.keyword} ${SelectiveStmtNode.startToken} $case ${SelectiveStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag matching`() {
        val tagName = "tag1"
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val block = "${BlockStmtNode.startToken} $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val case = "${ElsePatternSelectiveStmtNode.elseKeyword} $block"
        val text =
                "${SelectiveStmtNode.keyword} ${SelectiveStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $case ${SelectiveStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag not matching`() {
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val block =
                "${BlockStmtNode.startToken} $keyword${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val case = "${ElsePatternSelectiveStmtNode.elseKeyword} $block"
        val text =
                "${SelectiveStmtNode.keyword} ${SelectiveStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $case ${SelectiveStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without tag`(keyword: String) {
        val block = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val case = "${ElsePatternSelectiveStmtNode.elseKeyword} $block"
        val text = "${SelectiveStmtNode.keyword} ${SelectiveStmtNode.startToken} $case ${SelectiveStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveStmtNode.Companion::parse)

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
        val block = "${BlockStmtNode.startToken} $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val case = "${ElsePatternSelectiveStmtNode.elseKeyword} $block"
        val text = "${SelectiveStmtNode.keyword} ${SelectiveStmtNode.startToken} $case ${SelectiveStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagName, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test return control signal`(keyword: String) {
        val value = LxmInteger.Num10
        val block = "${BlockStmtNode.startToken} $keyword $value ${BlockStmtNode.endToken}"
        val case = "${ElsePatternSelectiveStmtNode.elseKeyword} $block"
        val text = "${SelectiveStmtNode.keyword} ${SelectiveStmtNode.startToken} $case ${SelectiveStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
