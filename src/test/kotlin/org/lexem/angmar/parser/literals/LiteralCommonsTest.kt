package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import java.util.stream.*
import kotlin.streams.*

internal class LiteralCommonsTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testAnyString = StringNodeTest.testExpression
        const val testAnyObject = ObjectNodeTest.testExpression
        const val testAnyInterval = IntervalNodeTest.testExpression

        @JvmStatic
        private fun provideObjects(): Stream<Arguments> {
            val sequence = sequence {
                val tests = listOf(ObjectNodeTest.testExpression, PropertyStyleObjectNodeTest.testExpression)

                for (test in tests.withIndex()) {
                    yield(Arguments.of(test.value, test.index))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideIntervals(): Stream<Arguments> {
            val sequence = sequence {
                val tests = listOf(IntervalNodeTest.testExpression, UnicodeIntervalNodeTest.testExpression)

                for (test in tests.withIndex()) {
                    yield(Arguments.of(test.value, test.index))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestAnyString(node: ParserNode) = StringNodeTest.checkTestExpression(node)
        fun checkTestAnyObject(node: ParserNode) = ObjectNodeTest.checkTestExpression(node)
        fun checkTestAnyInterval(node: ParserNode) = IntervalNodeTest.checkTestExpression(node)
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(
            strings = ["a", "this", "longText_with_a lotOfThings", "texts with\n break lines", "text with ${EscapeNode.startToken}d escapes", "${StringNode.additionalDelimiter}${StringNode.additionalDelimiter}"])
    fun `parse simple literal correct strings`(textContent: String) {
        val text = "${UnescapedStringNode.macroName}${StringNode.startToken}$textContent${StringNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = LiteralCommons.parseAnyString(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnescapedStringNode

        Assertions.assertEquals(text, res.content, "The content is not correct")
        Assertions.assertEquals(0, res.minAdditionalDelimiterRequired,
                "The minAdditionalDelimiterRequired is not correct")
        Assertions.assertEquals(textContent, res.text, "The text is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["a", "this", "longText_with_a lotOfThings", "texts with\n break lines", "${StringNode.additionalDelimiter}${StringNode.additionalDelimiter}"])
    fun `parse simple correct strings`(textContent: String) {
        val text = "${StringNode.startToken}$textContent${StringNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = LiteralCommons.parseAnyString(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as StringNode

        Assertions.assertEquals(text, res.content, "The content is not correct")
        Assertions.assertEquals(0, res.minAdditionalDelimiterRequired,
                "The minAdditionalDelimiterRequired is not correct")
        Assertions.assertEquals(1, res.texts.size, "The text list size is not correct")
        Assertions.assertEquals(0, res.escapes.size, "The escapes list size is not correct")
        Assertions.assertEquals(textContent, res.texts.first(), "The first text is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideObjects")
    fun `parse any correct object`(text: String, type: Int) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = LiteralCommons.parseAnyObject(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> ObjectNodeTest.checkTestExpression(res)
            1 -> PropertyStyleObjectNodeTest.checkTestExpression(res)
            else -> throw AngmarUnimplementedException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideIntervals")
    fun `parse any correct interval`(text: String, type: Int) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = LiteralCommons.parseAnyInterval(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> IntervalNodeTest.checkTestExpression(res)
            1 -> UnicodeIntervalNodeTest.checkTestExpression(res)
            else -> throw AngmarUnimplementedException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }
}
