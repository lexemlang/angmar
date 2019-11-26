package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import java.util.stream.*
import kotlin.streams.*

internal class WhitespaceNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        private const val multilineComment =
                "${CommentMultilineNode.MultilineStartToken} comment\nmultiline ${CommentMultilineNode.MultilineEndToken}"
        private const val singleLineComment = "${CommentSingleLineNode.SingleLineStartToken} comment"
        private val allWhiteCharsTest = StringBuilder().apply {
            for (c in WhitespaceNode.whitespaceChars) {
                append(c)
            }

            for (c in WhitespaceNode.endOfLineChars) {
                append(c)
            }
        }.toString()

        @JvmStatic
        private fun provideWsNoComments(): Stream<Arguments> {
            val sequence = sequence {
                for (c in WhitespaceNode.whitespaceChars + WhitespaceNode.endOfLineChars.asSequence()) {
                    yield("$c")
                }

                yield(allWhiteCharsTest)
            }


            return sequence.map { Arguments.of(it) }.asStream()
        }

        @JvmStatic
        private fun provideEOL() = WhitespaceEOLNodeTest.provideEOL()
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideWsNoComments")
    fun `parse correct whitespaces with end-of-lines`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = WhitespaceNode.parse(parser)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [multilineComment, singleLineComment])
    fun `parse correct comments`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = WhitespaceNode.parse(parser)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct complex whitespaces`() {
        val text = "$multilineComment$singleLineComment\n$multilineComment$allWhiteCharsTest"
        val parser = LexemParser(IOStringReader.from(text))
        val res = WhitespaceNode.parse(parser)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideWsNoComments")
    fun `parse correct simple whitespaces`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = WhitespaceNode.parseSimpleWhitespaces(parser)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [multilineComment, singleLineComment])
    fun `parse incorrect simple whitespaces`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = WhitespaceNode.parseSimpleWhitespaces(parser)

        Assertions.assertFalse(res, "The parser must capture something")

        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser advanced the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideEOL")
    fun `read correct line breaks`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = WhitespaceNode.readLineBreak(parser)

        Assertions.assertNotNull(res, "The parser must capture something")

        Assertions.assertEquals(text, res, "The parser did not read the whole line break")
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = WhitespaceNode.parse(parser)

        Assertions.assertFalse(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
