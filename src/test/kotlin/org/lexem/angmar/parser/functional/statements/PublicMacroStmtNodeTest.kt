package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*

internal class PublicMacroStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${PublicMacroStmtNode.macroName} ${StatementCommonsTest.testAnyPublicMacroStmt}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is PublicMacroStmtNode, "The node is not a PublicMacroStmtNode")
            node as PublicMacroStmtNode

            StatementCommonsTest.checkTestAnyPublicMacroStmt(node.element)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["${PublicMacroStmtNode.macroName} ${StatementCommonsTest.testAnyPublicMacroStmt}"])
    fun `parse correct public macro statement`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PublicMacroStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PublicMacroStmtNode

        StatementCommonsTest.checkTestAnyPublicMacroStmt(res.element)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect public macro without element`() {
        TestUtils.assertParserException(AngmarParserExceptionType.PublicMacroStatementWithoutValidStatement) {
            val text = PublicMacroStmtNode.macroName
            val parser = LexemParser(IOStringReader.from(text))
            PublicMacroStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PublicMacroStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
