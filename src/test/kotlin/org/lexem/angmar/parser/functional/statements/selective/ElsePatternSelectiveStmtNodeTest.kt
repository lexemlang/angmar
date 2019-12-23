package org.lexem.angmar.parser.functional.statements.selective

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*

internal class ElsePatternSelectiveStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = ElsePatternSelectiveStmtNode.elseKeyword

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ElsePatternSelectiveStmtNode,
                    "The node is not a ElsePatternSelectiveStmtNode")
            node as ElsePatternSelectiveStmtNode
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [ElsePatternSelectiveStmtNode.elseKeyword])
    fun `parse correct else pattern selective statement`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ElsePatternSelectiveStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ElsePatternSelectiveStmtNode

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ElsePatternSelectiveStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
