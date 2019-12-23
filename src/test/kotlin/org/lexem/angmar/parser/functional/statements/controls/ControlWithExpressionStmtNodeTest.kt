package org.lexem.angmar.parser.functional.statements.controls

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class ControlWithExpressionStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ControlWithExpressionStmtNode.returnKeyword} ${ExpressionsCommonsTest.testExpression}"

        @JvmStatic
        private fun provideCorrectControlWithExpression(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of(
                        "${ControlWithExpressionStmtNode.returnKeyword} ${ExpressionsCommonsTest.testExpression}",
                        false))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ControlWithExpressionStmtNode,
                    "The node is not a ControlWithExpressionStmtNode")
            node as ControlWithExpressionStmtNode

            Assertions.assertEquals(ControlWithExpressionStmtNode.returnKeyword, node.keyword,
                    "The keyword property is incorrect")
            Assertions.assertNull(node.tag, "The tag property must be null")
            ExpressionsCommonsTest.checkTestExpression(node.expression)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectControlWithExpression")
    fun `parse correct destructuring spread statement`(text: String, hasTag: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ControlWithExpressionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode,
                ControlWithExpressionStmtNode.returnKeyword, false)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ControlWithExpressionStmtNode

        Assertions.assertEquals(ControlWithExpressionStmtNode.returnKeyword, res.keyword,
                "The keyword property is incorrect")
        Assertions.assertNull(res.tag, "The tag property must be null")
        ExpressionsCommonsTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect control without expression`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ControlWithExpressionStatementWithoutExpression) {
            val text = ControlWithExpressionStmtNode.returnKeyword
            val parser = LexemParser(IOStringReader.from(text))
            ControlWithExpressionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode,
                    ControlWithExpressionStmtNode.returnKeyword)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ControlWithExpressionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode,
                ControlWithExpressionStmtNode.returnKeyword)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

