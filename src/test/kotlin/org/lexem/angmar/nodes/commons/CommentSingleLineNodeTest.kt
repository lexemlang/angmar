package org.lexem.angmar.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader

internal class CommentSingleLineNodeTest {
    @Test
    fun `parse correct single-line comments`() {
        val testCases = listOf("", "a", " this is a long test in one line")
        for (test in testCases) {
            for (eol in WhitespaceNode.endOfLineChars) {
                val text = "${CommentSingleLineNode.SingleLineStartToken}$test$eol"
                val res = CommentSingleLineNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )
                Assertions.assertNotNull(res, text)
                res as CommentSingleLineNode

                Assertions.assertEquals(text, res.content)
                Assertions.assertEquals(test, res.text)
                Assertions.assertEquals(eol.toString(), res.eol)
                Assertions.assertEquals(text, res.toString())
            }

            // End of file
            let {
                val text = "${CommentSingleLineNode.SingleLineStartToken}$test"
                val res = CommentSingleLineNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )
                Assertions.assertNotNull(res, text)
                res as CommentSingleLineNode

                Assertions.assertEquals(text, res.content)
                Assertions.assertEquals(test, res.text)
                Assertions.assertEquals("", res.eol)
                Assertions.assertEquals(text, res.toString())
            }

            // \r\n end of line
            let {
                val text = "${CommentSingleLineNode.SingleLineStartToken}$test\u000D\u000A"
                val res = CommentSingleLineNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )
                Assertions.assertNotNull(res, text)
                res as CommentSingleLineNode

                Assertions.assertEquals(text, res.content)
                Assertions.assertEquals(test, res.text)
                Assertions.assertEquals(WhitespaceNode.complexEndOfLine, res.eol)
                Assertions.assertEquals(text, res.toString())
            }
        }
    }
}