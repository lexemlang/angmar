package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.utils.*
import java.util.stream.*

internal class PointEscapeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun provideCorrectEscapesForBrackets(): Stream<Arguments> {
            val sequence =
                    listOf("01234567", "89abcdef", "ABCDEF", "0", "01", "012", "0123", "01234", "012345", "0123456",
                            "01234567", "89")


            return sequence.map { Arguments.of(it) }.stream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["01234567", "89abcdef", "ABCDEF01"])
    fun `parse correct point escape without brackets`(number: String) {
        val text = "${PointEscapeNode.escapeStart}$number"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PointEscapeNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PointEscapeNode

        Assertions.assertEquals(Converters.hexToDouble(number), res.value, "The value is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["", "0", "01", "012", "0123", "01234", "01234", "012345", "0123456"])
    fun `parse incorrect point escape without brackets`(number: String) {
        assertParserException {
            val text = "${PointEscapeNode.escapeStart}$number"
            val parser = LexemParser(CustomStringReader.from(text))
            PointEscapeNode.parse(parser)
        }
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEscapesForBrackets")
    fun `parse correct point escape with brackets`(number: String) {
        val text = "${PointEscapeNode.escapeStart}${PointEscapeNode.startBracket}$number${PointEscapeNode.endBracket}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PointEscapeNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PointEscapeNode

        Assertions.assertEquals(Converters.hexToDouble(number), res.value, "The value is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectEscapesForBrackets")
    fun `parse correct point escape with brackets and whitespaces`(number: String) {
        val text =
                "${PointEscapeNode.escapeStart}${PointEscapeNode.startBracket}${WhitespaceNoEOLNodeTest.allWhiteCharsTest}$number${WhitespaceNoEOLNodeTest.allWhiteCharsTest}${PointEscapeNode.endBracket}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PointEscapeNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PointEscapeNode

        Assertions.assertEquals(Converters.hexToDouble(number), res.value, "The value is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["012345678", ""])
    fun `parse incorrect point escape with brackets`(number: String) {
        assertParserException {
            val text =
                    "${PointEscapeNode.escapeStart}${PointEscapeNode.startBracket}$number${PointEscapeNode.endBracket}"
            PointEscapeNode.parse(LexemParser(CustomStringReader.from(text)))
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect point escape without the final bracket`() {
        assertParserException {
            val text = "${PointEscapeNode.escapeStart}${PointEscapeNode.startBracket}0"
            PointEscapeNode.parse(LexemParser(CustomStringReader.from(text)))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PointEscapeNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
