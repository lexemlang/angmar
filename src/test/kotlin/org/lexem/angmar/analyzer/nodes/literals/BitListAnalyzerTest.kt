package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.data.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.*

internal class BitListAnalyzerTest {
    @Test
    fun `test radix 2`() {
        val bits = "0110"
        val escapedVariable = "escapedVar"
        val text =
                "${BitlistNode.binaryPrefix}${BitlistNode.startToken}${EscapedExpressionNode.startToken}$escapedVariable${EscapedExpressionNode.endToken}$bits${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)

        // Prepare context.
        val bitList = BitList(4, BitSet().apply { set(0); set(1); set(3) })
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, escapedVariable, LxmBitList(bitList))

        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The value of the stack must be a LxmBitList")

        val resultSet = bitList.content.clone() as BitSet
        val resultValue = BitList(8, resultSet)
        resultSet[bitList.size + 1] = true
        resultSet[bitList.size + 2] = true
        Assertions.assertEquals(resultValue.size, stackValue.primitive.size,
                "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue.content, stackValue.primitive.content,
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(escapedVariable))
    }

    @Test
    @Incorrect
    fun `test incorrect expression value`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val variableName = "testVar"
            val text =
                    "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${EscapedExpressionNode.startToken}$variableName${EscapedExpressionNode.endToken}${BitlistNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BitlistNode.Companion::parse)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            context.setProperty(analyzer.memory, variableName, LxmInteger.Num10)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
