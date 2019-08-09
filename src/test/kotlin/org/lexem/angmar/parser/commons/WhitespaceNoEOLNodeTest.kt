package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import java.util.stream.*
import kotlin.streams.*

internal class WhitespaceNoEOLNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        val allWhiteCharsTest = StringBuilder().apply {
            for (c in WhitespaceNode.whitespaceChars) {
                append(c)
            }
        }.toString()

        @JvmStatic
        private fun provideWsNoComments(): Stream<Arguments> {
            val sequence = sequence {
                for (c in WhitespaceNode.whitespaceChars) {
                    yield("$c")
                }

                yield(allWhiteCharsTest)
            }


            return sequence.map { Arguments.of(it) }.asStream()
        }

        @JvmStatic
        private fun provideWsWithComments(): Stream<Arguments> {
            val sequence = sequence {
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

                    yield(text)
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

                    yield(text)
                }
            }


            return sequence.map { Arguments.of(it) }.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideWsNoComments")
    fun `parse correct whitespace without eol and comments`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = WhitespaceNoEOLNode.parse(parser)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideWsWithComments")
    fun `parse correct whitespace without eol but with comments`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = WhitespaceNoEOLNode.parse(parser)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "text"])
    fun `parse no whitespace`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = WhitespaceNoEOLNode.parse(parser)

        Assertions.assertFalse(res, "The parser must not capture anything")

        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser has advanced the cursor")
    }
}
