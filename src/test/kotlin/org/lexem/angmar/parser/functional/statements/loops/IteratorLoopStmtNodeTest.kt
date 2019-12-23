package org.lexem.angmar.parser.functional.statements.loops

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class IteratorLoopStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${IteratorLoopStmtNode.keyword} ${DestructuringStmtNodeTest.testExpression} ${IteratorLoopStmtNode.relationKeyword} ${ExpressionsCommonsTest.testExpression} ${BlockStmtNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectIteratorStmt(): Stream<Arguments> {
            val sequence = sequence {
                for (isDestructuring in listOf(false, true)) {
                    val variable = if (!isDestructuring) {
                        CommonsTest.testDynamicIdentifier
                    } else {
                        DestructuringStmtNodeTest.testExpression
                    }

                    for (hasClauses in listOf(false, true)) {
                        for (hasIndex in listOf(false, true)) {
                            var text =
                                    "${IteratorLoopStmtNode.keyword} $variable ${IteratorLoopStmtNode.relationKeyword} ${ExpressionsCommonsTest.testExpression} ${BlockStmtNodeTest.testExpression}"

                            if (hasIndex) {
                                text = "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression} $text"
                            }

                            if (hasClauses) {
                                text += " ${LoopClausesStmtNodeTest.testExpression}"
                            }

                            yield(Arguments.of(text, isDestructuring, hasClauses, hasIndex))

                            // Without whitespaces
                            text =
                                    "${IteratorLoopStmtNode.keyword} $variable ${IteratorLoopStmtNode.relationKeyword} ${ExpressionsCommonsTest.testExpression}${BlockStmtNodeTest.testExpression}"

                            if (hasIndex) {
                                text = "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression} $text"
                            }

                            if (hasClauses) {
                                text += LoopClausesStmtNodeTest.testExpression
                            }

                            yield(Arguments.of(text, isDestructuring, hasClauses, hasIndex))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is IteratorLoopStmtNode, "The node is not a IteratorLoopStmtNode")
            node as IteratorLoopStmtNode

            DestructuringStmtNodeTest.checkTestExpression(node.variable)
            Assertions.assertNull(node.index, "The index property must be null")
            Assertions.assertNull(node.lastClauses, "The lastClauses property must be null")
            ExpressionsCommonsTest.checkTestExpression(node.condition)
            BlockStmtNodeTest.checkTestExpression(node.thenBlock)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectIteratorStmt")
    fun `parse correct iterator loop statement`(text: String, isDestructuring: Boolean, hasClauses: Boolean,
            hasIndex: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = IteratorLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as IteratorLoopStmtNode

        if (isDestructuring) {
            DestructuringStmtNodeTest.checkTestExpression(res.variable)
        } else {
            CommonsTest.checkTestDynamicIdentifier(res.variable)
        }

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

        ExpressionsCommonsTest.checkTestExpression(res.condition)
        BlockStmtNodeTest.checkTestExpression(res.thenBlock)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect iterator loop statement without variable`() {
        TestUtils.assertParserException(AngmarParserExceptionType.IteratorLoopStatementWithoutIdentifier) {
            val text = IteratorLoopStmtNode.keyword
            val parser = LexemParser(IOStringReader.from(text))
            IteratorLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect iterator loop statement without relationalKeyword`() {
        TestUtils.assertParserException(AngmarParserExceptionType.IteratorLoopStatementWithoutRelationalKeyword) {
            val text = "${IteratorLoopStmtNode.keyword} ${DestructuringStmtNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            IteratorLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect iterator loop statement without condition`() {
        TestUtils.assertParserException(
                AngmarParserExceptionType.IteratorLoopStatementWithoutExpressionAfterRelationalKeyword) {
            val text =
                    "${IteratorLoopStmtNode.keyword} ${DestructuringStmtNodeTest.testExpression} ${IteratorLoopStmtNode.relationKeyword}"
            val parser = LexemParser(IOStringReader.from(text))
            IteratorLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect iterator loop statement without thenBlock`() {
        TestUtils.assertParserException(AngmarParserExceptionType.IteratorLoopStatementWithoutBlock) {
            val text =
                    "${IteratorLoopStmtNode.keyword} ${DestructuringStmtNodeTest.testExpression} ${IteratorLoopStmtNode.relationKeyword} ${ExpressionsCommonsTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            IteratorLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression}"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = IteratorLoopStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
