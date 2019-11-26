package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ConditionalExpressionAnalyzerTest {
    @Test
    fun `test and left`() {
        val text = "${NilNode.nilLiteral} ${ConditionalExpressionNode.andOperator} 1"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test and right`() {
        val text = "${LogicNode.trueLiteral} ${ConditionalExpressionNode.andOperator} 1"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(1, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test and short-circuit`() {
        val text =
                "${LogicNode.falseLiteral} ${ConditionalExpressionNode.andOperator} test${AccessExplicitMemberNode.accessToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test or left`() {
        val text = "6 ${ConditionalExpressionNode.orOperator} 1"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(6, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test or right`() {
        val text = "${LogicNode.falseLiteral} ${ConditionalExpressionNode.orOperator} 1"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(1, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test or short-circuit`() {
        val text =
                "${LogicNode.trueLiteral} ${ConditionalExpressionNode.orOperator} test${AccessExplicitMemberNode.accessToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test xor left`() {
        val text = "6 ${ConditionalExpressionNode.orOperator} 1"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(6, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test xor right`() {
        val text = "6 ${ConditionalExpressionNode.orOperator} 1"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(6, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test xor short-circuit`() {
        val text =
                "${LogicNode.trueLiteral} ${ConditionalExpressionNode.orOperator} test${AccessExplicitMemberNode.accessToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test and short-circuit with others`() {
        val text =
                "${NilNode.nilLiteral} ${ConditionalExpressionNode.andOperator} 1 ${ConditionalExpressionNode.orOperator} 7"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(7, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test or short-circuit with others`() {
        val text = "6 ${ConditionalExpressionNode.orOperator} 1 ${ConditionalExpressionNode.andOperator} 7"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(7, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test xor short-circuit with others`() {
        val text = "6 ${ConditionalExpressionNode.xorOperator} 1 ${ConditionalExpressionNode.andOperator} 7"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(7, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
