package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import java.util.stream.*
import kotlin.streams.*

internal class WhitespaceEOLNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        private val allWhiteCharsTest = StringBuilder().apply {
            for (c in WhitespaceNode.whitespaceChars) {
                append(c)
            }
        }.toString()

        @JvmStatic
        private fun provideWsUntilEOL(): Stream<Arguments> {
            val sequence = sequence {
                for (eol in WhitespaceNode.endOfLineChars) {
                    yield("$allWhiteCharsTest$eol")
                }

                yield(allWhiteCharsTest)


                yield("$allWhiteCharsTest\u000D\u000A")
            }


            return sequence.map { Arguments.of(it) }.asStream()
        }

        @JvmStatic
        fun provideEOL(): Stream<Arguments> {
            val sequence = sequence {
                for (eol in WhitespaceNode.endOfLineChars) {
                    yield("$eol")
                }

                yield("")

                yield(WhitespaceNode.windowsEndOfLine)
            }


            return sequence.map { Arguments.of(it) }.asStream()
        }

        @JvmStatic
        private fun provideWsNoEOL(): Stream<Arguments> {
            val sequence = sequence {
                yield("${allWhiteCharsTest}a")
            }


            return sequence.map { Arguments.of(it) }.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideWsUntilEOL")
    fun `parse correct whitespace until eol`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = WhitespaceEOLNode.parse(parser)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideEOL")
    fun `parse no whitespace until eol`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = WhitespaceEOLNode.parse(parser)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideWsNoEOL")
    fun `parse incorrect whitespace until eol`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = WhitespaceEOLNode.parse(parser)

        Assertions.assertFalse(res, "The parser must not capture anything")

        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser has advanced the cursor")
    }
}
