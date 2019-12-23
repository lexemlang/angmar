package org.lexem.angmar.parser.descriptive.lexemes.anchors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.BitlistNodeTest.Companion.prefix
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class AbsoluteAnchorLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${AbsoluteAnchorLexemeNode.startToken}${AbsoluteElementAnchorLexemeNodeTest.testExpression}${AbsoluteAnchorLexemeNode.endToken}"

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegative in listOf(false, true)) {
                    for (anchorCount in 1..3) {
                        val list = List(anchorCount) { AbsoluteElementAnchorLexemeNodeTest.testExpression }
                        var text = "${AbsoluteAnchorLexemeNode.startToken}${list.joinToString(
                                " ")}${AbsoluteAnchorLexemeNode.endToken}"

                        if (isNegative) {
                            text = AbsoluteAnchorLexemeNode.notOperator + text
                        }

                        yield(Arguments.of(text, isNegative, anchorCount, prefix))

                        // With whitespaces
                        text = "${AbsoluteAnchorLexemeNode.startToken} ${list.joinToString(
                                " ")} ${AbsoluteAnchorLexemeNode.endToken}"

                        if (isNegative) {
                            text = AbsoluteAnchorLexemeNode.notOperator + text
                        }

                        yield(Arguments.of(text, isNegative, anchorCount, prefix))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as AbsoluteAnchorLexemeNode

            Assertions.assertFalse(node.isNegated, "The isNegated property is incorrect")
            Assertions.assertEquals(1, node.elements.size, "The element count is incorrect")

            node.elements.forEach {
                AbsoluteElementAnchorLexemeNodeTest.checkTestExpression(it)
            }
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct node`(text: String, isNegative: Boolean, anchorCount: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AbsoluteAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AbsoluteAnchorLexemeNode

        Assertions.assertEquals(isNegative, res.isNegated, "The isNegated property is incorrect")
        Assertions.assertEquals(anchorCount, res.elements.size, "The element count is incorrect")

        res.elements.forEach {
            AbsoluteElementAnchorLexemeNodeTest.checkTestExpression(it)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect anchor node without any anchors`() {
        TestUtils.assertParserException(AngmarParserExceptionType.AbsoluteAnchorGroupWithoutAnchors) {
            val text = "${AbsoluteAnchorLexemeNode.startToken}${AbsoluteAnchorLexemeNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            AbsoluteAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect anchor node without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.AbsoluteAnchorGroupWithoutEndToken) {
            val text = "${AbsoluteAnchorLexemeNode.startToken}${AbsoluteElementAnchorLexemeNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            AbsoluteAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", AbsoluteAnchorLexemeNode.notOperator])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AbsoluteAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
