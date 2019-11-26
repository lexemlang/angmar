package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class RightExpressionAnalyzerTest {
    @Test
    fun `test primitive`() {
        val text = LogicNode.trueLiteral
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RightExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test setter`() {
        val varName = "test"
        val value = LxmLogic.True
        val text = varName
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RightExpressionNode.Companion::parse)

        // Prepare context.
        val ctx = AnalyzerCommons.getCurrentContext(analyzer.memory)
        ctx.setProperty(analyzer.memory, varName, value)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }
}
