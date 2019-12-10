package org.lexem.angmar.analyzer.nodes.functional.statements.selective

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

internal class SelectiveCaseStmtAnalyzerTest {
    @Test
    fun `test single-pattern case`() {
        val varName = "test"
        val block =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 10 ${BlockStmtNode.endToken}"
        val text = "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.trueLiteral} $block"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveCaseStmtNode.Companion::parse)

        // Prepare stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.Num10)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.Num10)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()

        Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")
        Assertions.assertEquals(LxmInteger.Num10,
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SelectiveCondition),
                "The mainValue is not in the stack")

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val variable = finalContext.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be LxmInteger")

        Assertions.assertEquals(20, variable.primitive, "The primitive property is incorrect")

        // Remove Last and SelectiveCondition from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test multi-pattern case`() {
        val varName = "test"
        val block =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 10 ${BlockStmtNode.endToken}"
        val text =
                "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.falseLiteral} ${SelectiveCaseStmtNode.patternSeparator} ${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.trueLiteral}  $block"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveCaseStmtNode.Companion::parse)

        // Prepare stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.Num10)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.Num10)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()

        Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")
        Assertions.assertEquals(LxmInteger.Num10,
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SelectiveCondition),
                "The mainValue is not in the stack")

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val variable = finalContext.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be LxmInteger")

        Assertions.assertEquals(20, variable.primitive, "The primitive property is incorrect")

        // Remove Last and SelectiveCondition from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test not matching case`() {
        val varName = "test"
        val block =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 10 ${BlockStmtNode.endToken}"
        val text =
                "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.falseLiteral} ${SelectiveCaseStmtNode.patternSeparator} 5 $block"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveCaseStmtNode.Companion::parse)

        // Prepare stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.Num10)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()

        Assertions.assertEquals(LxmLogic.False, result, "The result is incorrect")
        Assertions.assertEquals(LxmInteger.Num10,
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SelectiveCondition),
                "The mainValue is not in the stack")

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val variable = finalContext.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be LxmInteger")

        Assertions.assertEquals(10, variable.primitive, "The primitive property is incorrect")

        // Remove Last and SelectiveCondition from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test exit control signal with tag matching`() {
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val varName = "test"
        val tagName = "tag1"
        val block =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $keyword${BlockStmtNode.tagPrefix}$tagName \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 10 ${BlockStmtNode.endToken}"
        val text = "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.trueLiteral} $block"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveCaseStmtNode.Companion::parse)

        // Prepare stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.Num10)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()

        Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")
        Assertions.assertEquals(LxmInteger.Num10,
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SelectiveCondition),
                "The mainValue is not in the stack")

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val variable = finalContext.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be LxmInteger")

        Assertions.assertEquals(10, variable.primitive, "The primitive property is incorrect")

        // Remove Last and SelectiveCondition from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test exit control signal with tag not matching`() {
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val block =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $keyword${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val text = "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.trueLiteral} $block"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveCaseStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.Num10)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        Assertions.assertEquals(LxmInteger.Num10,
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SelectiveCondition),
                "The mainValue is not in the stack")

        // Remove SelectiveCondition from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without tag`(keyword: String) {
        val block = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val text = "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.trueLiteral} $block"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveCaseStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.Num10)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        Assertions.assertEquals(LxmInteger.Num10,
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SelectiveCondition),
                "The mainValue is not in the stack")

        // Remove SelectiveCondition from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag`(keyword: String) {
        val tagName = "tag"
        val block = "${BlockStmtNode.startToken} $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val text = "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.trueLiteral} $block"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveCaseStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.Num10)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagName, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        Assertions.assertEquals(LxmInteger.Num10,
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SelectiveCondition),
                "The mainValue is not in the stack")

        // Remove SelectiveCondition from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test return control signal`(keyword: String) {
        val value = LxmInteger.Num10
        val block = "${BlockStmtNode.startToken} $keyword $value ${BlockStmtNode.endToken}"
        val text = "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.trueLiteral} $block"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveCaseStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.Num10)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        Assertions.assertEquals(LxmInteger.Num10,
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SelectiveCondition),
                "The mainValue is not in the stack")

        // Remove SelectiveCondition from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}

