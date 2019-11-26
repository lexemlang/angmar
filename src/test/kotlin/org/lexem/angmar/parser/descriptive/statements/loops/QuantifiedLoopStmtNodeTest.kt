package org.lexem.angmar.parser.descriptive.statements.loops

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.loops.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class QuantifiedLoopStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${QuantifiedLoopStmtNode.keyword} ${QuantifierLexemeNodeTest.testExpression} ${BlockStmtNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectStatement(): Stream<Arguments> {
            val sequence = sequence {
                for (hasClauses in listOf(false, true)) {
                    for (hasIndex in listOf(false, true)) {
                        var text =
                                "${QuantifiedLoopStmtNode.keyword} ${QuantifierLexemeNodeTest.testExpression} ${BlockStmtNodeTest.testExpression}"

                        if (hasIndex) {
                            text = "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression} $text"
                        }

                        if (hasClauses) {
                            text += " ${LoopClausesStmtNodeTest.testExpression}"
                        }

                        yield(Arguments.of(text, hasClauses, hasIndex))

                        // Without whitespaces
                        text =
                                "${QuantifiedLoopStmtNode.keyword} ${QuantifierLexemeNodeTest.testExpression}${BlockStmtNodeTest.testExpression}"

                        if (hasIndex) {
                            text = "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression} $text"
                        }

                        if (hasClauses) {
                            text += LoopClausesStmtNodeTest.testExpression
                        }

                        yield(Arguments.of(text, hasClauses, hasIndex))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as QuantifiedLoopStmtNode

            Assertions.assertNull(node.index, "The index property must be null")
            Assertions.assertNull(node.lastClauses, "The lastClauses property must be null")
            QuantifierLexemeNodeTest.checkTestExpression(node.quantifier)
            BlockStmtNodeTest.checkTestExpression(node.thenBlock)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectStatement")
    fun `parse correct loop statement`(text: String, hasClauses: Boolean, hasIndex: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuantifiedLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as QuantifiedLoopStmtNode

        if (hasIndex) {
            Assertions.assertNotNull(res.index, "The index property cannot be null")
            IdentifierNodeTest.checkTestExpression(res.index!!)
        } else {
            Assertions.assertNull(res.index, "The index property must be null")
        }

        if (hasClauses) {
            Assertions.assertNotNull(res.lastClauses, "The lastClauses property cannot be null")
            LoopClausesStmtNodeTest.checkTestExpression(res.lastClauses!!)
        } else {
            Assertions.assertNull(res.lastClauses, "The lastClauses property must be null")
        }

        QuantifierLexemeNodeTest.checkTestExpression(res.quantifier)
        BlockStmtNodeTest.checkTestExpression(res.thenBlock)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect loop statement without quantifier`() {
        TestUtils.assertParserException(AngmarParserExceptionType.QuantifiedLoopStatementWithoutQuantifier) {
            val text = QuantifiedLoopStmtNode.keyword
            val parser = LexemParser(IOStringReader.from(text))
            QuantifiedLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect loop statement without thenBlock`() {
        TestUtils.assertParserException(AngmarParserExceptionType.QuantifiedLoopStatementWithoutBlock) {
            val text = "${QuantifiedLoopStmtNode.keyword} ${QuantifierLexemeNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            QuantifiedLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression}"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuantifiedLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
