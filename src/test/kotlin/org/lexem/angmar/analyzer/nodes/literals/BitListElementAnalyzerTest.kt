package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.*

internal class BitListElementAnalyzerTest {
    @Test
    fun `test radix 2`() {
        val text = "0110"
        val analyzer = TestUtils.createAnalyzerFrom(text) { parser, parent ->
            BitlistElementNode.parse(parser, parent, 2)
        }

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")

        val resultValue = BitSet()
        resultValue[1] = true
        resultValue[2] = true
        Assertions.assertEquals(text.length, stackValue.primitive.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive.content,
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test radix 8`() {
        val text = "13"
        val analyzer = TestUtils.createAnalyzerFrom(text) { parser, parent ->
            BitlistElementNode.parse(parser, parent, 8)
        }

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")

        val resultValue = BitSet()
        resultValue[2] = true
        resultValue[4] = true
        resultValue[5] = true
        Assertions.assertEquals(text.length * 3, stackValue.primitive.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive.content,
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test radix 16`() {
        val text = "2aF"
        val analyzer = TestUtils.createAnalyzerFrom(text) { parser, parent ->
            BitlistElementNode.parse(parser, parent, 16)
        }

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")

        val resultValue = BitSet()
        resultValue[2] = true
        resultValue[4] = true
        resultValue[6] = true
        resultValue[8] = true
        resultValue[9] = true
        resultValue[10] = true
        resultValue[11] = true
        Assertions.assertEquals(text.length * 4, stackValue.primitive.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive.content,
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test expression`() {
        val text =
                "${EscapedExpressionNode.startToken}${BitlistNode.binaryPrefix}${BitlistNode.startToken}0101${BitlistNode.endToken}${EscapedExpressionNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text) { parser, parent ->
            BitlistElementNode.parse(parser, parent, 16)
        }

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")

        val resultValue = BitSet()
        resultValue[1] = true
        resultValue[3] = true
        Assertions.assertEquals(4, stackValue.primitive.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive.content,
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
