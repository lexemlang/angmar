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

internal class BlockStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${BlockStmtNode.startToken}${StatementCommonsTest.testAnyStatement}${BlockStmtNode.endToken}"

        @JvmStatic
        private fun provideCorrectStatements(): Stream<Arguments> {
            val result = sequence {
                yield(Arguments.of("", 0))
                yield(Arguments.of("   ", 0))

                for (stmt in StatementCommonsTest.statements) {
                    yield(Arguments.of(stmt, 1))
                }

                for (eol in WhitespaceNode.endOfLineChars) {
                    yield(Arguments.of(StatementCommonsTest.statements.joinToString("$eol"),
                            StatementCommonsTest.statements.size))
                }

                yield(Arguments.of(StatementCommonsTest.statements.joinToString(WhitespaceNode.windowsEndOfLine),
                        StatementCommonsTest.statements.size))
            }

            return result.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is BlockStmtNode, "The node is not a BlockStmtNode")
            node as BlockStmtNode

            Assertions.assertNull(node.tag, "The tag property must be null")
            Assertions.assertEquals(1, node.statements.size, "The number of statements is incorrect")
            StatementCommonsTest.checkTestAnyStatement(node.statements.first())
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectStatements")
    fun `parse correct block`(statements: String, stmtNum: Int) {
        val text = "${BlockStmtNode.startToken}$statements${BlockStmtNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = BlockStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as BlockStmtNode

        Assertions.assertNull(res.tag, "The tag property must be null")
        Assertions.assertEquals(stmtNum, res.statements.size, "The number of statements is incorrect")

        for (stmt in res.statements.withIndex()) {
            Assertions.assertNotNull(stmt.value, "The statement[${stmt.index}] cannot be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectStatements")
    fun `parse correct block with tag`(statements: String, stmtNum: Int) {
        val text =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}${IdentifierNodeTest.testExpression}\n$statements${BlockStmtNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = BlockStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as BlockStmtNode

        Assertions.assertNotNull(res.tag, "The tag property cannot be null")
        IdentifierNodeTest.checkTestExpression(res.tag!!)
        Assertions.assertEquals(stmtNum, res.statements.size, "The number of statements is incorrect")

        for (stmt in res.statements.withIndex()) {
            Assertions.assertNotNull(stmt.value, "The statement[${stmt.index}] cannot be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @MethodSource("provideCorrectStatements")
    fun `parse incorrect block without endToken`(statements: String) {
        TestUtils.assertParserException {
            val text = "${BlockStmtNode.startToken}$statements"
            val parser = LexemParser(CustomStringReader.from(text))
            BlockStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = BlockStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
