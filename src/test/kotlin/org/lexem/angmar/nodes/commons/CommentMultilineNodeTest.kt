package org.lexem.angmar.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.readers.CustomStringReader

internal class CommentMultilineNodeTest {
    @Test
    fun `parse correct multiline comments`() {
        val testCases =
                listOf("a", " this is a long test in one line ", " this is a\nlong test in\n more than \none line ",
                        " this is a long test in one line ", " this is a\nlong test in\n more than \none line ",
                        " this is a comment finished with one more additional token ${CommentMultilineNode.MultilineAdditionalToken}",
                        " ${CommentMultilineNode.MultilineStartToken} ${CommentMultilineNode.MultilineEndToken} ${CommentMultilineNode.MultilineStartToken}${CommentMultilineNode.MultilineAdditionalToken} ${CommentMultilineNode.MultilineAdditionalToken}${CommentMultilineNode.MultilineEndToken} ${CommentMultilineNode.MultilineAdditionalToken.repeat(
                                3)} ")
        val testCasesRepetition = listOf(0, 0, 0, 1, 2, 2, 3)
        for (test in testCases.zip(testCasesRepetition)) {
            val text =
                    "${CommentMultilineNode.MultilineStartToken}${CommentMultilineNode.MultilineAdditionalToken.repeat(
                            test.second)}${test.first}${CommentMultilineNode.MultilineAdditionalToken.repeat(
                            test.second)}${CommentMultilineNode.MultilineEndToken}"
            val res = CommentMultilineNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, test.first)
            res as CommentMultilineNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(test.first, res.text)
            Assertions.assertEquals(CommentMultilineNode.MultilineAdditionalToken.repeat(test.second),
                    res.additionalTokens)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse empty multiline comments`() {
        Assertions.assertThrows(AngmarParserException::class.java) {
            val text = "${CommentMultilineNode.MultilineStartToken}${CommentMultilineNode.MultilineEndToken}"
            CommentMultilineNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
        }

        Assertions.assertThrows(AngmarParserException::class.java) {
            val text =
                    "${CommentMultilineNode.MultilineStartToken}${CommentMultilineNode.MultilineAdditionalToken}${CommentMultilineNode.MultilineEndToken}"
            CommentMultilineNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
        }
    }

    @Test
    fun `parse unclosed multiline comments`() {
        Assertions.assertThrows(AngmarParserException::class.java) {
            val text = "${CommentMultilineNode.MultilineStartToken} this comment \n is not closed"
            CommentMultilineNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
        }
    }

    @Test
    fun `parse closed multiline comments but with an incorrect number of additional tokens`() {
        Assertions.assertThrows(AngmarParserException::class.java) {
            val text =
                    "${CommentMultilineNode.MultilineStartToken}${CommentMultilineNode.MultilineAdditionalToken.repeat(
                            3)} this comment \n is closed with an incorrect number of ${CommentMultilineNode.MultilineAdditionalToken} ${CommentMultilineNode.MultilineAdditionalToken.repeat(
                            2)}${CommentMultilineNode.MultilineEndToken}"
            CommentMultilineNode.parse(
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
            val text = "${CommentMultilineNode.MultilineStartToken}${CommentMultilineNode.MultilineEndToken}"
            CommentMultilineNode.parse(
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