package org.lexem.angmar.analyzer.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*

internal class IdentifierAnalyzerTest {
    @Test
    fun `test simple`() {
        val text = "abc"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IdentifierNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("abc", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test quoted`() {
        val text = "${QuotedIdentifierNode.startQuote}43${QuotedIdentifierNode.endQuote}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IdentifierNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("43", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
