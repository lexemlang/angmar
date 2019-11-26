package org.lexem.angmar.parser.functional.statements.selective

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class VarPatternSelectiveStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${VarPatternSelectiveStmtNode.variableKeyword} ${CommonsTest.testDynamicIdentifier}"

        @JvmStatic
        private fun provideCorrectVariablePatterns(): Stream<Arguments> {
            val sequence = sequence {
                for (isConst in listOf(false, true)) {
                    val keyword = if (!isConst) {
                        VarPatternSelectiveStmtNode.variableKeyword
                    } else {
                        VarPatternSelectiveStmtNode.constKeyword
                    }

                    for (isDestructuring in listOf(false, true)) {
                        val identifier = if (!isDestructuring) {
                            CommonsTest.testDynamicIdentifier
                        } else {
                            DestructuringStmtNodeTest.testExpression
                        }

                        for (hasConditional in listOf(false, true)) {
                            var text = "$keyword $identifier"

                            if (hasConditional) {
                                text += " ${ConditionalPatternSelectiveStmtNodeTest.testExpression}"
                            }

                            yield(Arguments.of(text, isConst, isDestructuring, hasConditional))

                            // Without whitespace
                            text = "$keyword $identifier"

                            if (hasConditional) {
                                text += ConditionalPatternSelectiveStmtNodeTest.testExpression
                            }

                            yield(Arguments.of(text, isConst, isDestructuring, hasConditional))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is VarPatternSelectiveStmtNode, "The node is not a VarPatternSelectiveStmtNode")
            node as VarPatternSelectiveStmtNode

            Assertions.assertFalse(node.isConstant, "The isConstant property is incorrect")
            CommonsTest.checkTestDynamicIdentifier(node.identifier)
            Assertions.assertNull(node.conditional, "The conditional property must be null")
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectVariablePatterns")
    fun `parse correct variable pattern`(text: String, isConst: Boolean, isDestructuring: Boolean,
            hasConditional: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = VarPatternSelectiveStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as VarPatternSelectiveStmtNode

        Assertions.assertEquals(isConst, res.isConstant, "The isConstant property is incorrect")

        if (isDestructuring) {
            DestructuringStmtNodeTest.checkTestExpression(res.identifier)
        } else {
            CommonsTest.checkTestDynamicIdentifier(res.identifier)
        }

        if (hasConditional) {
            Assertions.assertNotNull(res.conditional, "The conditional property cannot be null")
            ConditionalPatternSelectiveStmtNodeTest.checkTestExpression(res.conditional!!)
        } else {
            Assertions.assertNull(res.conditional, "The conditional property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect variable pattern without identifier`() {
        TestUtils.assertParserException(AngmarParserExceptionType.VarPatternSelectiveStatementWithoutIdentifier) {
            val text = VarPatternSelectiveStmtNode.variableKeyword
            val parser = LexemParser(IOStringReader.from(text))
            VarPatternSelectiveStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = VarPatternSelectiveStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

