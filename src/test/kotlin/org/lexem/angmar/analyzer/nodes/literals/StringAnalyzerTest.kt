package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class StringAnalyzerTest {
    @Test
    fun `test with escaped expression and without text`() {
        val variableName = "testVar"
        val text =
                "${StringNode.startToken}${EscapedExpressionNode.startToken}$variableName${EscapedExpressionNode.endToken}${StringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = StringNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName, LxmInteger.from(4))
        context.setProperty( AnalyzerCommons.Identifiers.HiddenCurrentContextName, LxmString.Empty)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("4", result.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(variableName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test with escaped expression and with text`() {
        val variableName = "testVar"
        val text =
                "${StringNode.startToken}abc${EscapedExpressionNode.startToken}$variableName${EscapedExpressionNode.endToken}def${EscapedExpressionNode.startToken}$variableName${EscapedExpressionNode.endToken}${StringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = StringNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName, LxmString.from("4"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("abc4def4", result.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName))
    }
}

