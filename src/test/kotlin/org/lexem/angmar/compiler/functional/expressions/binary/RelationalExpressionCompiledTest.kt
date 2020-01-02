package org.lexem.angmar.compiler.functional.expressions.binary

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class RelationalExpressionCompiledTest {
    @Test
    fun `test equality`() {
        val text = "${LogicNode.trueLiteral} ${RelationalExpressionNode.equalityOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test inequality`() {
        val text = "${LogicNode.trueLiteral} ${RelationalExpressionNode.inequalityOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test identity`() {
        val text = "${LogicNode.trueLiteral} ${RelationalExpressionNode.identityOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test not identity`() {
        val text = "${LogicNode.trueLiteral} ${RelationalExpressionNode.notIdentityOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test lower than`() {
        val text = "2 ${RelationalExpressionNode.lowerThanOperator} 55"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test greater than`() {
        val text = "2 ${RelationalExpressionNode.greaterThanOperator} 55"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test lower or equal than`() {
        val text = "6 ${RelationalExpressionNode.lowerOrEqualThanOperator} 6"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test greater or equal than`() {
        val text = "6 ${RelationalExpressionNode.greaterOrEqualThanOperator} 6"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test short-circuit mode`() {
        val text =
                "6 ${RelationalExpressionNode.greaterThanOperator} 8 ${RelationalExpressionNode.greaterOrEqualThanOperator} test${AccessExplicitMemberNode.accessToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test final simplification`() {
        val variableName = "testVariable"
        val text =
                "$variableName ${RelationalExpressionNode.lowerThanOperator} 20 ${RelationalExpressionNode.greaterOrEqualThanOperator} 3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, variableName, LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName))
    }

    @Test
    fun `test intermediate simplification`() {
        val variableName = "testVariable"
        val text =
                "3 ${RelationalExpressionNode.lowerThanOperator} $variableName ${RelationalExpressionNode.greaterOrEqualThanOperator} $variableName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, variableName, LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName))
    }
}
