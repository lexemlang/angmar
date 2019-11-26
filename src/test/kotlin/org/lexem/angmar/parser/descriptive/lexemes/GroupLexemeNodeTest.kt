package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class GroupLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${GroupLexemeNode.startToken}${LexemPatternContentNodeTest.testExpression}${GroupLexemeNode.endToken}"

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegated in listOf(false, true)) {
                    for (hasHeader in listOf(false, true)) {
                        for (patternCount in 1..3) {
                            val list = List(patternCount) { LexemPatternContentNodeTest.testExpression }
                            var text = list.joinToString(GroupLexemeNode.patternToken)

                            if (hasHeader) {
                                text =
                                        "${GroupHeaderLexemeNodeTest.testExpression}${GroupLexemeNode.headerRelationalToken}$text"
                            }

                            text = "${GroupLexemeNode.startToken}$text${GroupLexemeNode.endToken}"

                            if (isNegated) {
                                text = "${GroupLexemeNode.notOperator}$text"
                            }

                            yield(Arguments.of(text, isNegated, hasHeader, patternCount))

                            // With whitespaces
                            text = list.joinToString(" ${GroupLexemeNode.patternToken} ")

                            if (hasHeader) {
                                text =
                                        "${GroupHeaderLexemeNodeTest.testExpression} ${GroupLexemeNode.headerRelationalToken} $text"
                            }

                            text = "${GroupLexemeNode.startToken} $text ${GroupLexemeNode.endToken}"

                            if (isNegated) {
                                text = "${GroupLexemeNode.notOperator}$text"
                            }

                            yield(Arguments.of(text, isNegated, hasHeader, patternCount))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as GroupLexemeNode

            Assertions.assertFalse(node.isNegated, "The isNegated property is incorrect")
            Assertions.assertNull(node.header, "The header property must be null")
            Assertions.assertEquals(1, node.patterns.size, "The pattern count is incorrect")

            node.patterns.forEach {
                LexemPatternContentNodeTest.checkTestExpression(it)
            }
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct node`(text: String, isNegated: Boolean, hasHeader: Boolean, patternCount: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = GroupLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as GroupLexemeNode

        Assertions.assertEquals(isNegated, res.isNegated, "The isNegated property is incorrect")

        if (hasHeader) {
            Assertions.assertNotNull(res.header, "The header property cannot be null")
            GroupHeaderLexemeNodeTest.checkTestExpression(res.header!!)
        } else {
            Assertions.assertNull(res.header, "The header property must be null")
        }

        Assertions.assertEquals(patternCount, res.patterns.size, "The pattern count is incorrect")

        res.patterns.forEach {
            LexemPatternContentNodeTest.checkTestExpression(it)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect group without lexemes after pattern token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.GroupWithoutLexemeAfterPatternToken) {
            val text =
                    "${GroupLexemeNode.startToken}${LexemPatternContentNodeTest.testExpression}${GroupLexemeNode.patternToken}${GroupLexemeNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            parser.isDescriptiveCode = true
            GroupLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect group without patterns`() {
        TestUtils.assertParserException(AngmarParserExceptionType.GroupWithoutPatterns) {
            val text = "${GroupLexemeNode.startToken}${GroupLexemeNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            parser.isDescriptiveCode = true
            GroupLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect group without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.GroupWithoutEndToken) {
            val text = "${GroupLexemeNode.startToken}${LexemPatternContentNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            parser.isDescriptiveCode = true
            GroupLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", GroupLexemeNode.notOperator])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = GroupLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
