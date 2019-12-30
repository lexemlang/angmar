package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class RelationalExpressionAnalyzerTest {
    @Test
    fun `test equality`() {
        val leftVariableName = "leftVariable"
        val rightVariableName = "rightVariable"
        val text = "$leftVariableName ${RelationalExpressionNode.equalityOperator} $rightVariableName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( leftVariableName, LxmLogic.True)
        context.setProperty( rightVariableName, LxmLogic.True)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(leftVariableName, rightVariableName))
    }

    @Test
    fun `test inequality`() {
        val leftVariableName = "leftVariable"
        val rightVariableName = "rightVariable"
        val text = "$leftVariableName ${RelationalExpressionNode.inequalityOperator} $rightVariableName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( leftVariableName, LxmLogic.True)
        context.setProperty( rightVariableName, LxmLogic.True)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(leftVariableName, rightVariableName))
    }

    @Test
    fun `test identity`() {
        val leftVariableName = "leftVariable"
        val rightVariableName = "rightVariable"
        val text = "$leftVariableName ${RelationalExpressionNode.identityOperator} $rightVariableName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( leftVariableName, LxmLogic.True)
        context.setProperty( rightVariableName, LxmLogic.True)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(leftVariableName, rightVariableName))
    }

    @Test
    fun `test not identity`() {
        val leftVariableName = "leftVariable"
        val rightVariableName = "rightVariable"
        val text = "$leftVariableName ${RelationalExpressionNode.notIdentityOperator} $rightVariableName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( leftVariableName, LxmLogic.True)
        context.setProperty( rightVariableName, LxmLogic.True)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(leftVariableName, rightVariableName))
    }

    @Test
    fun `test lower than`() {
        val leftVariableName = "leftVariable"
        val rightVariableName = "rightVariable"
        val text = "$leftVariableName ${RelationalExpressionNode.lowerThanOperator} $rightVariableName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( leftVariableName, LxmInteger.Num0)
        context.setProperty( rightVariableName, LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(leftVariableName, rightVariableName))
    }

    @Test
    fun `test greater than`() {
        val leftVariableName = "leftVariable"
        val rightVariableName = "rightVariable"
        val text = "$leftVariableName ${RelationalExpressionNode.greaterThanOperator} $rightVariableName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( leftVariableName, LxmInteger.Num0)
        context.setProperty( rightVariableName, LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(leftVariableName, rightVariableName))
    }

    @Test
    fun `test lower or equal than`() {
        val leftVariableName = "leftVariable"
        val rightVariableName = "rightVariable"
        val text = "$leftVariableName ${RelationalExpressionNode.lowerOrEqualThanOperator} $rightVariableName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( leftVariableName, LxmInteger.Num0)
        context.setProperty( rightVariableName, LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(leftVariableName, rightVariableName))
    }

    @Test
    fun `test greater or equal than`() {
        val leftVariableName = "leftVariable"
        val rightVariableName = "rightVariable"
        val text = "$leftVariableName ${RelationalExpressionNode.greaterOrEqualThanOperator} $rightVariableName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( leftVariableName, LxmInteger.Num10)
        context.setProperty( rightVariableName, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(leftVariableName, rightVariableName))
    }

    @Test
    fun `test incorrect short-circuit mode`() {
        val leftVariableName = "leftVariable"
        val rightVariableName = "rightVariable"
        val text =
                "$leftVariableName ${RelationalExpressionNode.greaterThanOperator} $rightVariableName ${RelationalExpressionNode.greaterOrEqualThanOperator} test${AccessExplicitMemberNode.accessToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( leftVariableName, LxmInteger.Num0)
        context.setProperty( rightVariableName, LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(leftVariableName, rightVariableName))
    }

    @Test
    fun `test final simplification`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val variableName3 = "variable3"
        val text =
                "$variableName1 ${RelationalExpressionNode.lowerThanOperator} $variableName2 ${RelationalExpressionNode.greaterOrEqualThanOperator} $variableName3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmInteger.Num1)
        context.setProperty( variableName2, LxmInteger.Num10)
        context.setProperty( variableName3, LxmInteger.Num1)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2, variableName3))
    }

    @Test
    fun `test intermediate simplification`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text =
                "$variableName1 ${RelationalExpressionNode.lowerThanOperator} $variableName2 ${RelationalExpressionNode.greaterOrEqualThanOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmInteger.Num1)
        context.setProperty( variableName2, LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }
}
