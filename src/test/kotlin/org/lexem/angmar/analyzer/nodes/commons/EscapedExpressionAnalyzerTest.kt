package org.lexem.angmar.analyzer.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*

internal class EscapedExpressionAnalyzerTest {
    @Test
    fun test() {
        val text = "${EscapedExpressionNode.startToken}345${EscapedExpressionNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = EscapedExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(345, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
