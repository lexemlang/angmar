package org.lexem.angmar.parser.functional.statements.loops

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LoopClausesStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${LoopClausesStmtNode.lastKeyword} ${BlockStmtNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectLoopClauses(): Stream<Arguments> {
            val sequence = sequence {
                for (hasElse in listOf(false, true)) {
                    for (hasLast in listOf(false, true)) {
                        // Avoid none of them
                        if (!hasElse && !hasLast) {
                            continue
                        }

                        val list = mutableListOf<String>()

                        if (hasLast) {
                            list += "${LoopClausesStmtNode.lastKeyword} ${BlockStmtNodeTest.testExpression}"
                        }

                        if (hasElse) {
                            list += "${LoopClausesStmtNode.elseKeyword} ${BlockStmtNodeTest.testExpression}"
                        }

                        yield(Arguments.of(list.joinToString(" "), hasLast, hasElse))

                        // Without whitespaces
                        list.clear()

                        if (hasLast) {
                            list += "${LoopClausesStmtNode.lastKeyword}${BlockStmtNodeTest.testExpression}"
                        }

                        if (hasElse) {
                            list += "${LoopClausesStmtNode.elseKeyword}${BlockStmtNodeTest.testExpression}"
                        }

                        yield(Arguments.of(list.joinToString(""), hasLast, hasElse))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is LoopClausesStmtNode, "The node is not a LoopClausesStmtNode")
            node as LoopClausesStmtNode

            Assertions.assertNotNull(node.lastBlock, "The lastBlock property cannot be null")
            BlockStmtNodeTest.checkTestExpression(node.lastBlock!!)
            Assertions.assertNull(node.elseBlock, "The elseBlock property must be null")
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectLoopClauses")
    fun `parse correct loop clauses`(text: String, hasLast: Boolean, hasElse: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = LoopClausesStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LoopClausesStmtNode

        if (hasLast) {
            Assertions.assertNotNull(res.lastBlock, "The lastBlock property cannot be null")
            BlockStmtNodeTest.checkTestExpression(res.lastBlock!!)
        } else {
            Assertions.assertNull(res.lastBlock, "The lastBlock property must be null")
        }

        if (hasElse) {
            Assertions.assertNotNull(res.elseBlock, "The elseBlock property cannot be null")
            BlockStmtNodeTest.checkTestExpression(res.elseBlock!!)
        } else {
            Assertions.assertNull(res.elseBlock, "The elseBlock property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect loop clauses with last but without block`() {
        TestUtils.assertParserException(AngmarParserExceptionType.LastLoopClauseWithoutBlock) {
            val text = LoopClausesStmtNode.lastKeyword
            val parser = LexemParser(IOStringReader.from(text))
            LoopClausesStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect loop clauses with else but without block`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ElseLoopClauseWithoutBlock) {
            val text = LoopClausesStmtNode.elseKeyword
            val parser = LexemParser(IOStringReader.from(text))
            LoopClausesStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = LoopClausesStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
