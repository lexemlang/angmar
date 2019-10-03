package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.utils.*

internal class MultiplicativeExpressionAnalyzerTest {
    @Test
    fun `test multiplication`() {
        val text = "5 ${MultiplicativeExpressionNode.multiplicationOperator} 2"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = MultiplicativeExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(10, result.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test division`() {
        val text = "5 ${MultiplicativeExpressionNode.divisionOperator} 2"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = MultiplicativeExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(2, result.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test integer division`() {
        val text = "5 ${MultiplicativeExpressionNode.integerDivisionOperator} 2.0"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = MultiplicativeExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(2, result.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test reminder`() {
        val text = "5 ${MultiplicativeExpressionNode.reminderOperator} 2"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = MultiplicativeExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(1, result.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
