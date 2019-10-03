package org.lexem.angmar.parser.functional.statements.selective

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

internal class ConditionalPatternSelectiveStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${ExpressionsCommonsTest.testExpression}"

        @JvmStatic
        private fun provideCorrectConditionalPatterns(): Stream<Arguments> {
            val sequence = sequence {
                for (isUnless in 0..1) {
                    val keyword = if (isUnless == 0) {
                        ConditionalPatternSelectiveStmtNode.ifKeyword
                    } else {
                        ConditionalPatternSelectiveStmtNode.unlessKeyword
                    }

                    val text = "$keyword ${ExpressionsCommonsTest.testExpression}"

                    yield(Arguments.of(text, isUnless == 1))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ConditionalPatternSelectiveStmtNode,
                    "The node is not a ConditionalPatternSelectiveStmtNode")
            node as ConditionalPatternSelectiveStmtNode

            Assertions.assertFalse(node.isUnless, "The isUnless property is incorrect")
            ExpressionsCommonsTest.checkTestExpression(node.condition)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectConditionalPatterns")
    fun `parse correct conditional pattern `(text: String, isUnless: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ConditionalPatternSelectiveStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ConditionalPatternSelectiveStmtNode

        Assertions.assertEquals(isUnless, res.isUnless, "The isUnless property is incorrect")
        ExpressionsCommonsTest.checkTestExpression(res.condition)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect conditional pattern without condition`() {
        TestUtils.assertParserException {
            val text = ConditionalPatternSelectiveStmtNode.ifKeyword
            val parser = LexemParser(CustomStringReader.from(text))
            ConditionalPatternSelectiveStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ConditionalPatternSelectiveStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
