package org.lexem.angmar.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.readers.CustomStringReader

internal class QuotedIdentifierNodeTest {
    @Test
    fun `parse simple quoted identifier`() {
        for (id in listOf("a", "this", "longText_with_a_lotOfThings")) {
            val text = "${QuotedIdentifierNode.startQuote}$id${QuotedIdentifierNode.endQuote}"
            val res = QuotedIdentifierNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as QuotedIdentifierNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(1, res.texts.size)
            Assertions.assertEquals(id, res.texts.first())
            Assertions.assertEquals(0, res.escapes.size)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse complex quoted identifier`() {
        for (id in listOf("this an example with whitespaces",
                "this is an example with , commas dots . and another characters +")) {
            val text = "${QuotedIdentifierNode.startQuote}$id${QuotedIdentifierNode.endQuote}"
            val res = QuotedIdentifierNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as QuotedIdentifierNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(1, res.texts.size)
            Assertions.assertEquals(id, res.texts.first())
            Assertions.assertEquals(0, res.escapes.size)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse complex quoted identifier with escapes`() {
        let {
            val id = "this is an example with escapes \\k \\ll"
            val text = "${QuotedIdentifierNode.startQuote}$id${QuotedIdentifierNode.endQuote}"
            val res = QuotedIdentifierNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as QuotedIdentifierNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(3, res.texts.size)
            Assertions.assertEquals("this is an example with escapes ", res.texts[0])
            Assertions.assertEquals(" ", res.texts[1])
            Assertions.assertEquals("l", res.texts[2])
            Assertions.assertEquals(2, res.escapes.size)
            Assertions.assertEquals(text, res.toString())
        }

        let {
            val id = "\\k \\ll this is an example with escapes "
            val text = "${QuotedIdentifierNode.startQuote}$id${QuotedIdentifierNode.endQuote}"
            val res = QuotedIdentifierNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as QuotedIdentifierNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(3, res.texts.size)
            Assertions.assertEquals("", res.texts[0])
            Assertions.assertEquals(" ", res.texts[1])
            Assertions.assertEquals("l this is an example with escapes ", res.texts[2])
            Assertions.assertEquals(2, res.escapes.size)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse incorrect empty quoted identifier`() {
        Assertions.assertThrows(AngmarParserException::class.java) {
            val text = "${QuotedIdentifierNode.startQuote}${QuotedIdentifierNode.endQuote}"
            QuotedIdentifierNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
        }
    }

    @Test
    fun `parse incorrect complex quoted identifier`() {
        for (id in listOf("this is\nan example\r\nwith end of lines")) {
            Assertions.assertThrows(AngmarParserException::class.java) {
                val text = "${QuotedIdentifierNode.startQuote}$id${QuotedIdentifierNode.endQuote}"
                QuotedIdentifierNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `parse incorrect not-ended quoted identifier`() {
        for (id in listOf("example\\`", "example")) {
            Assertions.assertThrows(AngmarParserException::class.java) {
                val text = "${QuotedIdentifierNode.startQuote}$id"
                QuotedIdentifierNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )
            }
        }
    }

    @Test
    @Disabled
    fun `test to see the logs`() {
        try {
            // Change this code by one of the tests above that throw an error.
            val text = "${QuotedIdentifierNode.startQuote}${QuotedIdentifierNode.endQuote}"
            QuotedIdentifierNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
        } catch (e: AngmarParserException) {
            e.logMessage()
        }
    }
}