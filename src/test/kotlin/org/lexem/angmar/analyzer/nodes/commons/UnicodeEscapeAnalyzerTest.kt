package org.lexem.angmar.analyzer.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*

internal class UnicodeEscapeAnalyzerTest {
    @Test
    fun `test simple`() {
        val text = "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}43${UnicodeEscapeNode.endBracket}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeEscapeNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(0x43, result.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test complex`() {
        val text =
                "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}20046${UnicodeEscapeNode.endBracket}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeEscapeNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(0x20046, result.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
