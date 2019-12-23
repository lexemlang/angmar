package org.lexem.angmar.parser.functional.statements.controls

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import java.util.stream.*
import kotlin.streams.*

internal class ControlWithoutExpressionStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = ControlWithoutExpressionStmtNode.restartKeyword

        @JvmStatic
        private fun provideCorrectControlWithoutExpression(): Stream<Arguments> {
            val sequence = sequence {
                val keywords = listOf(ControlWithoutExpressionStmtNode.exitKeyword,
                        ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword,
                        ControlWithoutExpressionStmtNode.restartKeyword)

                for (keyword in keywords) {
                    yield(Arguments.of(keyword, keyword, false))
                    yield(Arguments.of("$keyword${GlobalCommons.tagPrefix}${IdentifierNodeTest.testExpression}",
                            keyword, true))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ControlWithoutExpressionStmtNode,
                    "The node is not a ControlWithoutExpressionStmtNode")
            node as ControlWithoutExpressionStmtNode

            Assertions.assertEquals(ControlWithoutExpressionStmtNode.restartKeyword, node.keyword,
                    "The keyword property is incorrect")
            Assertions.assertNull(node.tag, "The tag property must be null")
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectControlWithoutExpression")
    fun `parse correct destructuring spread statement`(text: String, keyword: String, hasTag: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ControlWithoutExpressionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, keyword)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ControlWithoutExpressionStmtNode

        Assertions.assertEquals(keyword, res.keyword, "The keyword property is incorrect")
        if (hasTag) {
            Assertions.assertNotNull(res.tag, "The tag property cannot be null")
            IdentifierNodeTest.checkTestExpression(res.tag!!)
        } else {
            Assertions.assertNull(res.tag, "The tag property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ControlWithoutExpressionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode,
                ControlWithoutExpressionStmtNode.restartKeyword)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
