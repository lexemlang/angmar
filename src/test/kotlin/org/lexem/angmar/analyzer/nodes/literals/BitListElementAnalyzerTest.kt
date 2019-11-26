package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.*

internal class BitListElementAnalyzerTest {
    @Test
    fun `test radix 2`() {
        val text = "0110"
        val analyzer = TestUtils.createAnalyzerFrom(text) { parser, parent, parentSignal ->
            BitlistElementNode.parse(parser, parent, parentSignal, 2)
        }

        // Prepare stack.
        val initialSize = 3
        val initialValue = BitSet()
        initialValue[1] = true
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmBitList(initialSize, initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmBitList ?: throw Error(
                        "The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        for (i in 0 until initialSize) {
            resultValue[i] = initialValue[i]
        }

        resultValue[1 + initialSize] = true
        resultValue[2 + initialSize] = true
        Assertions.assertEquals(4 + initialSize, stackValue.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test radix 8`() {
        val text = "13"
        val analyzer = TestUtils.createAnalyzerFrom(text) { parser, parent, parentSignal ->
            BitlistElementNode.parse(parser, parent, parentSignal, 8)
        }

        // Prepare stack.
        val initialSize = 3
        val initialValue = BitSet()
        initialValue[1] = true
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmBitList(initialSize, initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmBitList ?: throw Error(
                        "The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        for (i in 0 until initialSize) {
            resultValue[i] = initialValue[i]
        }

        resultValue[2 + initialSize] = true
        resultValue[4 + initialSize] = true
        resultValue[5 + initialSize] = true
        Assertions.assertEquals(6 + initialSize, stackValue.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test radix 16`() {
        val text = "2aF"
        val analyzer = TestUtils.createAnalyzerFrom(text) { parser, parent, parentSignal ->
            BitlistElementNode.parse(parser, parent, parentSignal, 16)
        }

        // Prepare stack.
        val initialSize = 3
        val initialValue = BitSet()
        initialValue[1] = true
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmBitList(initialSize, initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmBitList ?: throw Error(
                        "The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        for (i in 0 until initialSize) {
            resultValue[i] = initialValue[i]
        }

        resultValue[2 + initialSize] = true
        resultValue[4 + initialSize] = true
        resultValue[6 + initialSize] = true
        resultValue[8 + initialSize] = true
        resultValue[9 + initialSize] = true
        resultValue[10 + initialSize] = true
        resultValue[11 + initialSize] = true
        Assertions.assertEquals(12 + initialSize, stackValue.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test expression`() {
        val text =
                "${EscapedExpressionNode.startToken}${BitlistNode.binaryPrefix}${BitlistNode.startToken}0101${BitlistNode.endToken}${EscapedExpressionNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text) { parser, parent, parentSignal ->
            BitlistElementNode.parse(parser, parent, parentSignal, 16)
        }

        // Prepare stack.
        val initialSize = 3
        val initialValue = BitSet()
        initialValue[1] = true
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmBitList(initialSize, initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmBitList ?: throw Error(
                        "The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        for (i in 0 until initialSize) {
            resultValue[i] = initialValue[i]
        }

        resultValue[1 + initialSize] = true
        resultValue[3 + initialSize] = true
        Assertions.assertEquals(4 + initialSize, stackValue.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    @Incorrect
    fun `test incorrect expression`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val text = "${EscapedExpressionNode.startToken}1234${EscapedExpressionNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(text) { parser, parent, parentSignal ->
                BitlistElementNode.parse(parser, parent, parentSignal, 16)
            }

            // Prepare stack.
            val initialSize = 3
            val initialValue = BitSet()
            initialValue[1] = true
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmBitList(initialSize, initialValue))

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
