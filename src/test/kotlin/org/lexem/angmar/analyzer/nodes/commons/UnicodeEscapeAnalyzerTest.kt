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

        // Prepare stack
        analyzer.memory.pushStack(LxmString.Empty)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("C", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test complex`() {
        val text =
                "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}20046${UnicodeEscapeNode.endBracket}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeEscapeNode.Companion::parse)

        // Prepare stack
        analyzer.memory.pushStack(LxmString.Empty)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("\uD840\uDC46", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
