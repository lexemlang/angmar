package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*


internal class NilNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = NilNode.nilLiteral

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is NilNode, "The node is not a NilNode")
            node as NilNode
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [NilNode.nilLiteral, "${NilNode.nilLiteral}-"])
    fun `parse correct nil keyword`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = NilNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as NilNode

        Assertions.assertEquals(NilNode.nilLiteral, res.toString(), "The content of the node is incorrect")
        Assertions.assertEquals(NilNode.nilLiteral.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["${NilNode.nilLiteral}able", "${NilNode.nilLiteral}-able"])
    fun `parse incorrect nil keyword`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = NilNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has not been correctly parsed")
        Assertions.assertEquals(0, parser.reader.currentPosition(),
                "The input has moved the cursor but has not capture anything")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = NilNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
