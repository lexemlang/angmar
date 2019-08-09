package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class IntervalElementNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${EscapedExpressionNode.startToken}${ExpressionsCommonsTest.testExpression}${EscapedExpressionNode.endToken}"

        @JvmStatic
        private fun provideSimpleIntervals(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of(NumberNodeTest.testExpression, true))
                yield(Arguments.of(EscapedExpressionNodeTest.testExpression, false))
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideFullIntervals(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of(
                        "${NumberNodeTest.testExpression}${IntervalElementNode.rangeToken}${NumberNodeTest.testExpression}",
                        true, true))
                yield(Arguments.of(
                        "${EscapedExpressionNodeTest.testExpression}${IntervalElementNode.rangeToken}${NumberNodeTest.testExpression}",
                        false, true))
                yield(Arguments.of(
                        "${NumberNodeTest.testExpression}${IntervalElementNode.rangeToken}${EscapedExpressionNodeTest.testExpression}",
                        true, false))
                yield(Arguments.of(
                        "${EscapedExpressionNodeTest.testExpression}${IntervalElementNode.rangeToken}${EscapedExpressionNodeTest.testExpression}",
                        false, false))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is IntervalElementNode, "The node is not a IntervalElementNode")
            node as IntervalElementNode

            EscapedExpressionNodeTest.checkTestExpression(node.left)
            Assertions.assertNull(node.right, "The right property must be null")

        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideSimpleIntervals")
    fun `parse correct interval element`(text: String, isLeftNumber: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = IntervalElementNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as IntervalElementNode

        if (isLeftNumber) {
            NumberNodeTest.checkTestExpression(res.left)
        } else {
            EscapedExpressionNodeTest.checkTestExpression(res.left)
        }
        Assertions.assertNull(res.right, "The right property must be null")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideFullIntervals")
    fun `parse correct full interval element`(text: String, isLeftNumber: Boolean, isRightNumber: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = IntervalElementNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as IntervalElementNode

        if (isLeftNumber) {
            NumberNodeTest.checkTestExpression(res.left)
        } else {
            EscapedExpressionNodeTest.checkTestExpression(res.left)
        }

        Assertions.assertNotNull(res.right, "The right property cannot be null")
        if (isRightNumber) {
            NumberNodeTest.checkTestExpression(res.right!!)
        } else {
            EscapedExpressionNodeTest.checkTestExpression(res.right!!)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect interval element without right element`() {
        assertParserException {
            val text = "3${IntervalElementNode.rangeToken}"
            val parser = LexemParser(CustomStringReader.from(text))
            IntervalElementNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = IntervalElementNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
