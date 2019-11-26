package org.lexem.angmar.parser.descriptive

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import java.util.stream.*
import kotlin.streams.*

internal class LexemPatternContentNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = GlobalCommonsTest.testLexeme

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (lexemeCount in 1..5) {
                    val list = List(lexemeCount) { GlobalCommonsTest.testLexeme }

                    yield(Arguments.of(list.joinToString(" "), lexemeCount))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as LexemePatternContentNode

            Assertions.assertEquals(1, node.lexemes.size, "The lexeme count is incorrect")

            node.lexemes.forEach {
                GlobalCommonsTest.checkTestLexeme(it)
            }
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct node`(text: String, lexemeCount: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = LexemePatternContentNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LexemePatternContentNode

        Assertions.assertEquals(lexemeCount, res.lexemes.size, "The lexeme count is incorrect")

        res.lexemes.forEach {
            GlobalCommonsTest.checkTestLexeme(it)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = LexemePatternContentNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
