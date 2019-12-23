package org.lexem.angmar.compiler.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.*

internal class BitlistCompiledTest {
    @Test
    fun `test radix 2`() {
        val bits = "0110"
        val text = "${BitlistNode.binaryPrefix}${BitlistNode.startToken}$bits${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")

        val resultValue = BitSet()
        resultValue[1] = true
        resultValue[2] = true
        Assertions.assertEquals(bits.length, stackValue.primitive.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive.content,
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test radix 8`() {
        val text = "${BitlistNode.octalPrefix}${BitlistNode.startToken}13${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue[2] = true
        resultValue[4] = true
        resultValue[5] = true
        Assertions.assertEquals(6, stackValue.primitive.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive.content,
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test radix 16`() {
        val text = "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}2aF${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
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
        Assertions.assertEquals(12, stackValue.primitive.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive.content,
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty radix 2`() {
        val text = "${BitlistNode.binaryPrefix}${BitlistNode.startToken}${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")
        Assertions.assertEquals(LxmBitList.Empty, stackValue, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty radix 8`() {
        val text = "${BitlistNode.octalPrefix}${BitlistNode.startToken}${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")
        Assertions.assertEquals(LxmBitList.Empty, stackValue, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty radix 16`() {
        val text = "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")
        Assertions.assertEquals(LxmBitList.Empty, stackValue, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test groups`() {
        val bits = "0110 1101"
        val text = "${BitlistNode.binaryPrefix}${BitlistNode.startToken}$bits${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")

        val resultValue = BitSet()
        resultValue[1] = true
        resultValue[2] = true
        resultValue[4] = true
        resultValue[5] = true
        resultValue[7] = true
        Assertions.assertEquals(bits.replace(" ", "").length, stackValue.primitive.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive.content,
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
    
    @Test
    @Incorrect
    fun `test incorrect expression value`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncompatibleType) {
            val text =
                    "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${EscapedExpressionNode.startToken}1234${EscapedExpressionNode.endToken}${BitlistNode.endToken}"
            TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)
        }
    }
}
