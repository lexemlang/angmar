package org.lexem.angmar.analyzer.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*

internal class QuotedIdentifierAnalyzerTest {
    @Test
    fun `test without escapes`() {
        val text = "${QuotedIdentifierNode.startQuote}43${QuotedIdentifierNode.endQuote}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuotedIdentifierNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("43", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test with escapes`() {
        val text =
                "${QuotedIdentifierNode.startQuote}${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}20046${UnicodeEscapeNode.endBracket}${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}43${UnicodeEscapeNode.endBracket}abc${QuotedIdentifierNode.endQuote}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuotedIdentifierNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("\uD840\uDC46Cabc", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}

