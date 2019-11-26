package org.lexem.angmar.parser.functional.statements.selective

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

internal class SelectiveCaseStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ConditionalPatternSelectiveStmtNodeTest.testExpression} ${BlockStmtNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectCaseStatement(): Stream<Arguments> {
            val sequence = sequence {
                for (pattern1Type in 1..4) {
                    val pattern1 = when (pattern1Type) {
                        1 -> ConditionalPatternSelectiveStmtNodeTest.testExpression
                        2 -> ElsePatternSelectiveStmtNodeTest.testExpression
                        3 -> VarPatternSelectiveStmtNodeTest.testExpression
                        4 -> ExpressionPatternSelectiveStmtNodeTest.testExpression
                        else -> null
                    }

                    for (pattern2Type in 0..4) {
                        val patternList = mutableListOf(pattern1)

                        when (pattern2Type) {
                            1 -> patternList += ConditionalPatternSelectiveStmtNodeTest.testExpression
                            2 -> patternList += ElsePatternSelectiveStmtNodeTest.testExpression
                            3 -> patternList += VarPatternSelectiveStmtNodeTest.testExpression
                            4 -> patternList += ExpressionPatternSelectiveStmtNodeTest.testExpression
                        }

                        var text = patternList.joinToString(" ${SelectiveCaseStmtNode.patternSeparator} ")
                        text += " ${BlockStmtNodeTest.testExpression}"

                        yield(Arguments.of(text, pattern1Type, pattern2Type))

                        // Without whitespace
                        text = patternList.joinToString(SelectiveCaseStmtNode.patternSeparator)
                        text += BlockStmtNodeTest.testExpression

                        yield(Arguments.of(text, pattern1Type, pattern2Type))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is SelectiveCaseStmtNode, "The node is not a SelectiveCaseStmtNode")
            node as SelectiveCaseStmtNode

            Assertions.assertEquals(1, node.patterns.size, "The number of patterns is incorrect")
            ConditionalPatternSelectiveStmtNodeTest.checkTestExpression(node.patterns.first())
            BlockStmtNodeTest.checkTestExpression(node.block)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectCaseStatement")
    fun `parse correct case statement`(text: String, pattern1Type: Int, pattern2Type: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = SelectiveCaseStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as SelectiveCaseStmtNode

        val patternLength = if (pattern2Type == 0) {
            1
        } else {
            2
        }

        Assertions.assertEquals(patternLength, res.patterns.size, "The number of pattern is incorrect")

        when (pattern1Type) {
            1 -> ConditionalPatternSelectiveStmtNodeTest.checkTestExpression(res.patterns.first())
            2 -> ElsePatternSelectiveStmtNodeTest.checkTestExpression(res.patterns.first())
            3 -> VarPatternSelectiveStmtNodeTest.checkTestExpression(res.patterns.first())
            4 -> ExpressionPatternSelectiveStmtNodeTest.checkTestExpression(res.patterns.first())
        }

        when (pattern2Type) {
            1 -> ConditionalPatternSelectiveStmtNodeTest.checkTestExpression(res.patterns.last())
            2 -> ElsePatternSelectiveStmtNodeTest.checkTestExpression(res.patterns.last())
            3 -> VarPatternSelectiveStmtNodeTest.checkTestExpression(res.patterns.last())
            4 -> ExpressionPatternSelectiveStmtNodeTest.checkTestExpression(res.patterns.last())
        }

        BlockStmtNodeTest.checkTestExpression(res.block)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect case statement with two patterns without the second pattern`() {
        TestUtils.assertParserException(
                AngmarParserExceptionType.SelectiveCaseStatementWithoutPatternAfterElementSeparator) {
            val text =
                    "${ConditionalPatternSelectiveStmtNodeTest.testExpression} ${SelectiveCaseStmtNode.patternSeparator}"
            val parser = LexemParser(IOStringReader.from(text))
            SelectiveCaseStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect case statement without the block`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectiveCaseStatementWithoutBlock) {
            val text = ConditionalPatternSelectiveStmtNodeTest.testExpression
            val parser = LexemParser(IOStringReader.from(text))
            SelectiveCaseStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = SelectiveCaseStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

