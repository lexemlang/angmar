package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.commons.*
import java.util.stream.*
import kotlin.streams.*

internal class ContinuationLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${ContinuationLexemeNode.token}\n"

        @JvmStatic
        private fun provideContinuations(): Stream<Arguments> {
            val sequence = sequence {
                for (eol in WhitespaceNode.endOfLineChars) {
                    yield("${ContinuationLexemeNode.token}$eol")
                }

                yield("${ContinuationLexemeNode.token}${WhitespaceNode.windowsEndOfLine}")
            }

            return sequence.map { Arguments.of(it) }.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideContinuations")
    fun `parse correct continuation`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PatternWhitespaceNode.parse(parser)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PatternWhitespaceNode.parse(parser)

        Assertions.assertFalse(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

