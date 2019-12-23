package org.lexem.angmar.compiler.functional.expressions.binary

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ConditionalExpressionCompiledTest {
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
    fun `test and left - with dynamics`() {
        val text = "${NilNode.nilLiteral} ${ConditionalExpressionNode.andOperator} variable"
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
    fun `test and right incorrect`() {
        val text = "${LogicNode.trueLiteral} ${ConditionalExpressionNode.andOperator} ${NilNode.nilLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

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
    fun `test or left - with dynamics`() {
        val text = "6 ${ConditionalExpressionNode.orOperator} variable"
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
    fun `test or right negative`() {
        val text = "${NilNode.nilLiteral} ${ConditionalExpressionNode.orOperator} ${LogicNode.falseLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

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
        val text = "6 ${ConditionalExpressionNode.xorOperator} ${LogicNode.falseLiteral}"
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
        val text = "${LogicNode.falseLiteral} ${ConditionalExpressionNode.xorOperator} 1"
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
    fun `test xor both true`() {
        val text = "${LogicNode.trueLiteral} ${ConditionalExpressionNode.xorOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test xor both false`() {
        val text = "${LogicNode.falseLiteral} ${ConditionalExpressionNode.xorOperator} ${LogicNode.falseLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test xor short-circuit`() {
        val text =
                "${LogicNode.trueLiteral} ${ConditionalExpressionNode.xorOperator} ${LogicNode.trueLiteral} ${ConditionalExpressionNode.xorOperator} test${AccessExplicitMemberNode.accessToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test mixed short-circuit`() {
        val text =
                "1 ${ConditionalExpressionNode.andOperator} $LxmNil ${ConditionalExpressionNode.orOperator} 3 ${ConditionalExpressionNode.orOperator} 4 ${ConditionalExpressionNode.andOperator} 10 ${ConditionalExpressionNode.xorOperator} ${LxmLogic.False} ${ConditionalExpressionNode.orOperator} ${LxmLogic.True}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmInteger.Num10, analyzer.memory.getLastFromStack(),
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
