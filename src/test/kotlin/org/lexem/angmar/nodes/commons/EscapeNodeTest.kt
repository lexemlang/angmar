package org.lexem.angmar.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.readers.CustomStringReader

internal class EscapeNodeTest {
    @Test
    fun `parse correct escape like unicode`() {
        val text = "${UnicodeEscapeNode.escapeStart}01234567"
        val res = EscapeNode.parse(
            LexemParser(
                CustomStringReader.from(
                    text
                )
            )
        )
        Assertions.assertNotNull(res, text)
        res as EscapeNode

        Assertions.assertEquals(text, res.content)
        Assertions.assertTrue(res.isUnicode)
        Assertions.assertNotNull(res.unicode)
        Assertions.assertNull(res.value)
        Assertions.assertEquals(text, res.toString())
    }

    @Test
    fun `parse correct escape`() {
        for (char in listOf("a", "v", "t", "\n", "'", "\"", WhitespaceNode.complexEndOfLine)) {
            val text = "${EscapeNode.escapeStart}$char"
            val res = EscapeNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as EscapeNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertFalse(res.isUnicode)
            Assertions.assertNull(res.unicode)
            Assertions.assertNotNull(res.value)
            Assertions.assertEquals(char, res.value)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse incorrect escape`() {
        Assertions.assertThrows(AngmarParserException::class.java) {
            val text = EscapeNode.escapeStart
            EscapeNode.parse(LexemParser(CustomStringReader.from(text)))
        }
    }


    @Test
    @Disabled
    fun `test to see the logs`() {
        try {
            // Change this code by one of the tests above that throw an error.
            val text = EscapeNode.escapeStart
            EscapeNode.parse(LexemParser(CustomStringReader.from(text)))
        } catch (e: AngmarParserException) {
            e.logMessage()
        }
    }
}