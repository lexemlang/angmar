package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.*

internal class BitListAnalyzerTest {
    @Test
    fun `test radix 2`() {
        val text = "${BitlistNode.binaryPrefix}${BitlistNode.startToken}0110${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue[1] = true
        resultValue[2] = true
        Assertions.assertEquals(4, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test radix 8`() {
        val text = "${BitlistNode.octalPrefix}${BitlistNode.startToken}13${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue[2] = true
        resultValue[4] = true
        resultValue[5] = true
        Assertions.assertEquals(6, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }


    @Test
    fun `test radix 16`() {
        val text = "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}2aF${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue[2] = true
        resultValue[4] = true
        resultValue[6] = true
        resultValue[8] = true
        resultValue[9] = true
        resultValue[10] = true
        resultValue[11] = true
        Assertions.assertEquals(12, stackValue.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty radix 2`() {
        val text = "${BitlistNode.binaryPrefix}${BitlistNode.startToken}${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        Assertions.assertEquals(LxmBitList.Empty, stackValue, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty radix 8`() {
        val text = "${BitlistNode.octalPrefix}${BitlistNode.startToken}${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        Assertions.assertEquals(LxmBitList.Empty, stackValue, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }


    @Test
    fun `test empty radix 16`() {
        val text = "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        Assertions.assertEquals(LxmBitList.Empty, stackValue, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
