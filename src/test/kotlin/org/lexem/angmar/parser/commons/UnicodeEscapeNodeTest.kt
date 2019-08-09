package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
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
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnicodeEscapeNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeEscapeNode

        Assertions.assertEquals(Converters.hexToDouble(number), res.value, "The value is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["", "0", "01", "012", "0123", "01234", "01234"])
    fun `parse incorrect unicode escape without brackets`(number: String) {
        assertParserException {
            val text = "${UnicodeEscapeNode.escapeStart}$number"
            val parser = LexemParser(CustomStringReader.from(text))
            UnicodeEscapeNode.parse(parser)
        }
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEscapesForBrackets")
    fun `parse correct unicode escape with brackets`(number: String) {
        val text =
                "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}$number${UnicodeEscapeNode.endBracket}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnicodeEscapeNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeEscapeNode

        Assertions.assertEquals(Converters.hexToDouble(number), res.value, "The value is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEscapesForBrackets")
    fun `parse correct unicode escape with brackets and whitespaces`(number: String) {
        val text =
                "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}${WhitespaceNoEOLNodeTest.allWhiteCharsTest}$number${WhitespaceNoEOLNodeTest.allWhiteCharsTest}${UnicodeEscapeNode.endBracket}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnicodeEscapeNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeEscapeNode

        Assertions.assertEquals(Converters.hexToDouble(number), res.value, "The value is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["0123456", ""])
    fun `parse incorrect unicode escape with brackets`(number: String) {
        assertParserException {
            val text =
                    "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}$number${UnicodeEscapeNode.endBracket}"
            UnicodeEscapeNode.parse(LexemParser(CustomStringReader.from(text)))
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect unicode escape without the final bracket`() {
        assertParserException {
            val text = "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}0"
            UnicodeEscapeNode.parse(LexemParser(CustomStringReader.from(text)))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnicodeEscapeNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
