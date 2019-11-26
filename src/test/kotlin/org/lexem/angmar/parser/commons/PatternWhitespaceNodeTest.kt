package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import java.util.stream.*

internal class PatternWhitespaceNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun provideWhitespaces(): Stream<Arguments> {
            return Stream.concat(WhitespaceNoEOLNodeTest.provideWsNoComments(),
                    WhitespaceNoEOLNodeTest.provideWsWithComments())
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideWhitespaces")
    fun `parse correct whitespaces`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PatternWhitespaceNode.parse(parser)

        Assertions.assertTrue(res, "The parser must capture something")
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct continuation`() {
        val text = ContinuationLexemeNodeTest.testExpression
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
