package org.lexem.angmar.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.readers.CustomStringReader

internal class UnicodeEscapeNodeTest {
    @Test
    fun `parse correct unicode escape without brackets`() {
        for (number in listOf("01234567", "89abcdef", "ABCDEF01")) {
            val text = "${UnicodeEscapeNode.escapeStart}$number"
            val res = UnicodeEscapeNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as UnicodeEscapeNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertFalse(res.hasBrackets)
            Assertions.assertNull(res.leftWhitespace)
            Assertions.assertNull(res.rightWhitespace)
            Assertions.assertEquals(number, res.value)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse incorrect unicode escape without brackets`() {
        for (number in listOf("", "0", "01", "012", "0123", "01234", "012345", "0123456")) {
            Assertions.assertThrows(AngmarParserException::class.java) {
                val text = "${UnicodeEscapeNode.escapeStart}$number"
                UnicodeEscapeNode.parse(
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
    fun `parse correct unicode escape with brackets`() {
        for (number in listOf("01234567", "89abcdef", "ABCDEF01", "0", "01", "012", "0123", "01234", "012345",
                "0123456")) {
            val text =
                    "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}$number${UnicodeEscapeNode.endBracket}"
            val res = UnicodeEscapeNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as UnicodeEscapeNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertTrue(res.hasBrackets)
            Assertions.assertNotNull(res.leftWhitespace)
            Assertions.assertEquals("", res.leftWhitespace!!.toString())
            Assertions.assertNotNull(res.rightWhitespace)
            Assertions.assertEquals("", res.rightWhitespace!!.toString())
            Assertions.assertEquals(number, res.value)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse correct unicode escape with brackets and whitespaces`() {
        for (number in listOf("01234567", "89abcdef", "ABCDEF01", "0", "01", "012", "0123", "01234", "012345",
                "0123456")) {
            val text =
                    "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}${WhitespaceNoEOLNodeTest.allWhiteCharsTest}$number${WhitespaceNoEOLNodeTest.allWhiteCharsTest}${UnicodeEscapeNode.endBracket}"
            val res = UnicodeEscapeNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as UnicodeEscapeNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertTrue(res.hasBrackets)
            Assertions.assertNotNull(res.leftWhitespace)
            Assertions.assertEquals(WhitespaceNoEOLNodeTest.allWhiteCharsTest, res.leftWhitespace!!.toString())
            Assertions.assertNotNull(res.rightWhitespace)
            Assertions.assertEquals(WhitespaceNoEOLNodeTest.allWhiteCharsTest, res.rightWhitespace!!.toString())
            Assertions.assertEquals(number, res.value)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse incorrect unicode escape with brackets`() {
        for (number in listOf("012345678", "")) {
            Assertions.assertThrows(AngmarParserException::class.java) {
                val text =
                        "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}$number${UnicodeEscapeNode.endBracket}"
                UnicodeEscapeNode.parse(
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
    fun `parse incorrect unicode escape without the final bracket`() {
        Assertions.assertThrows(AngmarParserException::class.java) {
            val text = "${UnicodeEscapeNode.escapeStart}${UnicodeEscapeNode.startBracket}0"
            UnicodeEscapeNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
        }
    }

    @Test
    @Disabled
    fun `test to see the logs`() {
        try {
            // Change this code by one of the tests above that throw an error.
            val text = "${UnicodeEscapeNode.escapeStart}0000000"
            UnicodeEscapeNode.parse(
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