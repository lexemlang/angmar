package org.lexem.angmar.parser.functional.expressions

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.binary.*

internal class RightExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = ConditionalExpressionNodeTest.testExpression

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) =
                ConditionalExpressionNodeTest.checkTestExpression((node as RightExpressionNode).expression)
    }


    // TESTS ------------------------------------------------------------------

    @Test
    fun `parse correct right expression`() {
        val text = ConditionalExpressionNodeTest.testExpression
        val parser = LexemParser(IOStringReader.from(text))
        val res = RightExpressionNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        ConditionalExpressionNodeTest.checkTestExpression(res!!.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }
}
