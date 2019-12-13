package org.lexem.angmar.analyzer.nodes.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*

internal class EscapeAnalyzerTest {
    @ParameterizedTest
    @ValueSource(chars = ['\t', '\n', '\r'])
    fun `test escaped characters`(char: Char) {
        val text = EscapeNode.startToken + when (char) {
            '\t' -> "t"
            '\n' -> "n"
            else -> "r"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = EscapeNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(char.toInt(), result.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(chars = [' ', 'g', ',', '\n'])
    fun `test same character`(char: Char) {
        val text = "${EscapeNode.startToken}$char"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = EscapeNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(char.toInt(), result.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
