package org.lexem.angmar.parser.descriptive

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.expressions.macros.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.functional.statements.*
import java.util.stream.*
import kotlin.streams.*

internal class AnyLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = TextLexemeNodeTest.testExpression
        const val testFilterExpression = AdditionFilterLexemeNodeTest.testExpression

        @JvmStatic
        private fun provideMacros(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of(MacroCheckPropsNodeTest.testExpression, 0))
                yield(Arguments.of(MacroBacktrackNodeTest.testExpression, 1))
                yield(Arguments.of(SetPropsMacroStmtNodeTest.testExpression, 2))
                yield(Arguments.of(LexemeCommonsTest.testAnchors, 3))
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideLexemesWithDataCapturing(): Stream<Arguments> {
            val sequence = sequence {
                val tests = listOf(TextLexemeNodeTest.testExpression, BinarySequenceLexemeNodeTest.testExpression,
                        BlockLexemeNodeTest.testExpression, QuantifiedGroupLexemeNodeTest.testExpression,
                        ExecutorLexemeNodeTest.testExpression, BlockStmtNodeTest.testExpression,
                        AccessLexemeNodeTest.testExpression, GroupLexemeNodeTest.testExpression)

                for ((index, value) in tests.withIndex()) {
                    for (hasDataCapturing in listOf(false, true)) {
                        for (hasQuantifier in listOf(false, true)) {
                            var text = value

                            if (hasDataCapturing) {
                                text =
                                        "${DataCapturingAccessLexemeNodeTest.testExpression}${AnyLexemeNode.dataCapturingRelationalToken}" + text
                            }

                            if (hasQuantifier) {
                                text += QuantifierLexemeNodeTest.testExpression
                            }

                            yield(Arguments.of(text, index, hasDataCapturing, hasQuantifier))

                            // With whitespace

                            text = value

                            if (hasDataCapturing) {
                                text =
                                        "${DataCapturingAccessLexemeNodeTest.testExpression} ${AnyLexemeNode.dataCapturingRelationalToken} " + text
                            }

                            if (hasQuantifier) {
                                text += QuantifierLexemeNodeTest.testExpression
                            }

                            yield(Arguments.of(text, index, hasDataCapturing, hasQuantifier))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as AnyLexemeNode

            Assertions.assertNull(node.dataCapturing, "The dataCapturing property must be null")
            Assertions.assertNull(node.quantifier, "The quantifier property must be null")
            TextLexemeNodeTest.checkTestExpression(node.lexeme)
        }

        fun checkTestFilterExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as AnyLexemeNode

            Assertions.assertNull(node.dataCapturing, "The dataCapturing property must be null")
            Assertions.assertNull(node.quantifier, "The quantifier property must be null")
            AdditionFilterLexemeNodeTest.checkTestExpression(node.lexeme)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideMacros")
    fun `parse correct macros`(text: String, type: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = AnyLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AnyLexemeNode

        Assertions.assertNull(res.dataCapturing, "The dataCapturing property must be null")
        Assertions.assertNull(res.quantifier, "The quantifier property must be null")

        when (type) {
            0 -> MacroCheckPropsNodeTest.checkTestExpression(res.lexeme)
            1 -> MacroBacktrackNodeTest.checkTestExpression(res.lexeme)
            2 -> SetPropsMacroStmtNodeTest.checkTestExpression(res.lexeme)
            3 -> LexemeCommonsTest.checkTestAnchors(res.lexeme)
            else -> throw AngmarUnreachableException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideLexemesWithDataCapturing")
    fun `parse correct lexemes with data capturing`(text: String, type: Int, hasDataCapturing: Boolean,
            hasQuantifier: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = AnyLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AnyLexemeNode

        when (type) {
            0 -> TextLexemeNodeTest.checkTestExpression(res.lexeme)
            1 -> BinarySequenceLexemeNodeTest.checkTestExpression(res.lexeme)
            2 -> BlockLexemeNodeTest.checkTestExpression(res.lexeme)
            3 -> QuantifiedGroupLexemeNodeTest.checkTestExpression(res.lexeme)
            4 -> ExecutorLexemeNodeTest.checkTestExpression(res.lexeme)
            5 -> BlockStmtNodeTest.checkTestExpression(res.lexeme)
            6 -> AccessLexemeNodeTest.checkTestExpression(res.lexeme)
            7 -> GroupLexemeNodeTest.checkTestExpression(res.lexeme)
            else -> throw AngmarUnreachableException()
        }

        if (hasDataCapturing) {
            Assertions.assertNotNull(res.dataCapturing, "The dataCapturing property cannot be null")
            DataCapturingAccessLexemeNodeTest.checkTestExpression(res.dataCapturing!!)
        } else {
            Assertions.assertNull(res.dataCapturing, "The dataCapturing property must be null")
        }

        if (hasQuantifier) {
            Assertions.assertNotNull(res.quantifier, "The quantifier property cannot be null")
            QuantifierLexemeNodeTest.checkTestExpression(res.quantifier!!)
        } else {
            Assertions.assertNull(res.quantifier, "The quantifier property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }
}
