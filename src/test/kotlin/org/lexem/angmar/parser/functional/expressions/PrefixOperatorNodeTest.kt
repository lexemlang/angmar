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

internal class PrefixOperatorNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = PrefixOperatorNode.bitwiseNegationOperator

        @JvmStatic
        private fun provideCorrectOperators(): Stream<Arguments> {
            val result = sequence {
                for (op in PrefixOperatorNode.operators) {
                    yield(op)
                }
            }

            return result.map { Arguments.of(it) }.asStream()
        }

        @JvmStatic
        private fun provideIncorrectOperators(): Stream<Arguments> {
            val result = sequence {
                for (op in PrefixOperatorNode.operators) {
                    yield("$op$op")
                    yield("$op$op$op")
                }
            }

            return result.map { Arguments.of(it) }.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is PrefixOperatorNode, "The node is not a PrefixOperatorNode")
            node as PrefixOperatorNode

            Assertions.assertEquals(testExpression, node.operator, "The operator parameter is incorrect")
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectOperators")
    fun `parse correct prefix operator`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PrefixOperatorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PrefixOperatorNode

        Assertions.assertEquals(text, res.operator, "The operator parameter is incorrect")
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectOperators")
    fun `parse correct prefix operator with value`(operator: String) {
        val text = "${operator}125"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PrefixOperatorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PrefixOperatorNode

        Assertions.assertEquals(text.substring(0, text.length - 3), res.operator, "The operator parameter is incorrect")
        Assertions.assertEquals(operator.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectOperators")
    fun `parse incorrect assign operator`(text: String) {
        TestUtils.assertParserException {
            val parser = LexemParser(CustomStringReader.from(text))
            PrefixOperatorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PrefixOperatorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
