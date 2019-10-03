package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class CommonsTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testDynamicIdentifier = EscapedExpressionNodeTest.testExpression

        @JvmStatic
        fun provideKeywords(): Stream<Arguments> {
            val sequence = sequence {
                for (key in GlobalCommons.keywords) {
                    yield(Arguments.of(key))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestDynamicIdentifier(node: ParserNode) = EscapedExpressionNodeTest.checkTestExpression(node)
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["012345"])
    fun `parse correct point-like escape`(number: String) {
        val text = "${UnicodeEscapeNode.escapeStart}$number"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = Commons.parseAnyEscape(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeEscapeNode

        Assertions.assertEquals(Converters.hexToDouble(number), res.value, "The value is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["01234567"])
    fun `parse correct unicode-like escape`(number: String) {
        val text = "${PointEscapeNode.escapeStart}$number"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = Commons.parseAnyEscape(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PointEscapeNode

        Assertions.assertEquals(Converters.hexToDouble(number), res.value, "The value is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }


    @ParameterizedTest
    @ValueSource(strings = ["a", "v", "t", "\n", "'", "\"", WhitespaceNode.windowsEndOfLine])
    fun `parse correct escape`(escapeLetter: String) {
        val text = "${EscapeNode.startToken}$escapeLetter"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = Commons.parseAnyEscape(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as EscapeNode

        Assertions.assertEquals(escapeLetter, res.value, "The value is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse correct dynamic identifier - escaped expression`(expression: String) {
        val text = "${EscapedExpressionNode.startToken}$expression${EscapedExpressionNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = Commons.parseDynamicIdentifier(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as EscapedExpressionNode

        ExpressionsCommonsTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["a", "test"])
    fun `parse correct dynamic identifier - simple identifier`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = Commons.parseDynamicIdentifier(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as IdentifierNode

        Assertions.assertFalse(res.isQuotedIdentifier, "The isQuotedIdentifier is not correct")
        Assertions.assertNull(res.quotedIdentifier, "The quotedIdentifier is not correct")
        Assertions.assertEquals(text.split(IdentifierNode.middleChar).size, res.simpleIdentifiers.size,
                "The count of simple identifiers is not correct")

        for (word in text.split(IdentifierNode.middleChar).withIndex()) {
            Assertions.assertEquals(word.value, res.simpleIdentifiers[word.index], "A simple identifier is not correct")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["a", "abc231"])
    fun `check identifier`(identifier: String) {
        val parser = LexemParser(CustomStringReader.from(identifier))
        val res = Commons.checkIdentifier(parser)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser has advanced the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "123", ":s"])
    fun `check bad identifier`(identifier: String) {
        val parser = LexemParser(CustomStringReader.from(identifier))
        val res = Commons.checkIdentifier(parser)

        Assertions.assertFalse(res, "The parser must not capture anything")

        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser has advanced the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideKeywords")
    fun `parse a correct specific keyword`(keyword: String) {
        val parser = LexemParser(CustomStringReader.from(keyword))
        val res = Commons.parseKeyword(parser, keyword)

        Assertions.assertTrue(res, "The parser must capture something")

        Assertions.assertEquals(keyword.length, parser.reader.currentPosition(),
                "The parser has not advanced the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideKeywords")
    fun `parse an incorrect specific keyword`(keyword: String) {
        val parser = LexemParser(CustomStringReader.from("${keyword}a"))
        val res = Commons.parseKeyword(parser, keyword)

        Assertions.assertFalse(res, "The parser must not capture anything")

        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser has advanced the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideKeywords")
    fun `parse any correct keyword`(keyword: String) {
        val parser = LexemParser(CustomStringReader.from(keyword))
        val res = Commons.parseAnyKeyword(parser)

        Assertions.assertEquals(res, keyword, "The parser must capture something")

        Assertions.assertEquals(keyword.length, parser.reader.currentPosition(),
                "The parser has not advanced the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideKeywords")
    fun `parse any incorrect keyword`(keyword: String) {
        val parser = LexemParser(CustomStringReader.from("${keyword}a"))
        val res = Commons.parseAnyKeyword(parser)

        Assertions.assertNull(res, "The parser must not capture anything")

        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser has advanced the cursor")
    }
}