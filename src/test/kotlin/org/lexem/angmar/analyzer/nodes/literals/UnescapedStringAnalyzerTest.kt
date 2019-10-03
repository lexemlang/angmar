package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class UnescapedStringAnalyzerTest {
    @Test
    fun test() {
        val text = "${UnescapedStringNode.macroName}${UnescapedStringNode.startToken}43${UnescapedStringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = UnescapedStringNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals("43", result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
