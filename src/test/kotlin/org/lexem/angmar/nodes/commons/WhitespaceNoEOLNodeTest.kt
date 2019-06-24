package org.lexem.angmar.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader

internal class WhitespaceNoEOLNodeTest {
    @Test
    fun `parse correct whitespace without eol and comments`() {
        for (c in WhitespaceNode.whitespaceChars) {
            val text = "$c"
            val res = WhitespaceNoEOLNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceNoEOLNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(1, res.whitespaces.size)
            Assertions.assertEquals(0, res.comments.size)
            Assertions.assertEquals(text, res.whitespaces[0])
            Assertions.assertEquals(text, res.toString())
        }

        val text = allWhiteCharsTest
        val res = WhitespaceNoEOLNode.parse(
            LexemParser(
                CustomStringReader.from(
                    text
                )
            )
        )
        Assertions.assertNotNull(res, text)
        res as WhitespaceNoEOLNode

        Assertions.assertEquals(text, res.content)
        Assertions.assertEquals(1, res.whitespaces.size)
        Assertions.assertEquals(0, res.comments.size)
        Assertions.assertEquals(text, res.whitespaces[0])
        Assertions.assertEquals(text, res.toString())
    }

    @Test
    fun `parse correct whitespace without eol but with comments`() {
        val comment =
                "${CommentMultilineNode.MultilineStartToken} comment\nmultiline ${CommentMultilineNode.MultilineEndToken}"

        // First comment
        for (i in 1..5) {
            val text = StringBuilder().apply {
                repeat(i) {
                    if (it % 2 == 0) {
                        append(comment)
                    } else {
                        append(allWhiteCharsTest)
                    }
                }
            }.toString()

            val res = WhitespaceNoEOLNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceNoEOLNode

            val commentSize = Math.ceil(i / 2.0).toInt()
            val whiteSize = commentSize + 1

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(whiteSize, res.whitespaces.size)
            Assertions.assertEquals(commentSize, res.comments.size)

            Assertions.assertEquals("", res.whitespaces[0])
            for (ws in res.whitespaces.asSequence().drop(1).take(res.whitespaces.size - 2)) {
                Assertions.assertEquals(allWhiteCharsTest, ws)
            }

            if (i % 2 == 0) {
                Assertions.assertEquals(allWhiteCharsTest, res.whitespaces.last())
            } else {
                Assertions.assertEquals("", res.whitespaces.last())
            }

            for (cmt in res.comments) {
                Assertions.assertEquals(comment, cmt.content)
            }

            Assertions.assertEquals(text, res.toString())
        }

        // First whitespace
        for (i in 1..5) {
            val text = StringBuilder().apply {
                repeat(i) {
                    if (it % 2 == 0) {
                        append(allWhiteCharsTest)
                    } else {
                        append(comment)
                    }
                }
            }.toString()

            val res = WhitespaceNoEOLNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceNoEOLNode

            val commentSize = Math.ceil((i - 1) / 2.0).toInt()
            val whiteSize = commentSize + 1

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(whiteSize, res.whitespaces.size)
            Assertions.assertEquals(commentSize, res.comments.size)

            Assertions.assertEquals(allWhiteCharsTest, res.whitespaces[0])
            for (ws in res.whitespaces.asSequence().drop(1).take(Math.max(0, res.whitespaces.size - 2))) {
                Assertions.assertEquals(allWhiteCharsTest, ws)
            }

            if (i % 2 == 0) {
                Assertions.assertEquals("", res.whitespaces.last())
            } else {
                Assertions.assertEquals(allWhiteCharsTest, res.whitespaces.last())
            }

            for (cmt in res.comments) {
                Assertions.assertEquals(comment, cmt.content)
            }

            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse no whitespace`() {
        val text = ""
        val parser = LexemParser(CustomStringReader.from(text))
        val res = WhitespaceNoEOLNode.parse(parser)

        Assertions.assertNull(res, text)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun `parse no whitespace with parseOrEmpty`() {
        val text = ""
        val parser = LexemParser(CustomStringReader.from(text))
        val res = WhitespaceNoEOLNode.parseOrEmpty(parser)

        Assertions.assertEquals(text, res.content)
        Assertions.assertEquals(0, res.whitespaces.size)
        Assertions.assertEquals(0, res.comments.size)
        Assertions.assertEquals(text, res.toString())
    }

    companion object {
        val allWhiteCharsTest = StringBuilder().apply {
            for (c in WhitespaceNode.whitespaceChars) {
                append(c)
            }
        }.toString()
    }
}