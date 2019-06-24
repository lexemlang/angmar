package org.lexem.angmar.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader
import org.lexem.angmar.nodes.commons.WhitespaceNoEOLNodeTest.Companion.allWhiteCharsTest

internal class WhitespaceUntilEOLNodeTest {
    @Test
    fun `parse correct whitespace until eol`() {
        for (eol in WhitespaceNode.endOfLineChars) {
            val text = "$allWhiteCharsTest$eol"
            val res = WhitespaceUntilEOLNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceUntilEOLNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertNotNull(res.whitespace)
            Assertions.assertEquals(allWhiteCharsTest, res.whitespace!!.content)
            Assertions.assertEquals(eol.toString(), res.eol)
            Assertions.assertEquals(text, res.toString())
        }

        let {
            val text = allWhiteCharsTest
            val res = WhitespaceUntilEOLNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceUntilEOLNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertNotNull(res.whitespace)
            Assertions.assertEquals(allWhiteCharsTest, res.whitespace!!.content)
            Assertions.assertEquals("", res.eol)
            Assertions.assertEquals(text, res.toString())
        }

        let {
            val text = "$allWhiteCharsTest\u000D\u000A"
            val res = WhitespaceUntilEOLNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceUntilEOLNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertNotNull(res.whitespace)
            Assertions.assertEquals(allWhiteCharsTest, res.whitespace!!.content)
            Assertions.assertEquals(WhitespaceNode.complexEndOfLine, res.eol)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse no whitespace until eol`() {
        for (eol in WhitespaceNode.endOfLineChars) {
            val text = "$eol"
            val res = WhitespaceUntilEOLNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceUntilEOLNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertNull(res.whitespace)
            Assertions.assertEquals(eol.toString(), res.eol)
            Assertions.assertEquals(text, res.toString())
        }

        let {
            val text = ""
            val res = WhitespaceUntilEOLNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceUntilEOLNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertNull(res.whitespace)
            Assertions.assertEquals("", res.eol)
            Assertions.assertEquals(text, res.toString())
        }

        let {
            val text = WhitespaceNode.complexEndOfLine
            val res = WhitespaceUntilEOLNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceUntilEOLNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertNull(res.whitespace)
            Assertions.assertEquals(WhitespaceNode.complexEndOfLine, res.eol)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse incorrect whitespace until eol`() {
        val text = "${allWhiteCharsTest}a"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = WhitespaceUntilEOLNode.parse(parser)

        Assertions.assertNull(res, text)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }
}