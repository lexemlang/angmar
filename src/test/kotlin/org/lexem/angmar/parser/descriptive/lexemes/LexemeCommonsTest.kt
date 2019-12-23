package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*
import java.util.stream.*
import kotlin.streams.*

internal class LexemeCommonsTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testAnchors = RelativeAnchorLexemeNodeTest.testExpression

        @JvmStatic
        private fun provideAnchors(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of(RelativeAnchorLexemeNodeTest.testExpression, 0))
                yield(Arguments.of(AbsoluteAnchorLexemeNodeTest.testExpression, 1))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestAnchors(node: ParserNode) = RelativeAnchorLexemeNodeTest.checkTestExpression(node)
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideAnchors")
    fun `parse correct anchor lexemes`(text: String, type: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = LexemeCommons.parseAnyAnchorLexeme(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> RelativeAnchorLexemeNodeTest.checkTestExpression(res)
            1 -> AbsoluteAnchorLexemeNodeTest.checkTestExpression(res)
            else -> throw AngmarUnreachableException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }
}

