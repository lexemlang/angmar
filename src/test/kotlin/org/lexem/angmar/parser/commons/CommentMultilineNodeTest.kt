package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class CommentMultilineNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun provideCorrectMultilineComments(): Stream<Arguments> {
            val commentTexts =
                    listOf("a", " this is a long test in one line ", " this is a\nlong test in\n more than \none line ",
                            " this is a long test in one line ", " this is a\nlong test in\n more than \none line ",
                            " this is a comment finished with one more additional token ${CommentMultilineNode.MultilineAdditionalToken}",
                            " ${CommentMultilineNode.MultilineStartToken} ${CommentMultilineNode.MultilineEndToken} ${CommentMultilineNode.MultilineStartToken}${CommentMultilineNode.MultilineAdditionalToken} ${CommentMultilineNode.MultilineAdditionalToken}${CommentMultilineNode.MultilineEndToken} ${CommentMultilineNode.MultilineAdditionalToken.repeat(
                                    3)} ")
            val repetitions = listOf(0, 0, 0, 1, 2, 2, 3)

            val immediatelyClosed = List(5) {
                "${CommentMultilineNode.MultilineStartToken}${CommentMultilineNode.MultilineAdditionalToken.repeat(
                        it)}${CommentMultilineNode.MultilineEndToken}"
            }.map { Arguments.of(it) }

            val result = commentTexts.zip(repetitions).map {
                "${CommentMultilineNode.MultilineStartToken}${CommentMultilineNode.MultilineAdditionalToken.repeat(
                        it.second)}${it.first}${CommentMultilineNode.MultilineAdditionalToken.repeat(
                        it.second)}${CommentMultilineNode.MultilineEndToken}"
            }.map { Arguments.of(it) }.asSequence() + immediatelyClosed.asSequence()

            return result.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectMultilineComments")
    fun `parse correct multiline comments`(comment: String) {
        val parser = LexemParser(IOStringReader.from(comment))
        val res = CommentMultilineNode.parse(parser)

        Assertions.assertTrue(res, "A correct multiline comment has not been read")
        Assertions.assertEquals(comment.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse unclosed multiline comments`() {
        TestUtils.assertParserException(AngmarParserExceptionType.MultilineCommentWithoutEndToken) {
            val text = "${CommentMultilineNode.MultilineStartToken} this comment \n is not closed"
            CommentMultilineNode.parse(LexemParser(IOStringReader.from(text)))
        }
    }

    @Test
    @Incorrect
    fun `parse closed multiline comments but with an incorrect number of additional tokens`() {
        TestUtils.assertParserException(AngmarParserExceptionType.MultilineCommentWithoutEndToken) {
            val text =
                    "${CommentMultilineNode.MultilineStartToken}${CommentMultilineNode.MultilineAdditionalToken.repeat(
                            3)} this comment \n is closed with an incorrect number of ${CommentMultilineNode.MultilineAdditionalToken} ${CommentMultilineNode.MultilineAdditionalToken.repeat(
                            2)}${CommentMultilineNode.MultilineEndToken}"
            CommentMultilineNode.parse(LexemParser(IOStringReader.from(text)))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = CommentMultilineNode.parse(parser)

        Assertions.assertFalse(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
