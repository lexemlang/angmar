package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.StringNode.Companion.additionalDelimiter
import org.lexem.angmar.utils.*
import java.util.stream.*

internal class StringNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${StringNode.startToken}abc${StringNode.endToken}"

        @JvmStatic
        private fun provideComplexStrings(): Stream<Arguments> {
            val sequence =
                    listOf("test ${StringNode.startToken} test", "test ${StringNode.endToken}$additionalDelimiter test",
                            "test ${StringNode.startToken}${additionalDelimiter.repeat(2)} test")


            return sequence.map { Arguments.of(it) }.stream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is StringNode, "The node is not a StringNode")
            node as StringNode

            Assertions.assertEquals(0, node.minAdditionalDelimiterRequired,
                    "The minAdditionalDelimiterRequired is incorrect")
            Assertions.assertEquals(1, node.texts.size, "The text list size is incorrect")
            Assertions.assertEquals(0, node.escapes.size, "The escapes list size is incorrect")
            Assertions.assertEquals("abc", node.texts.first(), "The first text is incorrect")
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(
            strings = ["a", "this", "longText_with_a lotOfThings", "texts with\n break lines", "$additionalDelimiter$additionalDelimiter"])
    fun `parse simple correct strings`(textContent: String) {
        val text = "${StringNode.startToken}$textContent${StringNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = StringNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as StringNode

        Assertions.assertEquals(text, res.content, "The content is incorrect")
        Assertions.assertEquals(0, res.minAdditionalDelimiterRequired,
                "The minAdditionalDelimiterRequired is incorrect")
        Assertions.assertEquals(1, res.texts.size, "The text list size is incorrect")
        Assertions.assertEquals(0, res.escapes.size, "The escapes list size is incorrect")
        Assertions.assertEquals(textContent, res.texts.first(), "The first text is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["\\\"", "this \\is test", "this \\ have \\two", "\\x at the begining", "or at the end\\x"])
    fun `parse simple correct strings with escapes`(textContent: String) {
        val text = "${StringNode.startToken}$textContent${StringNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = StringNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as StringNode

        Assertions.assertEquals(text, res.content, "The content is incorrect")
        Assertions.assertEquals(0, res.minAdditionalDelimiterRequired,
                "The minAdditionalDelimiterRequired is incorrect")

        val texts = textContent.split("\\")
        Assertions.assertEquals(texts.size, res.texts.size, "The text list size is incorrect")
        Assertions.assertEquals(texts.size - 1, res.escapes.size, "The escapes list size is incorrect")

        Assertions.assertEquals(texts.first(), res.texts.first(), "The first text is incorrect")

        for (i in 1 until texts.size) {
            Assertions.assertEquals(texts[i].substring(1), res.texts[i], "The text $i is incorrect")
            Assertions.assertEquals("${EscapeNode.startToken}${texts[i][0]}", res.escapes[i - 1].content,
                    "The escape ${i - 1} is incorrect")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideComplexStrings")
    fun `parse complex identifier`(textContent: String) {
        val additionalDelimiter = additionalDelimiter.repeat(
                textContent.filter { it.toString() == StringNode.startToken || it.toString() == StringNode.endToken || it.toString() == additionalDelimiter }.length)
        val text = "$additionalDelimiter${StringNode.startToken}$textContent${StringNode.endToken}$additionalDelimiter"
        val parser = LexemParser(IOStringReader.from(text))
        val res = StringNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as StringNode

        Assertions.assertEquals(text, res.content, "The content is incorrect")
        Assertions.assertEquals(additionalDelimiter.length, res.minAdditionalDelimiterRequired,
                "The minAdditionalDelimiterRequired is incorrect")

        val texts = textContent.split("\\")
        Assertions.assertEquals(texts.size, res.texts.size, "The text list size is incorrect")
        Assertions.assertEquals(texts.size - 1, res.escapes.size, "The escapes list size is incorrect")

        Assertions.assertEquals(texts.first(), res.texts.first(), "The first text is incorrect")

        for (i in 1 until texts.size) {
            Assertions.assertEquals(texts[i].substring(1), res.texts[i], "The text $i is incorrect")
            Assertions.assertEquals("${EscapeNode.startToken}${texts[i][0]}", res.escapes[i - 1].content,
                    "The escape ${i - 1} is incorrect")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse unclosed string`() {
        TestUtils.assertParserException(AngmarParserExceptionType.StringWithoutEndQuote) {
            val text = "$additionalDelimiter${StringNode.startToken} this is a test"
            val parser = LexemParser(IOStringReader.from(text))
            StringNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @MethodSource("provideComplexStrings")
    fun `parse incorrect complex strings`(textContent: String) {
        val text = "${StringNode.startToken}$textContent${StringNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = StringNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as StringNode

        val lastIndex = textContent.indexOf(StringNode.endToken) + StringNode.endToken.length + 1
        Assertions.assertEquals(text.substring(0 until lastIndex), res.content, "The content is incorrect")
        Assertions.assertEquals(0, res.minAdditionalDelimiterRequired,
                "The minAdditionalDelimiterRequired is incorrect")
        Assertions.assertEquals(textContent.substring(0 until textContent.indexOf(StringNode.endToken)),
                res.texts.first(), "The text is incorrect")

        Assertions.assertEquals(lastIndex, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3", additionalDelimiter])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = StringNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
