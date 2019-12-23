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

internal class AdditionFilterLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${AdditionFilterLexemeNode.startToken}${SelectorNodeTest.testExpression}${AdditionFilterLexemeNode.endToken}"

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                var text =
                        "${AdditionFilterLexemeNode.startToken}${SelectorNodeTest.testExpression}${AdditionFilterLexemeNode.endToken}"

                yield(Arguments.of(text))

                // With whitespaces
                text =
                        "${AdditionFilterLexemeNode.startToken} ${SelectorNodeTest.testExpression} ${AdditionFilterLexemeNode.endToken}"

                yield(Arguments.of(text))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as AdditionFilterLexemeNode

            SelectorNodeTest.checkTestExpression(node.selector, isAddition = true)
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AdditionFilterLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AdditionFilterLexemeNode

        SelectorNodeTest.checkTestExpression(res.selector, isAddition = true)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect node without selector`() {
        TestUtils.assertParserException(AngmarParserExceptionType.AdditionFilterLexemeWithoutSelector) {
            val text = "${AdditionFilterLexemeNode.startToken}${AdditionFilterLexemeNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            AdditionFilterLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect node without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.AdditionFilterLexemeWithoutEndToken) {
            val text = "${AdditionFilterLexemeNode.startToken}${SelectorNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            AdditionFilterLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", AdditionFilterLexemeNode.notOperator])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AdditionFilterLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
