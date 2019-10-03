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

internal class UnicodeIntervalSubIntervalNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${UnicodeIntervalSubIntervalNode.startToken}${UnicodeIntervalElementNodeTest.testExpression}${UnicodeIntervalSubIntervalNode.endToken}"

        @JvmStatic
        private fun provideSubIntervals(): Stream<Arguments> {
            val sequence = sequence {
                for (exp in 0..1) {
                    val expression = if (exp == 0) {
                        UnicodeIntervalElementNodeTest.testExpression
                    } else {
                        testExpression
                    }

                    for (i in 0..3) {
                        val list = List(i) { expression }.joinToString(" ")

                        yield(Arguments.of(
                                "${UnicodeIntervalSubIntervalNode.startToken}$list${UnicodeIntervalSubIntervalNode.endToken}",
                                IntervalSubIntervalNode.Operator.Add, false, i, exp == 1))

                        for (op in IntervalSubIntervalNode.Operator.values()) {
                            for (reversed in 0..1) {
                                val reverseText = if (reversed == 0) {
                                    ""
                                } else {
                                    UnicodeIntervalSubIntervalNode.reversedToken
                                }

                                yield(Arguments.of(
                                        "${UnicodeIntervalSubIntervalNode.startToken}${op.operator}$reverseText$list${UnicodeIntervalSubIntervalNode.endToken}",
                                        op, reversed == 1, i, exp == 1))
                            }
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is UnicodeIntervalSubIntervalNode,
                    "The node is not an UnicodeIntervalSubIntervalNode")
            node as UnicodeIntervalSubIntervalNode

            Assertions.assertEquals(IntervalSubIntervalNode.Operator.Add, node.operator,
                    "The operator property is incorrect")
            Assertions.assertFalse(node.reversed, "The reversed property in incorrect")
            Assertions.assertEquals(1, node.elements.size, "The number of elements is incorrect")

            UnicodeIntervalElementNodeTest.checkTestExpression(node.elements.first())
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideSubIntervals")
    fun `parse correct unicode sub interval element`(text: String, operator: IntervalSubIntervalNode.Operator,
            reversed: Boolean, numElements: Int, areElementsOfSubIntervals: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnicodeIntervalSubIntervalNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as UnicodeIntervalSubIntervalNode

        Assertions.assertEquals(operator, res.operator, "The operator property is incorrect")
        Assertions.assertEquals(reversed, res.reversed, "The reversed property in incorrect")
        Assertions.assertEquals(numElements, res.elements.size, "The number of elements is incorrect")

        for (el in res.elements) {
            if (areElementsOfSubIntervals) {
                checkTestExpression(el)
            } else {
                UnicodeIntervalElementNodeTest.checkTestExpression(el)
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect unicode sub interval element without right element`() {
        TestUtils.assertParserException {
            val text = "${UnicodeIntervalSubIntervalNode.startToken}a"
            val parser = LexemParser(CustomStringReader.from(text))
            UnicodeIntervalSubIntervalNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "\n"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = UnicodeIntervalElementNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

