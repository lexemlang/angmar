package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class IntervalNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${IntervalNode.macroName}${IntervalNode.startToken}${IntervalElementNodeTest.testExpression}${IntervalNode.endToken}"

        @JvmStatic
        private fun provideSubIntervals(): Stream<Arguments> {
            val sequence = sequence {
                for (exp in listOf(false, true)) {
                    val expression = if (!exp) {
                        IntervalElementNodeTest.testExpression
                    } else {
                        IntervalSubIntervalNodeTest.testExpression
                    }

                    for (i in 0..3) {
                        val list = List(i) { expression }.joinToString(" ")

                        for (reversed in listOf(false, true)) {
                            val reverseText = if (!reversed) {
                                ""
                            } else {
                                IntervalNode.reversedToken
                            }

                            yield(Arguments.of(
                                    "${IntervalNode.macroName}${IntervalNode.startToken}$reverseText$list${IntervalNode.endToken}",
                                    reversed, i, exp))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is IntervalNode, "The node is not an IntervalNode")
            node as IntervalNode

            Assertions.assertFalse(node.reversed, "The reversed property in incorrect")
            Assertions.assertEquals(1, node.elements.size, "The number of elements is incorrect")

            IntervalElementNodeTest.checkTestExpression(node.elements.first())
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideSubIntervals")
    fun `parse correct interval`(text: String, reversed: Boolean, numElements: Int,
            areElementsOfSubIntervals: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = IntervalNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as IntervalNode

        Assertions.assertEquals(reversed, res.reversed, "The reversed property in incorrect")
        Assertions.assertEquals(numElements, res.elements.size, "The number of elements is incorrect")

        for (el in res.elements) {
            if (areElementsOfSubIntervals) {
                IntervalSubIntervalNodeTest.checkTestExpression(el)
            } else {
                IntervalElementNodeTest.checkTestExpression(el)
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect interval without startToken`() {
        TestUtils.assertParserException(AngmarParserExceptionType.IntervalWithoutStartToken) {
            val text = IntervalNode.macroName
            val parser = LexemParser(IOStringReader.from(text))
            IntervalNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect interval without endToken`() {
        TestUtils.assertParserException(AngmarParserExceptionType.IntervalWithoutEndToken) {
            val text = "${IntervalNode.macroName}${IntervalNode.startToken}"
            val parser = LexemParser(IOStringReader.from(text))
            IntervalNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "\n"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = IntervalNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

