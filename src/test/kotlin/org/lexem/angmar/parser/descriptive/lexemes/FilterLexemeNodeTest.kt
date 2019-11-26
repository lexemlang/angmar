package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class FilterLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${FilterLexemeNode.startToken}${SelectorNodeTest.testExpression}${FilterLexemeNode.endToken}"

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegative in listOf(false, true)) {
                    var text =
                            "${FilterLexemeNode.startToken}${SelectorNodeTest.testExpression}${FilterLexemeNode.endToken}"

                    if (isNegative) {
                        text = FilterLexemeNode.notOperator + text
                    }

                    yield(Arguments.of(text, isNegative))

                    // With whitespaces
                    text =
                            "${FilterLexemeNode.startToken} ${SelectorNodeTest.testExpression} ${FilterLexemeNode.endToken}"

                    if (isNegative) {
                        text = FilterLexemeNode.notOperator + text
                    }

                    yield(Arguments.of(text, isNegative))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as FilterLexemeNode

            Assertions.assertFalse(node.isNegated, "The isNegated property is incorrect")
            SelectorNodeTest.checkTestExpression(node.selector, isAddition = false)
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct node`(text: String, isNegative: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = FilterLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FilterLexemeNode

        Assertions.assertEquals(isNegative, res.isNegated, "The isNegated property is incorrect")
        SelectorNodeTest.checkTestExpression(res.selector, isAddition = false)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect node without selector`() {
        TestUtils.assertParserException(AngmarParserExceptionType.FilterLexemeWithoutSelector) {
            val text = "${FilterLexemeNode.startToken}${FilterLexemeNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            FilterLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect node without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.FilterLexemeWithoutEndToken) {
            val text = "${FilterLexemeNode.startToken}${SelectorNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            FilterLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", FilterLexemeNode.notOperator])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = FilterLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
