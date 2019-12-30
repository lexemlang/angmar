package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.utils.*

internal class ExecutorLexemeAnalyzerTest {
    @Test
    fun `test not conditional`() {
        val varName = "a"
        val grammar =
                "${ExecutorLexemeNode.startToken}$varName ${AssignOperatorNode.assignOperator} 3${ExecutorLexemeNode.separatorToken}$varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 2${ExecutorLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = ExecutorLexemeNode.Companion::parse)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setPropertyAsContext(analyzer.memory, varName, LxmNil)
        context.setProperty( AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(5, (result as LxmInteger).primitive, "The returned value is incorrect")

        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val varValue = context.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The returned value must be a LxmInteger")
        Assertions.assertEquals(5, varValue.primitive, "The returned value is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test matching conditional`() {
        val varName = "a"
        val grammar =
                "${ExecutorLexemeNode.startToken}${ExecutorLexemeNode.conditionalToken}$varName ${AssignOperatorNode.assignOperator} 3${ExecutorLexemeNode.separatorToken}${LxmLogic.False}${ExecutorLexemeNode.separatorToken}${LxmLogic.True}${ExecutorLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = ExecutorLexemeNode.Companion::parse)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setPropertyAsContext(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(LxmLogic.True, result, "The returned value is incorrect")

        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val varValue = context.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The returned value must be a LxmInteger")
        Assertions.assertEquals(3, varValue.primitive, "The returned value is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test not matching conditional`() {
        val varName = "a"
        val grammar =
                "${ExecutorLexemeNode.startToken}${ExecutorLexemeNode.conditionalToken}$varName ${AssignOperatorNode.assignOperator} 3${ExecutorLexemeNode.separatorToken}${LxmLogic.False}${ExecutorLexemeNode.separatorToken}${LxmLogic.True}${ExecutorLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = ExecutorLexemeNode.Companion::parse)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setPropertyAsContext(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(LxmLogic.True, result, "The returned value is incorrect")

        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val varValue = context.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The returned value must be a LxmInteger")
        Assertions.assertEquals(3, varValue.primitive, "The returned value is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }
}
