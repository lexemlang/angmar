package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class StringAnalyzerTest {
    @Test
    fun `test without escapes`() {
        val text = "${StringNode.startToken}43${StringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = StringNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("43", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test with escapes`() {
        val text =
                "${StringNode.startToken}${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}20046${UnicodeEscapeNode.endBracket}${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}43${UnicodeEscapeNode.endBracket}abc${StringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = StringNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("\uD840\uDC46Cabc", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}

