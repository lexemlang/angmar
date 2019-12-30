package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.statements.selective.*
import org.lexem.angmar.utils.*

internal class ExpressionPatternSelectiveStmtAnalyzerTest {
    @Test
    fun `test only expression`() {
        val text = "3 ${AdditiveExpressionNode.additionOperator} 2"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = ExpressionPatternSelectiveStmtNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.from(5))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()

        Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")

        // Remove Accumulator and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test not matching only expression`() {
        val text = "3 ${AdditiveExpressionNode.additionOperator} 2"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = ExpressionPatternSelectiveStmtNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmString.Nil)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()

        Assertions.assertEquals(LxmLogic.False, result, "The result is incorrect")

        // Remove Accumulator and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test expression with if conditional`() {
        val varName = "test"
        val text =
                "3 ${AdditiveExpressionNode.additionOperator} 2 ${ConditionalPatternSelectiveStmtNode.ifKeyword} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 10"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = ExpressionPatternSelectiveStmtNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( varName, LxmInteger.Num10)
        context.setProperty( AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.from(5))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()

        Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")

        // Remove Accumulator and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
        analyzer.memory.removeLastFromStack()

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val variable = finalContext.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be LxmInteger")

        Assertions.assertEquals(20, variable.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test expression with unless conditional`() {
        val varName = "test"
        val text =
                "3 ${AdditiveExpressionNode.additionOperator} 2 ${ConditionalPatternSelectiveStmtNode.unlessKeyword} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 10"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = ExpressionPatternSelectiveStmtNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( varName, LxmInteger.Num10)
        context.setProperty( AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmInteger.from(5))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()

        Assertions.assertEquals(LxmLogic.False, result, "The result is incorrect")

        // Remove Accumulator and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
        analyzer.memory.removeLastFromStack()

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val variable = finalContext.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be LxmInteger")

        Assertions.assertEquals(20, variable.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test not matching expression with not executed conditional`() {
        val varName = "test"
        val text =
                "3 ${AdditiveExpressionNode.additionOperator} 2 ${ConditionalPatternSelectiveStmtNode.ifKeyword} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 10"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = ExpressionPatternSelectiveStmtNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( varName, LxmInteger.Num10)
        context.setProperty( AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmString.Nil)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()

        Assertions.assertEquals(LxmLogic.False, result, "The result is incorrect")

        // Remove Accumulator and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
        analyzer.memory.removeLastFromStack()

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val variable = finalContext.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be LxmInteger")

        Assertions.assertEquals(10, variable.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }
}
