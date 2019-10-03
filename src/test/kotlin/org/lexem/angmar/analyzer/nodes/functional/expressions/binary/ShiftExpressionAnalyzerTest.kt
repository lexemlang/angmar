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
                "${BitlistNode.binaryPrefix}${BitlistNode.startToken}0110${BitlistNode.endToken} ${ShiftExpressionNode.leftShiftOperator} 3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ShiftExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        Assertions.assertEquals(1, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test right shift`() {
        val text =
                "${BitlistNode.binaryPrefix}${BitlistNode.startToken}0110${BitlistNode.endToken} ${ShiftExpressionNode.rightShiftOperator} 3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ShiftExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue[4] = true
        resultValue[5] = true
        Assertions.assertEquals(7, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test left rotation`() {
        val text =
                "${BitlistNode.binaryPrefix}${BitlistNode.startToken}0110${BitlistNode.endToken} ${ShiftExpressionNode.leftRotationOperator} 3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ShiftExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)


        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue[2] = true
        resultValue[3] = true
        Assertions.assertEquals(4, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test right rotation`() {
        val text =
                "${BitlistNode.binaryPrefix}${BitlistNode.startToken}0110${BitlistNode.endToken} ${ShiftExpressionNode.rightRotationOperator} 3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ShiftExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)


        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue[0] = true
        resultValue[1] = true
        Assertions.assertEquals(4, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
