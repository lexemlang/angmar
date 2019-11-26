package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*

internal class FunctionalExpressionStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = NumberNodeTest.testExpression

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is FunctionalExpressionStmtNode, "The node is not a ExpressionStmtNode")
            node as FunctionalExpressionStmtNode

            NumberNodeTest.checkTestExpression((node.expression as RightExpressionNode).expression)
        }
    }


    // TESTS ------------------------------------------------------------------


    @Test
    fun `parse correct`() {
        val text = NumberNodeTest.testExpression
        val parser = LexemParser(IOStringReader.from(text))
        val res = FunctionalExpressionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionalExpressionStmtNode

        NumberNodeTest.checkTestExpression((res.expression as RightExpressionNode).expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }
}
