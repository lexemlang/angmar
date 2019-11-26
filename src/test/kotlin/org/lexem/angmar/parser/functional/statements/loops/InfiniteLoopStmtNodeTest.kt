package org.lexem.angmar.parser.functional.statements.loops

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class InfiniteLoopStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${InfiniteLoopStmtNode.keyword} ${BlockStmtNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectInfiniteLoopStmt(): Stream<Arguments> {
            val sequence = sequence {
                for (hasClauses in listOf(false, true)) {
                    for (hasIndex in listOf(false, true)) {
                        var text = if (hasIndex) {
                            "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression} ${BlockStmtNodeTest.testExpression}"
                        } else {
                            "${InfiniteLoopStmtNode.keyword} ${BlockStmtNodeTest.testExpression}"
                        }

                        if (hasClauses) {
                            text += " ${LoopClausesStmtNodeTest.testExpression}"
                        }

                        yield(Arguments.of(text, hasClauses, hasIndex))

                        // Without whitespaces
                        text = if (hasIndex) {
                            "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression}${BlockStmtNodeTest.testExpression}"
                        } else {
                            "${InfiniteLoopStmtNode.keyword}${BlockStmtNodeTest.testExpression}"
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
            Assertions.assertTrue(node is InfiniteLoopStmtNode, "The node is not a InfiniteLoopStmtNode")
            node as InfiniteLoopStmtNode

            Assertions.assertNull(node.index, "The index property must be null")
            Assertions.assertNull(node.lastClauses, "The lastClauses property must be null")
            BlockStmtNodeTest.checkTestExpression(node.thenBlock)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectInfiniteLoopStmt")
    fun `parse correct infinite loop statement`(text: String, hasClauses: Boolean, hasIndex: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = InfiniteLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as InfiniteLoopStmtNode

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

        BlockStmtNodeTest.checkTestExpression(res.thenBlock)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect infinite loop statement without thenBlock`() {
        TestUtils.assertParserException(AngmarParserExceptionType.InfiniteLoopStatementWithoutBlock) {
            val text = InfiniteLoopStmtNode.keyword
            val parser = LexemParser(IOStringReader.from(text))
            InfiniteLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = InfiniteLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
