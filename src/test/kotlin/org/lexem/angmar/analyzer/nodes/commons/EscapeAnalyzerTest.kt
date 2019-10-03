package org.lexem.angmar.analyzer.nodes.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*

internal class EscapeAnalyzerTest {
    private val prefix = "-test-"

    @Test
    fun `test t`() {
        val text = "${EscapeNode.startToken}t"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = EscapeNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmString.from(prefix))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("$prefix\t", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test n`() {
        val text = "${EscapeNode.startToken}n"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = EscapeNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmString.from(prefix))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("$prefix\n", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test r`() {
        val text = "${EscapeNode.startToken}r"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = EscapeNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmString.from(prefix))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("$prefix\r", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }


    @ParameterizedTest
    @ValueSource(strings = [" ", "g", ",", "\n"])
    fun `test same character`(char: String) {
        val text = "${EscapeNode.startToken}$char"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = EscapeNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmString.from(prefix))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("$prefix$char", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
