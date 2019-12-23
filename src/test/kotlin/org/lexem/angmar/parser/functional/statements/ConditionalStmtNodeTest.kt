package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class ConditionalStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ConditionalStmtNode.ifKeyword} ${ExpressionsCommonsTest.testExpression} ${BlockStmtNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectConditionalStmt(): Stream<Arguments> {
            val sequence = sequence {
                for (isUnless in listOf(false, true)) {
                    val keyword = if (!isUnless) {
                        ConditionalStmtNode.ifKeyword
                    } else {
                        ConditionalStmtNode.unlessKeyword
                    }

                    for (hasElse in listOf(false, true)) {
                        for (isIfElse in listOf(false, true)) {
                            val elseBlock = if (!isIfElse) {
                                BlockStmtNodeTest.testExpression
                            } else {
                                testExpression
                            }

                            var text =
                                    "$keyword ${ExpressionsCommonsTest.testExpression} ${BlockStmtNodeTest.testExpression}"

                            if (hasElse) {
                                text += " ${ConditionalStmtNode.elseKeyword} $elseBlock"
                            }

                            yield(Arguments.of(text, isUnless, hasElse, isIfElse))

                            // Without whitespaces
                            text =
                                    "$keyword ${ExpressionsCommonsTest.testExpression}${BlockStmtNodeTest.testExpression}"

                            if (hasElse) {
                                val elseBlockText = if (isIfElse) {
                                    " $elseBlock"
                                } else {
                                    elseBlock
                                }

                                text += "${ConditionalStmtNode.elseKeyword}$elseBlockText"
                            }

                            yield(Arguments.of(text, isUnless, hasElse, isIfElse))
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
            BlockStmtNodeTest.checkTestExpression(node.thenBlock)

            Assertions.assertNull(node.elseBlock, "The elseBlock property must be null")
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectConditionalStmt")
    fun `parse correct conditional statement`(text: String, isUnless: Boolean, hasElse: Boolean, isIfElse: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ConditionalStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ConditionalStmtNode

        Assertions.assertEquals(isUnless, res.isUnless, "The isUnless property is incorrect")
        ExpressionsCommonsTest.checkTestExpression(res.condition)
        BlockStmtNodeTest.checkTestExpression(res.thenBlock)

        if (hasElse) {
            Assertions.assertNotNull(res.elseBlock, "The elseBlock property cannot be null")

            if (isIfElse) {
                checkTestExpression(res.elseBlock!!)
            } else {
                BlockStmtNodeTest.checkTestExpression(res.elseBlock!!)
            }
        } else {
            Assertions.assertNull(res.elseBlock, "The elseBlock property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect conditional statement without condition`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ConditionalStatementWithoutCondition) {
            val text = ConditionalStmtNode.ifKeyword
            val parser = LexemParser(IOStringReader.from(text))
            ConditionalStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect conditional statement without thenBlock`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ConditionalStatementWithoutThenBlock) {
            val text = "${ConditionalStmtNode.ifKeyword} ${ExpressionsCommonsTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            ConditionalStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect conditional statement with else but without elseBlock`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ConditionalStatementWithoutElseBlock) {
            val text =
                    "${ConditionalStmtNode.ifKeyword} ${ExpressionsCommonsTest.testExpression} ${BlockStmtNodeTest.testExpression} ${ConditionalStmtNode.elseKeyword}"
            val parser = LexemParser(IOStringReader.from(text))
            ConditionalStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ConditionalStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
