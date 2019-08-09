package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.StringNode.Companion.additionalDelimiter
import org.lexem.angmar.utils.*
import java.util.stream.*

internal class UnescapedStringNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun provideComplexStrings(): Stream<Arguments> {
            val sequence = listOf("test ${UnescapedStringNode.endToken} test",
                    "test ${UnescapedStringNode.startToken}$additionalDelimiter test",
                    "test ${UnescapedStringNode.startToken}${additionalDelimiter.repeat(2)} test")


            return sequence.map { Arguments.of(it) }.stream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(
            strings = ["a", "this", "longText_with_a lotOfThings", "texts with\n break lines", "text with ${EscapeNode.startToken}d escapes", "$additionalDelimiter$additionalDelimiter"])
    fun `parse simple correct strings`(textContent: String) {
        val text =
                "${UnescapedStringNode.macroName}${UnescapedStringNode.startToken}$textContent${UnescapedStringNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnescapedStringNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnescapedStringNode

        Assertions.assertEquals(text, res.content, "The content is not correct")
        Assertions.assertEquals(0, res.minAdditionalDelimiterRequired,
                "The minAdditionalDelimiterRequired is not correct")
        Assertions.assertEquals(textContent, res.text, "The text is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse simple correct strings with two close tokens`() {
        val textContent =
                "this is a test ${UnescapedStringNode.startToken}$additionalDelimiter ${UnescapedStringNode.endToken}$additionalDelimiter"
        val text =
                "${UnescapedStringNode.macroName}$additionalDelimiter$additionalDelimiter${UnescapedStringNode.startToken}$textContent${UnescapedStringNode.endToken}$additionalDelimiter$additionalDelimiter"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnescapedStringNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnescapedStringNode

        Assertions.assertEquals(text, res.content, "The content is not correct")
        Assertions.assertEquals(2, res.minAdditionalDelimiterRequired,
                "The minAdditionalDelimiterRequired is not correct")
        Assertions.assertEquals(textContent, res.text, "The text is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideComplexStrings")
    fun `parse complex identifier`(textContent: String) {
        val additionalDelimiter = additionalDelimiter.repeat(
                textContent.filter { it.toString() == UnescapedStringNode.startToken || it.toString() == UnescapedStringNode.endToken || it.toString() == additionalDelimiter }.length)
        val text =
                "${UnescapedStringNode.macroName}$additionalDelimiter${UnescapedStringNode.startToken}$textContent${UnescapedStringNode.endToken}$additionalDelimiter"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnescapedStringNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnescapedStringNode

        Assertions.assertEquals(text, res.content, "The content is not correct")
        Assertions.assertEquals(additionalDelimiter.length, res.minAdditionalDelimiterRequired,
                "The minAdditionalDelimiterRequired is not correct")
        Assertions.assertEquals(textContent, res.text, "The text is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse unclosed string`() {
        assertParserException {
            val text =
                    "${UnescapedStringNode.macroName}$additionalDelimiter${UnescapedStringNode.startToken} this is a test"
            UnescapedStringNode.parse(LexemParser(CustomStringReader.from(text)))
        }
    }

    @ParameterizedTest
    @MethodSource("provideComplexStrings")
    fun `parse incorrect complex strings`(textContent: String) {
        val text =
                "${UnescapedStringNode.macroName}${UnescapedStringNode.startToken}$textContent${UnescapedStringNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnescapedStringNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnescapedStringNode

        val lastIndex = textContent.indexOf(
                StringNode.endToken) + StringNode.endToken.length + UnescapedStringNode.macroName.length + 1
        Assertions.assertEquals(text.substring(0 until lastIndex), res.content, "The content is not correct")
        Assertions.assertEquals(0, res.minAdditionalDelimiterRequired,
                "The minAdditionalDelimiterRequired is not correct")
        Assertions.assertEquals(textContent.substring(0 until textContent.indexOf(StringNode.endToken)), res.text,
                "The text is not correct")

        Assertions.assertEquals(lastIndex, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["", "3", UnescapedStringNode.macroName, "${UnescapedStringNode.macroName}$additionalDelimiter"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnescapedStringNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
