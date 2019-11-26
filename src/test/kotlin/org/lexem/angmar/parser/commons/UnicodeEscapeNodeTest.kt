package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*

internal class UnicodeEscapeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun provideCorrectEscapesForBrackets(): Stream<Arguments> {
            val sequence =
                    listOf("012345", "6789ab", "cdefAB", "CDEF01", "0", "01", "012", "0123", "01234", "012345", "6789")


            return sequence.map { Arguments.of(it) }.stream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["012345", "6789ab", "cdefAB", "CDEF01"])
    fun `parse correct unicode escape without brackets`(number: String) {
        val text = "${UnicodeEscapeNode.escapeStart}$number"
        val parser = LexemParser(IOStringReader.from(text))
        val res = UnicodeEscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeEscapeNode

        Assertions.assertEquals(number.toUpperCase().toInt(16), res.value, "The value is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [""])
    fun `parse incorrect unicode escape without brackets and value`(number: String) {
        TestUtils.assertParserException(AngmarParserExceptionType.UnicodeEscapeWithoutValue) {
            val text = "${UnicodeEscapeNode.escapeStart}$number"
            val parser = LexemParser(IOStringReader.from(text))
            UnicodeEscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["0", "01", "012", "0123", "01234", "01234"])
    fun `parse incorrect unicode escape without brackets but with fewer digits than allowed`(number: String) {
        TestUtils.assertParserException(AngmarParserExceptionType.UnicodeEscapeWithFewerDigitsThanAllowed) {
            val text = "${UnicodeEscapeNode.escapeStart}$number"
            val parser = LexemParser(IOStringReader.from(text))
            UnicodeEscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEscapesForBrackets")
    fun `parse correct unicode escape with brackets`(number: String) {
        val text =
                "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}$number${UnicodeEscapeNode.endBracket}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = UnicodeEscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeEscapeNode

        Assertions.assertEquals(number.toUpperCase().toInt(16), res.value, "The value is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEscapesForBrackets")
    fun `parse correct unicode escape with brackets and whitespaces`(number: String) {
        val text =
                "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}${WhitespaceNoEOLNodeTest.allWhiteCharsTest}$number${WhitespaceNoEOLNodeTest.allWhiteCharsTest}${UnicodeEscapeNode.endBracket}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = UnicodeEscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeEscapeNode

        Assertions.assertEquals(number.toUpperCase().toInt(16), res.value, "The value is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["0123456"])
    fun `parse incorrect unicode escape with brackets and with more digits than allowed`(number: String) {
        TestUtils.assertParserException(AngmarParserExceptionType.UnicodeEscapeWithBracketsWithMoreDigitsThanAllowed) {
            val text =
                    "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}$number${UnicodeEscapeNode.endBracket}"
            val parser = LexemParser(IOStringReader.from(text))
            UnicodeEscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect unicode escape with brackets but without value`() {
        TestUtils.assertParserException(AngmarParserExceptionType.UnicodeEscapeWithBracketsWithoutValue) {
            val text =
                    "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}${UnicodeEscapeNode.endBracket}"
            val parser = LexemParser(IOStringReader.from(text))
            UnicodeEscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect unicode escape without the final bracket`() {
        TestUtils.assertParserException(AngmarParserExceptionType.UnicodeEscapeWithBracketsWithoutEndToken) {
            val text = "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}0"
            val parser = LexemParser(IOStringReader.from(text))
            UnicodeEscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = UnicodeEscapeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
