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

internal class AssignOperatorNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = AssignOperatorNode.assignOperator

        @JvmStatic
        private fun provideCorrectOperators(): Stream<Arguments> {
            val result = sequence {
                yield(Arguments.of(AssignOperatorNode.assignOperator))

                for (op in AssignOperatorNode.operators) {
                    yield(Arguments.of(op))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideIncorrectOperators(): Stream<Arguments> {
            val result = sequence {
                for (op in AssignOperatorNode.operators) {
                    // Skip those that are textual
                    if (op.first() in ExpressionsCommons.operatorCharacters) {
                        yield("$op$op")
                    }
                }
            }

            return result.map { Arguments.of(it) }.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is AssignOperatorNode, "The node is not a AssignOperatorNode")
            node as AssignOperatorNode

            Assertions.assertEquals("", node.operator, "The operator parameter is incorrect")
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectOperators")
    fun `parse correct assign operator`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = AssignOperatorNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AssignOperatorNode

        Assertions.assertEquals(text.substring(0 until text.length - 1), res.operator,
                "The operator parameter is incorrect")
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectOperators")
    fun `parse correct assign operator with value`(operator: String) {
        val text = "${operator}125"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = AssignOperatorNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AssignOperatorNode

        Assertions.assertEquals(operator.substring(0 until operator.length - 1), res.operator,
                "The operator parameter is incorrect")
        Assertions.assertEquals(operator.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectOperators")
    fun `parse incorrect assign operator`(text: String) {
        assertParserException {
            val parser = LexemParser(CustomStringReader.from(text))
            AssignOperatorNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = AssignOperatorNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
