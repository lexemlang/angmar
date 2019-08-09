package org.lexem.angmar.parser.functional.expressions

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class PrefixExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${PrefixOperatorNodeTest.testExpression}${AccessExpressionNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectPrefixExpressions(): Stream<Arguments> {
            val sequence = sequence {
                var text = AccessExpressionNodeTest.testExpression
                yield(Arguments.of(text, false))

                text = "${PrefixOperatorNodeTest.testExpression}${AccessExpressionNodeTest.testExpression}"
                yield(Arguments.of(text, true))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is PrefixExpressionNode, "The node is not a PrefixExpressionNode")
            node as PrefixExpressionNode

            PrefixOperatorNodeTest.checkTestExpression(node.prefix)
            AccessExpressionNodeTest.checkTestExpression(node.element)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectPrefixExpressions")
    fun `parse correct prefix expression`(text: String, hasPrefix: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PrefixExpressionNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        if (!hasPrefix) {
            res as ParserNode

            AccessExpressionNodeTest.checkTestExpression(res)
        } else {
            res as PrefixExpressionNode

            PrefixOperatorNodeTest.checkTestExpression(res.prefix)
            AccessExpressionNodeTest.checkTestExpression(res.element)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect prefix expression with prefix operator but without element`() {
        assertParserException {
            val text = PrefixOperatorNodeTest.testExpression
            val parser = LexemParser(CustomStringReader.from(text))
            PrefixExpressionNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = AccessExpressionNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

