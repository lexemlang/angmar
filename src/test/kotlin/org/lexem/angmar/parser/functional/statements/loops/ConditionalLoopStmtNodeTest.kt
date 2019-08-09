package org.lexem.angmar.parser.functional.statements.loops

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class ConditionalLoopStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ConditionalLoopStmtNode.whileKeyword} ${ExpressionsCommonsTest.testExpression} ${GlobalCommonsTest.testBlock}"

        @JvmStatic
        private fun provideCorrectConditionalStmt(): Stream<Arguments> {
            val sequence = sequence {
                for (isUntil in 0..1) {
                    val keyword = if (isUntil == 0) {
                        ConditionalLoopStmtNode.whileKeyword
                    } else {
                        ConditionalLoopStmtNode.untilKeyword
                    }

                    for (hasClauses in 0..1) {
                        for (hasIndex in 0..1) {
                            var text =
                                    "$keyword ${ExpressionsCommonsTest.testExpression} ${GlobalCommonsTest.testBlock}"

                            if (hasIndex == 1) {
                                text = "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression} $text"
                            }

                            if (hasClauses == 1) {
                                text += " ${LoopClausesStmtNodeTest.testExpression}"
                            }

                            yield(Arguments.of(text, isUntil == 1, hasClauses == 1, hasIndex == 1))

                            // Without whitespaces
                            text = "$keyword ${ExpressionsCommonsTest.testExpression}${GlobalCommonsTest.testBlock}"

                            if (hasIndex == 1) {
                                text = "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression} $text"
                            }

                            if (hasClauses == 1) {
                                text += LoopClausesStmtNodeTest.testExpression
                            }

                            yield(Arguments.of(text, isUntil == 1, hasClauses == 1, hasIndex == 1))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ConditionalLoopStmtNode, "The node is not a ConditionalLoopStmtNode")
            node as ConditionalLoopStmtNode

            Assertions.assertFalse(node.isUntil, "The isUntil property is incorrect")
            Assertions.assertNull(node.index, "The index property must be null")
            Assertions.assertNull(node.lastClauses, "The lastClauses property must be null")
            ExpressionsCommonsTest.checkTestExpression(node.condition)
            GlobalCommonsTest.checkTestBlock(node.thenBlock)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectConditionalStmt")
    fun `parse correct conditional loop statement`(text: String, isUntil: Boolean, hasClauses: Boolean,
            hasIndex: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ConditionalLoopStmtNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ConditionalLoopStmtNode

        Assertions.assertEquals(isUntil, res.isUntil, "The isUntil property is incorrect")

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
        GlobalCommonsTest.checkTestBlock(res.thenBlock)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect conditional loop statement without condition`() {
        assertParserException {
            val text = ConditionalLoopStmtNode.whileKeyword
            val parser = LexemParser(CustomStringReader.from(text))
            ConditionalLoopStmtNode.parse(parser)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect conditional loop statement without thenBlock`() {
        assertParserException {
            val text = "${ConditionalLoopStmtNode.whileKeyword} ${ExpressionsCommonsTest.testExpression}"
            val parser = LexemParser(CustomStringReader.from(text))
            ConditionalLoopStmtNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "${InfiniteLoopStmtNode.keyword} ${IdentifierNodeTest.testExpression}"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ConditionalLoopStmtNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
