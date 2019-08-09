package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import java.util.stream.*
import kotlin.streams.*

internal class CommentSingleLineNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        private val commentTexts = listOf("", "a", " this is a long test in one line")

        @JvmStatic
        private fun provideCorrectInlineComments(): Stream<Arguments> {
            return commentTexts.map { "${CommentSingleLineNode.SingleLineStartToken}$it" }.map { Arguments.of(it) }
                    .stream()
        }

        @JvmStatic
        private fun provideCorrectInlineCommentsAllEOLs(): Stream<Arguments> {
            var sequence = sequence<Arguments> { }

            for (ch in WhitespaceNode.endOfLineChars) {
                sequence += commentTexts.map { "${CommentSingleLineNode.SingleLineStartToken}$it$ch" }
                        .map { Arguments.of(it) }
            }
            return sequence.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectInlineCommentsAllEOLs")
    fun `parse correct single-line comments`(comment: String) {
        val parser = LexemParser(CustomStringReader.from(comment))
        val res = CommentSingleLineNode.parse(parser)

        Assertions.assertTrue(res, "A correct single-line comment has not been read")
        Assertions.assertEquals(comment.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectInlineComments")
    fun `parse correct eof`(comment: String) {
        val parser = LexemParser(CustomStringReader.from(comment))
        val res = CommentSingleLineNode.parse(parser)

        Assertions.assertTrue(res, "A correct single-line comment has not been read")
        Assertions.assertEquals(comment.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectInlineComments")
    fun `parse correct windows eol`(comment: String) {
        val text = "$comment${WhitespaceNode.windowsEndOfLine}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = CommentSingleLineNode.parse(parser)

        Assertions.assertTrue(res, "A correct single-line comment has not been read")
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = CommentSingleLineNode.parse(parser)

        Assertions.assertFalse(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
