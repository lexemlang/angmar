package org.lexem.angmar.parser.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*

internal class IndexerNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${IndexerNode.startToken}${ExpressionsCommonsTest.testExpression}${IndexerNode.endToken}"


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is IndexerNode, "The node is not a IndexerNode")
            node as IndexerNode

            Assertions.assertNotNull(node.expression, "The expression property cannot be null")
            ExpressionsCommonsTest.checkTestExpression(node.expression)
        }
    }

    // AUX METHODS --------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse correct indexer`(expression: String) {
        val text = "${IndexerNode.startToken}$expression${IndexerNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = IndexerNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as IndexerNode

        ExpressionsCommonsTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse correct indexer with whites`(expression: String) {
        val text = "${IndexerNode.startToken}  $expression  ${IndexerNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = IndexerNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as IndexerNode

        ExpressionsCommonsTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect indexer with no expression`() {
        assertParserException {
            val text = IndexerNode.startToken
            val parser = LexemParser(CustomStringReader.from(text))
            IndexerNode.parse(parser)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse incorrect indexer with no endToken`(expression: String) {
        assertParserException {
            val text = "${IndexerNode.startToken}$expression"
            val parser = LexemParser(CustomStringReader.from(text))
            IndexerNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = IndexerNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
