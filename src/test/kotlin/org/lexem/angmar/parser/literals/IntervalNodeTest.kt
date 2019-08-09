package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
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
                for (exp in 0..1) {
                    val expression = if (exp == 0) {
                        IntervalElementNodeTest.testExpression
                    } else {
                        IntervalSubIntervalNodeTest.testExpression
                    }

                    for (i in 0..3) {
                        val list = List(i) { expression }.joinToString(" ")

                        for (reversed in 0..1) {
                            val reverseText = if (reversed == 0) {
                                ""
                            } else {
                                IntervalNode.reversedToken
                            }

                            yield(Arguments.of(
                                    "${IntervalNode.macroName}${IntervalNode.startToken}$reverseText$list${IntervalNode.endToken}",
                                    reversed == 1, i, exp == 1))
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
        val parser = LexemParser(CustomStringReader.from(text))
        val res = IntervalNode.parse(parser)

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
        assertParserException {
            val text = IntervalNode.macroName
            val parser = LexemParser(CustomStringReader.from(text))
            IntervalNode.parse(parser)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect interval without endToken`() {
        assertParserException {
            val text = "${IntervalNode.macroName}${IntervalNode.startToken}"
            val parser = LexemParser(CustomStringReader.from(text))
            IntervalNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "\n"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = IntervalNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

