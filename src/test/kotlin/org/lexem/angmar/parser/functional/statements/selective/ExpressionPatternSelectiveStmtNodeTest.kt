package org.lexem.angmar.parser.functional.statements.selective

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import java.util.stream.*
import kotlin.streams.*

internal class ExpressionPatternSelectiveStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = ExpressionsCommonsTest.testExpression

        @JvmStatic
        private fun provideCorrectExpressionPatterns(): Stream<Arguments> {
            val sequence = sequence {
                for (hasConditional in 0..1) {
                    var text = ExpressionsCommonsTest.testExpression

                    if (hasConditional == 1) {
                        text += " ${ConditionalPatternSelectiveStmtNodeTest.testExpression}"
                    }

                    yield(Arguments.of(text, hasConditional == 1))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ExpressionPatternSelectiveStmtNode,
                    "The node is not a ExpressionPatternSelectiveStmtNode")
            node as ExpressionPatternSelectiveStmtNode

            ExpressionsCommonsTest.checkTestExpression(node.expression)
            Assertions.assertNull(node.conditional, "The conditional property must be null")
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectExpressionPatterns")
    fun `parse correct expression pattern`(text: String, hasConditional: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ExpressionPatternSelectiveStmtNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ExpressionPatternSelectiveStmtNode

        ExpressionsCommonsTest.checkTestExpression(res.expression)

        if (hasConditional) {
            Assertions.assertNotNull(res.conditional, "The conditional property cannot be null")
            ConditionalPatternSelectiveStmtNodeTest.checkTestExpression(res.conditional!!)
        } else {
            Assertions.assertNull(res.conditional, "The conditional property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ExpressionPatternSelectiveStmtNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
