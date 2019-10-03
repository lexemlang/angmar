package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.primitives.setters.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class AccessExpressionAnalyzerTest {
    @Test
    fun `test getter with no modifiers`() {
        val varName = "test"
        val text = varName
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AccessExpressionNode.Companion::parse)

        // Create variable in context.
        val value = LxmInteger.from(5)
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, value)

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.popStack() as? LxmAccessSetter ?: throw Error("The result must be a LxmAccessSetter")
        Assertions.assertTrue(result.context is LxmReference, "The context property is incorrect")
        Assertions.assertEquals(varName, result.variableName, "The variableName property is incorrect")
        Assertions.assertEquals(value, result.dereference(analyzer.memory), "The value property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test getter with modifiers`() {
        val varName = "test"
        val cellIndex = 1
        val funName = AnalyzerCommons.Operators.Add
        val left = 3
        val right = 5
        val resultValue = left + right
        val text =
                "$varName${IndexerNode.startToken}$cellIndex${IndexerNode.endToken}${AccessExplicitMemberNode.accessToken}$funName${FunctionCallNode.startToken}$right${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AccessExpressionNode.Companion::parse)

        // Create variable in context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val list = LxmList()
        for (i in 0 until cellIndex) {
            list.addCell(analyzer.memory, LxmNil)
        }

        list.addCell(analyzer.memory, LxmInteger.from(left))
        val listRef = analyzer.memory.add(list)
        context.setProperty(analyzer.memory, varName, listRef)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test setter`() {
        val varName = "test"
        val right = 77
        val text = "$varName ${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Create variable in context.
        val value = LxmInteger.from(5)
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, value)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(right, result.primitive, "The element property is incorrect")

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = finalContext.getDereferencedProperty<LxmInteger>(analyzer.memory, varName) ?: throw Error(
                "The result must be a LxmInteger")
        Assertions.assertEquals(right, property.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }
}
