package org.lexem.angmar.analyzer.nodes.descriptive

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.primitives.setters.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class DataCapturingAccessLexemeAnalyzerTest {
    @Test
    fun `test with no modifiers`() {
        val varName = "test"
        val text = varName
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = DataCapturingAccessLexemeNode.Companion::parse)

        // Create variable in context.
        val value = LxmInteger.from(5)
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, value)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmAccessSetter ?: throw Error(
                "The result must be a LxmAccessSetter")
        Assertions.assertEquals(varName, result.variableName, "The context is incorrect")
        Assertions.assertEquals(value, result.getPrimitive(analyzer.memory), "The final value is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test with modifiers`() {
        val varName = "test"
        val cellIndex = 1
        val funName = AnalyzerCommons.Operators.Add
        val left = 3
        val right = 5
        val resultValue = left + right
        val text =
                "$varName${IndexerNode.startToken}$cellIndex${IndexerNode.endToken}${AccessExplicitMemberNode.accessToken}$funName${FunctionCallNode.startToken}$right${FunctionCallNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = DataCapturingAccessLexemeNode.Companion::parse)

        // Create variable in context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val list = LxmList()
        for (i in 0 until cellIndex) {
            list.addCell(analyzer.memory, LxmNil)
        }

        list.addCell(analyzer.memory, LxmInteger.from(left))
        val listRef = analyzer.memory.add(list)
        context.setProperty(analyzer.memory, varName, listRef)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }
}
