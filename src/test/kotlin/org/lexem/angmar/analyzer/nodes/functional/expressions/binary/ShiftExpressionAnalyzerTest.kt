package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.*

internal class ShiftExpressionAnalyzerTest {
    @Test
    fun `test left shift`() {
        val text =
                "${BitlistNode.binaryPrefix}${BitlistNode.startToken}0110${BitlistNode.endToken} ${ShiftExpressionNode.leftShiftOperator} 2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ShiftExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue.set(0)
        Assertions.assertEquals(4, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test right shift`() {
        val text =
                "${BitlistNode.binaryPrefix}${BitlistNode.startToken}0110${BitlistNode.endToken} ${ShiftExpressionNode.rightShiftOperator} 2"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ShiftExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue.set(3)
        Assertions.assertEquals(4, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test left rotation`() {
        val text =
                "${BitlistNode.binaryPrefix}${BitlistNode.startToken}0110${BitlistNode.endToken} ${ShiftExpressionNode.leftRotationOperator} 3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ShiftExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue[2] = true
        resultValue[3] = true
        Assertions.assertEquals(4, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test right rotation`() {
        val text =
                "${BitlistNode.binaryPrefix}${BitlistNode.startToken}0110${BitlistNode.endToken} ${ShiftExpressionNode.rightRotationOperator} 3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ShiftExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue[0] = true
        resultValue[1] = true
        Assertions.assertEquals(4, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
