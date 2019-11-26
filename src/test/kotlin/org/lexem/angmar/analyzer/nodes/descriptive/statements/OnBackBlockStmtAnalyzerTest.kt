package org.lexem.angmar.analyzer.nodes.descriptive.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.selective.*
import org.lexem.angmar.utils.*

internal class OnBackBlockStmtAnalyzerTest {
    @Test
    fun `test on backtrack block without stopping the backtracking`() {
        val onBackBlock = "${OnBackBlockStmtNode.macroName}${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val text = "${BlockStmtNode.startToken}$onBackBlock\n${MacroBacktrackNode.macroName}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse,
                isDescriptiveCode = true)
        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward, bigNodeCount = 0)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test on backtrack block with arguments and stopping the backtracking`() {
        val paramName = "parameter"
        val varName = "variable"
        val value = 3
        val onBackBlockExpression = "$varName ${AssignOperatorNode.assignOperator} $paramName"
        val onBackBlockParams = "${ParenthesisExpressionNode.startToken}$paramName${ParenthesisExpressionNode.endToken}"
        val onBackBlock =
                "${OnBackBlockStmtNode.macroName}$onBackBlockParams${BlockStmtNode.startToken}$onBackBlockExpression\n${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.endToken}"
        val backtrackMacro =
                "${MacroBacktrackNode.macroName}${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
        val exitCondition =
                "${ConditionalStmtNode.unlessKeyword}${ParenthesisExpressionNode.startToken}$varName${ParenthesisExpressionNode.endToken}${BlockStmtNode.startToken}$backtrackMacro${BlockStmtNode.endToken}"
        val text = "${BlockStmtNode.startToken}$onBackBlock\n$exitCondition${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse,
                isDescriptiveCode = true)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 2)

        // Check variable.
        context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val variable = context.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be a LxmInteger")

        Assertions.assertEquals(value, variable.primitive, "The value of the variable is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test on backtrack block with conditional`() {
        val onBackBlock =
                "${OnBackBlockStmtNode.macroName}${ConditionalStmtNode.ifKeyword}${ParenthesisExpressionNode.startToken}${LxmLogic.False}${ParenthesisExpressionNode.endToken}${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val text = "${BlockStmtNode.startToken}$onBackBlock\n${MacroBacktrackNode.macroName}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse,
                isDescriptiveCode = true)
        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward, bigNodeCount = 0)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test on backtrack block with selective`() {
        val onBackBlock =
                "${OnBackBlockStmtNode.macroName}${SelectiveStmtNode.keyword} ${SelectiveStmtNode.startToken} ${ElsePatternSelectiveStmtNode.elseKeyword} ${BlockStmtNode.startToken}${BlockStmtNode.endToken} ${SelectiveStmtNode.endToken}"
        val text = "${BlockStmtNode.startToken}$onBackBlock\n${MacroBacktrackNode.macroName}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse,
                isDescriptiveCode = true)
        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward, bigNodeCount = 0)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without tag`(keyword: String) {
        val onBackBlock = "${OnBackBlockStmtNode.macroName}${BlockStmtNode.startToken}$keyword${BlockStmtNode.endToken}"
        val grammar =
                "${BlockStmtNode.startToken}$onBackBlock\n${MacroBacktrackNode.macroName}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockStmtNode.Companion::parse,
                isDescriptiveCode = true)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag`(keyword: String) {
        val tagName = "tag"
        val onBackBlockExpression = "$keyword${BlockStmtNode.tagPrefix}$tagName"
        val onBackBlock =
                "${OnBackBlockStmtNode.macroName}${BlockStmtNode.startToken}$onBackBlockExpression${BlockStmtNode.endToken}"
        val grammar =
                "${BlockStmtNode.startToken}$onBackBlock\n${MacroBacktrackNode.macroName}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockStmtNode.Companion::parse,
                isDescriptiveCode = true)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagName, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test return control signal`(keyword: String) {
        val value = LxmInteger.Num10
        val onBackBlockExpression = "$keyword $value"
        val onBackBlock =
                "${OnBackBlockStmtNode.macroName}${BlockStmtNode.startToken}$onBackBlockExpression${BlockStmtNode.endToken}"
        val grammar =
                "${BlockStmtNode.startToken}$onBackBlock\n${MacroBacktrackNode.macroName}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockStmtNode.Companion::parse,
                isDescriptiveCode = true)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
