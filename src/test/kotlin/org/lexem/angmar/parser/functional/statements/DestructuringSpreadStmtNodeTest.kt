package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class DestructuringSpreadStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${DestructuringSpreadStmtNode.spreadToken}${DestructuringSpreadStmtNode.constantToken}${IdentifierNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectDestructuringSpreadStmt(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of("${DestructuringSpreadStmtNode.spreadToken}${IdentifierNodeTest.testExpression}",
                        false))
                yield(Arguments.of(
                        "${DestructuringSpreadStmtNode.spreadToken}${DestructuringSpreadStmtNode.constantToken}${IdentifierNodeTest.testExpression}",
                        true))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is DestructuringSpreadStmtNode, "The node is not a DestructuringSpreadStmtNode")
            node as DestructuringSpreadStmtNode

            Assertions.assertTrue(node.isConstant, "The isConstant property is incorrect")
            IdentifierNodeTest.checkTestExpression(node.identifier)

        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectDestructuringSpreadStmt")
    fun `parse correct destructuring spread statement`(text: String, isConstant: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = DestructuringSpreadStmtNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as DestructuringSpreadStmtNode

        Assertions.assertEquals(isConstant, res.isConstant, "The isConstant property is incorrect")
        IdentifierNodeTest.checkTestExpression(res.identifier)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect destructuring spread statement without identifier`() {
        assertParserException {
            val text = "${DestructuringSpreadStmtNode.spreadToken}${DestructuringSpreadStmtNode.constantToken}"
            val parser = LexemParser(CustomStringReader.from(text))
            DestructuringSpreadStmtNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = DestructuringSpreadStmtNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

