package org.lexem.angmar.parser.functional.statements.loops

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LoopClausesStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${LoopClausesStmtNode.lastKeyword} ${GlobalCommonsTest.testBlock}"

        @JvmStatic
        private fun provideCorrectLoopClauses(): Stream<Arguments> {
            val sequence = sequence {
                for (hasElse in 0..1) {
                    for (hasLast in 0..1) {
                        // Avoid none of them
                        if (hasElse == 0 && hasLast == 0) {
                            continue
                        }

                        val list = mutableListOf<String>()

                        if (hasLast == 1) {
                            list += "${LoopClausesStmtNode.lastKeyword} ${GlobalCommonsTest.testBlock}"
                        }

                        if (hasElse == 1) {
                            list += "${LoopClausesStmtNode.elseKeyword} ${GlobalCommonsTest.testBlock}"
                        }

                        yield(Arguments.of(list.joinToString(" "), hasLast == 1, hasElse == 1))

                        // Without whitespaces
                        list.clear()

                        if (hasLast == 1) {
                            list += "${LoopClausesStmtNode.lastKeyword}${GlobalCommonsTest.testBlock}"
                        }

                        if (hasElse == 1) {
                            list += "${LoopClausesStmtNode.elseKeyword}${GlobalCommonsTest.testBlock}"
                        }

                        yield(Arguments.of(list.joinToString(""), hasLast == 1, hasElse == 1))
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
            GlobalCommonsTest.checkTestBlock(node.lastBlock!!)
            Assertions.assertNull(node.elseBlock, "The elseBlock property must be null")
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectLoopClauses")
    fun `parse correct loop clauses`(text: String, hasLast: Boolean, hasElse: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = LoopClausesStmtNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LoopClausesStmtNode

        if (hasLast) {
            Assertions.assertNotNull(res.lastBlock, "The lastBlock property cannot be null")
            GlobalCommonsTest.checkTestBlock(res.lastBlock!!)
        } else {
            Assertions.assertNull(res.lastBlock, "The lastBlock property must be null")
        }

        if (hasElse) {
            Assertions.assertNotNull(res.elseBlock, "The elseBlock property cannot be null")
            GlobalCommonsTest.checkTestBlock(res.elseBlock!!)
        } else {
            Assertions.assertNull(res.elseBlock, "The elseBlock property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect loop clauses with last but without block`() {
        assertParserException {
            val text = LoopClausesStmtNode.lastKeyword
            val parser = LexemParser(CustomStringReader.from(text))
            LoopClausesStmtNode.parse(parser)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect loop clauses with else but without block`() {
        assertParserException {
            val text = LoopClausesStmtNode.elseKeyword
            val parser = LexemParser(CustomStringReader.from(text))
            LoopClausesStmtNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = LoopClausesStmtNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
