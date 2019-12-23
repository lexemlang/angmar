package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class UnicodeIntervalElementNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val charValue = 'a'
        const val testExpression = "$charValue${IntervalElementNode.rangeToken}$charValue"

        @JvmStatic
        private fun provideSimpleIntervals(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of("$charValue", true))
                yield(Arguments.of(EscapedExpressionNodeTest.testExpression, false))
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideFullIntervals(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of("$charValue${IntervalElementNode.rangeToken}$charValue", true, true))
                yield(Arguments.of(
                        "${EscapedExpressionNodeTest.testExpression}${IntervalElementNode.rangeToken}$charValue", false,
                        true))
                yield(Arguments.of(
                        "$charValue${IntervalElementNode.rangeToken}${EscapedExpressionNodeTest.testExpression}", true,
                        false))
                yield(Arguments.of(
                        "${EscapedExpressionNodeTest.testExpression}${IntervalElementNode.rangeToken}${EscapedExpressionNodeTest.testExpression}",
                        false, false))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is UnicodeIntervalElementNode, "The node is not a unicodeIntervalElementNode")
            node as UnicodeIntervalElementNode

            Assertions.assertEquals(charValue, node.leftChar, "The leftChar property is incorrect")
            Assertions.assertNull(node.left, "The left property must be null")
            Assertions.assertEquals(charValue, node.rightChar, "The rightChar property is incorrect")
            Assertions.assertNull(node.right, "The right property must be null")
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideSimpleIntervals")
    fun `parse correct unicode interval element`(text: String, isLeftChar: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = UnicodeIntervalElementNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeIntervalElementNode

        if (isLeftChar) {
            Assertions.assertEquals(charValue, res.leftChar, "The leftChar property is incorrect")
            Assertions.assertNull(res.left, "The left property must be null")
        } else {
            Assertions.assertEquals(' ', res.leftChar, "The leftChar property is incorrect")
            Assertions.assertNotNull(res.left, "The left property cannot be null")
            EscapedExpressionNodeTest.checkTestExpression(res.left!!)
        }

        Assertions.assertEquals(' ', res.rightChar, "The rightChar property is incorrect")
        Assertions.assertNull(res.right, "The right property must be null")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideFullIntervals")
    fun `parse correct full unicode interval element`(text: String, isLeftChar: Boolean, isRightChar: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = UnicodeIntervalElementNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeIntervalElementNode

        if (isLeftChar) {
            Assertions.assertEquals(charValue, res.leftChar, "The leftChar property is incorrect")
            Assertions.assertNull(res.left, "The left property must be null")
        } else {
            Assertions.assertEquals(' ', res.leftChar, "The leftChar property is incorrect")
            Assertions.assertNotNull(res.left, "The left property cannot be null")
            EscapedExpressionNodeTest.checkTestExpression(res.left!!)
        }

        if (isRightChar) {
            Assertions.assertEquals(charValue, res.rightChar, "The rightChar property is incorrect")
            Assertions.assertNull(res.right, "The right property must be null")
        } else {
            Assertions.assertEquals(' ', res.rightChar, "The rightChar property is incorrect")
            Assertions.assertNotNull(res.right, "The right property cannot be null")
            EscapedExpressionNodeTest.checkTestExpression(res.right!!)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["", "[", "]", " ", "\n"])
    fun `parse incorrect unicode interval element with incorrect close element`(endValue: String) {
        TestUtils.assertParserException(
                AngmarParserExceptionType.UnicodeIntervalElementWithoutElementAfterRangeOperator) {
            val text = "3${IntervalElementNode.rangeToken}$endValue"
            val parser = LexemParser(IOStringReader.from(text))
            UnicodeIntervalElementNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "\n", "["])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = UnicodeIntervalElementNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

