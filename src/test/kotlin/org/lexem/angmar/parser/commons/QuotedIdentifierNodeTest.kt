package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*

internal class QuotedIdentifierNodeTest {
    @ParameterizedTest
    @ValueSource(strings = ["a", "this", "longText_with_a_lotOfThings"])
    fun `parse simple quoted identifier`(id: String) {
        val text = "${QuotedIdentifierNode.startQuote}$id${QuotedIdentifierNode.endQuote}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuotedIdentifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as QuotedIdentifierNode

        Assertions.assertEquals(text, res.content, "The content is incorrect")
        Assertions.assertEquals(1, res.texts.size, "The text size is incorrect")
        Assertions.assertEquals(id, res.texts.first(), "The first text is incorrect")
        Assertions.assertEquals(0, res.escapes.size, "The escape size is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["this an example with whitespaces", "this is an example with , commas dots . and another characters +"])
    fun `parse complex quoted identifier`(id: String) {
        val text = "${QuotedIdentifierNode.startQuote}$id${QuotedIdentifierNode.endQuote}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuotedIdentifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as QuotedIdentifierNode

        Assertions.assertEquals(text, res.content, "The content is incorrect")
        Assertions.assertEquals(1, res.texts.size, "The text size is incorrect")
        Assertions.assertEquals(id, res.texts.first(), "The first text is incorrect")
        Assertions.assertEquals(0, res.escapes.size, "The escape size is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["this is an example with escapes ${EscapeNode.startToken}k ${EscapeNode.startToken}ll", "${EscapeNode.startToken}k ${EscapeNode.startToken}ll this is an example with escapes "])
    fun `parse complex quoted identifier with escapes`(id: String) {
        val text = "${QuotedIdentifierNode.startQuote}$id${QuotedIdentifierNode.endQuote}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuotedIdentifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as QuotedIdentifierNode

        Assertions.assertEquals(text, res.content, "The content is incorrect")

        val texts = id.split(EscapeNode.startToken)
        Assertions.assertEquals(texts.size, res.texts.size, "The text size is incorrect")
        Assertions.assertEquals(texts.size - 1, res.escapes.size, "The escape size is incorrect")

        Assertions.assertEquals(texts.first(), res.texts.first(), "The first text is incorrect")
        for (i in 1 until texts.size) {
            val txt = texts[i].substring(1)
            val escape = texts[i].substring(0 until 1)
            Assertions.assertEquals(txt, res.texts[i], "The $i-nth text is incorrect")
            Assertions.assertEquals("${EscapeNode.startToken}$escape", res.escapes[i - 1].content,
                    "The $i-nth escape is incorrect")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect empty quoted identifier`() {
        TestUtils.assertParserException(AngmarParserExceptionType.QuotedIdentifiersEmpty) {
            val text = "${QuotedIdentifierNode.startQuote}${QuotedIdentifierNode.endQuote}"
            val parser = LexemParser(IOStringReader.from(text))
            QuotedIdentifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["this is\nan example\r\nwith end of lines"])
    fun `parse incorrect complex quoted identifier`(id: String) {
        TestUtils.assertParserException(AngmarParserExceptionType.QuotedIdentifiersMultilineNotAllowed) {
            val text = "${QuotedIdentifierNode.startQuote}$id${QuotedIdentifierNode.endQuote}"
            val parser = LexemParser(IOStringReader.from(text))
            QuotedIdentifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["example\\`", "example"])
    fun `parse incorrect not-ended quoted identifier`(id: String) {
        TestUtils.assertParserException(AngmarParserExceptionType.QuotedIdentifiersWithoutEndQuote) {
            val text = "${QuotedIdentifierNode.startQuote}$id"
            val parser = LexemParser(IOStringReader.from(text))
            QuotedIdentifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuotedIdentifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
