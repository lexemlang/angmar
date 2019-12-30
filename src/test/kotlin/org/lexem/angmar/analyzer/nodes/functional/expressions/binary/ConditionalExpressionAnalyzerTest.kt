package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class ConditionalExpressionAnalyzerTest {
    @Test
    fun `test and left`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text = "$variableName1 ${ConditionalExpressionNode.andOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmNil)
        context.setProperty( variableName2, LxmInteger.Num1)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test and right`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text = "$variableName1 ${ConditionalExpressionNode.andOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmLogic.True)
        context.setProperty( variableName2, LxmInteger.Num1)

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(1, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test and right negative`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text = "$variableName1 ${ConditionalExpressionNode.andOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmLogic.True)
        context.setProperty( variableName2, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test and short-circuit`() {
        val variableName1 = "variable1"
        val text =
                "$variableName1 ${ConditionalExpressionNode.andOperator} test${AccessExplicitMemberNode.accessToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmLogic.False)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1))
    }

    @Test
    fun `test or left`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text = "$variableName1 ${ConditionalExpressionNode.orOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmInteger.Num1)
        context.setProperty( variableName2, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmInteger.Num1, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test or right`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text = "$variableName1 ${ConditionalExpressionNode.orOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmLogic.False)
        context.setProperty( variableName2, LxmInteger.Num1)

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(1, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test or right negative`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text = "$variableName1 ${ConditionalExpressionNode.orOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmLogic.False)
        context.setProperty( variableName2, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test or short-circuit`() {
        val variableName1 = "variable1"
        val text =
                "$variableName1 ${ConditionalExpressionNode.orOperator} test${AccessExplicitMemberNode.accessToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmLogic.True)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1))
    }

    @Test
    fun `test xor left`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text = "$variableName1 ${ConditionalExpressionNode.xorOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmInteger.Num1)
        context.setProperty( variableName2, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmInteger.Num1, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test xor right`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text = "$variableName1 ${ConditionalExpressionNode.xorOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmLogic.False)
        context.setProperty( variableName2, LxmInteger.Num1)

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(1, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test xor both true`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text = "$variableName1 ${ConditionalExpressionNode.xorOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmLogic.True)
        context.setProperty( variableName2, LxmInteger.Num1)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test xor both false`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text = "$variableName1 ${ConditionalExpressionNode.xorOperator} $variableName2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmNil)
        context.setProperty( variableName2, LxmLogic.False)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test xor short-circuit`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val text =
                "$variableName1 ${ConditionalExpressionNode.xorOperator} $variableName2  ${ConditionalExpressionNode.xorOperator} test${AccessExplicitMemberNode.accessToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmLogic.True)
        context.setProperty( variableName2, LxmInteger.Num1)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName1, variableName2))
    }

    @Test
    fun `test mixed short-circuit`() {
        val variableName1 = "variable1"
        val variableName2 = "variable2"
        val variableName3 = "variable3"
        val variableName4 = "variable4"
        val variableName5 = "variable5"
        val variableName6 = "variable6"
        val variableName7 = "variable7"
        val text =
                "$variableName1 ${ConditionalExpressionNode.andOperator} $variableName2 ${ConditionalExpressionNode.orOperator} $variableName3 " + "${ConditionalExpressionNode.orOperator} $variableName4 ${ConditionalExpressionNode.andOperator} $variableName5 " + "${ConditionalExpressionNode.xorOperator} $variableName6 ${ConditionalExpressionNode.orOperator} $variableName7"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( variableName1, LxmInteger.Num1)
        context.setProperty( variableName2, LxmNil)
        context.setProperty( variableName3, LxmInteger.Num2)
        context.setProperty( variableName4, LxmInteger.Num0)
        context.setProperty( variableName5, LxmInteger.Num10)
        context.setProperty( variableName6, LxmLogic.False)
        context.setProperty( variableName7, LxmLogic.True)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmInteger.Num10, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(variableName1, variableName2, variableName3, variableName4, variableName5, variableName6,
                        variableName7))
    }
}
