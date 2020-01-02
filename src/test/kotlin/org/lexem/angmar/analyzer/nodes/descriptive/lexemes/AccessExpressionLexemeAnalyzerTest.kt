package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class AccessExpressionLexemeAnalyzerTest {
    @Test
    fun `test with no modifiers - simple value`() {
        val varName = "test"
        val text = varName
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AccessExpressionLexemeNode.Companion::parse)

        // Prepare context.
        val value = LxmInteger.from(5)
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, value)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(value, result, "The result is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test with no modifiers - implicit calling`() {
        val functionName = "test"
        val returnValue = 26
        val text = functionName
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AccessExpressionLexemeNode.Companion::parse)

        // Prepare context.
        val function = LxmFunction(analyzer.memory) { analyzer, arguments, _, _ ->
            val parserArguments = arguments.mapArguments(analyzer.memory, emptyList())

            val thisValue =
                    parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                            ?: LxmNil

            Assertions.assertNotNull(thisValue, "The ${AnalyzerCommons.Identifiers.This} cannot be null")

            analyzer.memory.addToStackAsLast(LxmInteger.from(returnValue))
            return@LxmFunction true
        }

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, functionName, function)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(returnValue, result.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(functionName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
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
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AccessExpressionLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val list = LxmList(analyzer.memory)
        for (i in 0 until cellIndex) {
            list.addCell(analyzer.memory, LxmNil)
        }

        list.addCell(analyzer.memory, LxmInteger.from(left))
        context.setProperty(analyzer.memory, varName, list)
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
