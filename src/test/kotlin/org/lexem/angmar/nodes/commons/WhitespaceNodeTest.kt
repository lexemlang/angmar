package org.lexem.angmar.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader

internal class WhitespaceNodeTest {
    private val multilineComment =
            "${CommentMultilineNode.MultilineStartToken} comment\nmultiline ${CommentMultilineNode.MultilineEndToken}"
    private val singleLineComment = "${CommentSingleLineNode.SingleLineStartToken} comment"
    private val allWhiteCharsTest = StringBuilder().apply {
        for (c in WhitespaceNode.whitespaceChars) {
            append(c)
        }

        for (c in WhitespaceNode.endOfLineChars) {
            append(c)
        }
    }.toString()

    @Test
    fun `parse correct whitespaces with end-of-lines`() {
        for (c in WhitespaceNode.whitespaceChars.asSequence() + WhitespaceNode.endOfLineChars.asSequence()) {
            val text = "$c"
            val res = WhitespaceNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(1, res.whitespaces.size)
            Assertions.assertEquals(0, res.comments.size)
            Assertions.assertEquals(text, res.whitespaces[0])
            Assertions.assertEquals(text, res.toString())
        }

        val text = allWhiteCharsTest
        val res = WhitespaceNode.parse(
            LexemParser(
                CustomStringReader.from(
                    text
                )
            )
        )
        Assertions.assertNotNull(res, text)
        res as WhitespaceNode

        Assertions.assertEquals(text, res.content)
        Assertions.assertEquals(1, res.whitespaces.size)
        Assertions.assertEquals(0, res.comments.size)
        Assertions.assertEquals(text, res.whitespaces[0])
        Assertions.assertEquals(text, res.toString())
    }

    @Test
    fun `parse correct comments`() {
        let {
            val text = multilineComment
            val res = WhitespaceNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(2, res.whitespaces.size)
            Assertions.assertEquals(1, res.comments.size)

            for (ws in res.whitespaces) {
                Assertions.assertEquals("", ws)
            }

            Assertions.assertEquals(text, res.comments[0].content)
            Assertions.assertEquals(text, res.toString())
        }

        let {
            val text = singleLineComment
            val res = WhitespaceNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as WhitespaceNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(2, res.whitespaces.size)
            Assertions.assertEquals(1, res.comments.size)

            for (ws in res.whitespaces) {
                Assertions.assertEquals("", ws)
            }

            Assertions.assertEquals(text, res.comments[0].content)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse correct complex whitespaces`() {
        val text = "$multilineComment$singleLineComment\n$multilineComment$allWhiteCharsTest"
        val res = WhitespaceNode.parse(
            LexemParser(
                CustomStringReader.from(
                    text
                )
            )
        )
        Assertions.assertNotNull(res, text)
        res as WhitespaceNode

        Assertions.assertEquals(text, res.content)
        Assertions.assertEquals(4, res.whitespaces.size)
        Assertions.assertEquals(3, res.comments.size)

        Assertions.assertEquals("", res.whitespaces[0])
        Assertions.assertEquals("", res.whitespaces[1])
        Assertions.assertEquals("", res.whitespaces[2])
        Assertions.assertEquals(allWhiteCharsTest, res.whitespaces[3])
        Assertions.assertEquals(multilineComment, res.comments[0].content)
        Assertions.assertEquals("$singleLineComment\n", res.comments[1].content)
        Assertions.assertEquals(multilineComment, res.comments[2].content)
        Assertions.assertEquals(text, res.toString())
    }
}