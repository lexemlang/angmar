package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.lexem.angmar.Incorrect
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader
import org.lexem.angmar.parser.GlobalCommonsTest
import org.lexem.angmar.parser.ParserNode
import org.lexem.angmar.parser.functional.expressions.ExpressionsCommonsTest
import org.lexem.angmar.utils.assertParserException
import java.util.stream.Stream
import kotlin.streams.asStream

internal class ConditionalStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
            "${ConditionalStmtNode.ifKeyword} ${ExpressionsCommonsTest.testExpression} ${GlobalCommonsTest.testBlock}"

        @JvmStatic
        private fun provideCorrectConditionalStmt(): Stream<Arguments> {
            val sequence = sequence {
                for (isUnless in 0..1) {
                    val keyword = if (isUnless == 0) {
                        ConditionalStmtNode.ifKeyword
                    } else {
                        ConditionalStmtNode.unlessKeyword
                    }

                    for (hasElse in 0..1) {
                        for (isIfElse in 0..1) {
                            val elseBlock = if (isIfElse == 0) {
                                GlobalCommonsTest.testBlock
                            } else {
                                testExpression
                            }

                            var text =
                                "$keyword ${ExpressionsCommonsTest.testExpression} ${GlobalCommonsTest.testBlock}"

                            if (hasElse == 1) {
                                text += " ${ConditionalStmtNode.elseKeyword} $elseBlock"
                            }

                            yield(Arguments.of(text, isUnless == 1, hasElse == 1, isIfElse == 1))

                            // Without whitespaces
                            text = "$keyword ${ExpressionsCommonsTest.testExpression}${GlobalCommonsTest.testBlock}"

                            if (hasElse == 1) {
                                val elseBlockText = if (isIfElse == 1) {
                                    " $elseBlock"
                                } else {
                                    elseBlock
                                }

                                text += "${ConditionalStmtNode.elseKeyword}$elseBlockText"
                            }

                            yield(Arguments.of(text, isUnless == 1, hasElse == 1, isIfElse == 1))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ConditionalStmtNode, "The node is not a ConditionalStmtNode")
            node as ConditionalStmtNode

            Assertions.assertFalse(node.isUnless, "The isUnless property is incorrect")
            ExpressionsCommonsTest.checkTestExpression(node.condition)
            GlobalCommonsTest.checkTestBlock(node.thenBlock)

            Assertions.assertNull(node.elseBlock, "The elseBlock property must be null")
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectConditionalStmt")
    fun `parse correct conditional statement`(text: String, isUnless: Boolean, hasElse: Boolean, isIfElse: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ConditionalStmtNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ConditionalStmtNode

        Assertions.assertEquals(isUnless, res.isUnless, "The isUnless property is incorrect")
        ExpressionsCommonsTest.checkTestExpression(res.condition)
        GlobalCommonsTest.checkTestBlock(res.thenBlock)

        if (hasElse) {
            Assertions.assertNotNull(res.elseBlock, "The elseBlock property cannot be null")

            if (isIfElse) {
                checkTestExpression(res.elseBlock!!)
            } else {
                GlobalCommonsTest.checkTestBlock(res.elseBlock!!)
            }
        } else {
            Assertions.assertNull(res.elseBlock, "The elseBlock property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect conditional statement without condition`() {
        assertParserException {
            val text = ConditionalStmtNode.ifKeyword
            val parser = LexemParser(CustomStringReader.from(text))
            ConditionalStmtNode.parse(parser)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect conditional statement without thenBlock`() {
        assertParserException {
            val text = "${ConditionalStmtNode.ifKeyword} ${ExpressionsCommonsTest.testExpression}"
            val parser = LexemParser(CustomStringReader.from(text))
            ConditionalStmtNode.parse(parser)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect conditional statement with else but without elseBlock`() {
        assertParserException {
            val text =
                "${ConditionalStmtNode.ifKeyword} ${ExpressionsCommonsTest.testExpression} ${GlobalCommonsTest.testBlock} ${ConditionalStmtNode.elseKeyword}"
            val parser = LexemParser(CustomStringReader.from(text))
            ConditionalStmtNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ConditionalStmtNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
