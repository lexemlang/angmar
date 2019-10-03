package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class NilAnalyzerTest {
    @Test
    fun `test false`() {
        val text = NilNode.nilLiteral
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = NilNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmNil, analyzer.memory.popStack(), "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
