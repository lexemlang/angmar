package org.lexem.angmar.parser.descriptive.lexemes.anchors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class RelativeAnchorLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = RelativeAnchorLexemeNode.startTextToken

        @JvmStatic
        private fun provideAbbreviation(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegative in listOf(false, true)) {
                    for (prefix in listOf(RelativeAnchorLexemeNode.RelativeAnchorType.StartText,
                            RelativeAnchorLexemeNode.RelativeAnchorType.StartLine,
                            RelativeAnchorLexemeNode.RelativeAnchorType.EndText,
                            RelativeAnchorLexemeNode.RelativeAnchorType.EndLine)) {
                        var text = prefix.token

                        if (isNegative) {
                            text = RelativeAnchorLexemeNode.notOperator + text
                        }

                        yield(Arguments.of(text, isNegative, prefix))
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegative in listOf(false, true)) {
                    for (anchorCount in 1..3) {
                        for (prefix in listOf(RelativeAnchorLexemeNode.RelativeAnchorType.StartLine,
                                RelativeAnchorLexemeNode.RelativeAnchorType.EndLine)) {
                            val list = List(anchorCount) { RelativeElementAnchorLexemeNodeTest.testExpression }
                            var text = "${prefix.token}${RelativeAnchorLexemeNode.groupStartToken}${list.joinToString(
                                    " ")}${RelativeAnchorLexemeNode.groupEndToken}"

                            if (isNegative) {
                                text = RelativeAnchorLexemeNode.notOperator + text
                            }

                            yield(Arguments.of(text, isNegative, anchorCount, prefix))

                            // With whitespaces
                            text = "${prefix.token}${RelativeAnchorLexemeNode.groupStartToken} ${list.joinToString(
                                    " ")} ${RelativeAnchorLexemeNode.groupEndToken}"

                            if (isNegative) {
                                text = RelativeAnchorLexemeNode.notOperator + text
                            }

                            yield(Arguments.of(text, isNegative, anchorCount, prefix))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as RelativeAnchorLexemeNode

            Assertions.assertFalse(node.isNegated, "The isNegated property is incorrect")
            Assertions.assertEquals(0, node.elements.size, "The element count is incorrect")
            Assertions.assertEquals(RelativeAnchorLexemeNode.RelativeAnchorType.StartText, node.type,
                    "The type property is incorrect")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideAbbreviation")
    fun `parse correct abbreviations`(text: String, isNegative: Boolean,
            prefix: RelativeAnchorLexemeNode.RelativeAnchorType) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = RelativeAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as RelativeAnchorLexemeNode

        Assertions.assertEquals(isNegative, res.isNegated, "The isNegated property is incorrect")
        Assertions.assertEquals(0, res.elements.size, "The element count is incorrect")
        Assertions.assertEquals(prefix, res.type, "The type property is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct node`(text: String, isNegative: Boolean, anchorCount: Int,
            prefix: RelativeAnchorLexemeNode.RelativeAnchorType) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = RelativeAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as RelativeAnchorLexemeNode

        Assertions.assertEquals(isNegative, res.isNegated, "The isNegated property is incorrect")
        Assertions.assertEquals(anchorCount, res.elements.size, "The element count is incorrect")

        res.elements.forEach {
            RelativeElementAnchorLexemeNodeTest.checkTestExpression(it)
        }

        Assertions.assertEquals(prefix, res.type, "The type property is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect anchor node without any anchors`() {
        TestUtils.assertParserException(AngmarParserExceptionType.RelativeAnchorGroupWithoutAnchors) {
            val text =
                    "${RelativeAnchorLexemeNode.startLineToken}${RelativeAnchorLexemeNode.groupStartToken}${RelativeAnchorLexemeNode.groupEndToken}"
            val parser = LexemParser(IOStringReader.from(text))
            RelativeAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect anchor node without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.RelativeAnchorGroupWithoutEndToken) {
            val text =
                    "${RelativeAnchorLexemeNode.startLineToken}${RelativeAnchorLexemeNode.groupStartToken}${RelativeElementAnchorLexemeNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            RelativeAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", RelativeAnchorLexemeNode.notOperator])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = RelativeAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
