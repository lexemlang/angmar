package org.lexem.angmar.parser.functional.statements.selective

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
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
                for (isConst in 0..1) {
                    val keyword = if (isConst == 0) {
                        VarPatternSelectiveStmtNode.variableKeyword
                    } else {
                        VarPatternSelectiveStmtNode.constKeyword
                    }

                    for (isDestructuring in 0..1) {
                        val identifier = if (isDestructuring == 0) {
                            CommonsTest.testDynamicIdentifier
                        } else {
                            DestructuringStmtNodeTest.testExpression
                        }

                        for (hasConditional in 0..1) {
                            var text = "$keyword $identifier"

                            if (hasConditional == 1) {
                                text += " ${ConditionalPatternSelectiveStmtNodeTest.testExpression}"
                            }

                            yield(Arguments.of(text, isConst == 1, isDestructuring == 1, hasConditional == 1))

                            // Without whitespace
                            text = "$keyword $identifier"

                            if (hasConditional == 1) {
                                text += ConditionalPatternSelectiveStmtNodeTest.testExpression
                            }

                            yield(Arguments.of(text, isConst == 1, isDestructuring == 1, hasConditional == 1))
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
        val parser = LexemParser(CustomStringReader.from(text))
        val res = VarPatternSelectiveStmtNode.parse(parser)

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
        assertParserException {
            val text = VarPatternSelectiveStmtNode.variableKeyword
            val parser = LexemParser(CustomStringReader.from(text))
            VarPatternSelectiveStmtNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = VarPatternSelectiveStmtNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

