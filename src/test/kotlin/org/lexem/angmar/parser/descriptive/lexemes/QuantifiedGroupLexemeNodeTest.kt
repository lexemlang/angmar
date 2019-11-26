package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class QuantifiedGroupLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${QuantifiedGroupLexemeNode.startToken}${LexemPatternContentNodeTest.testExpression}${QuantifiedGroupLexemeNode.patternToken}${QuantifiedGroupLexemeNode.endToken}"

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegated in listOf(false, true)) {
                    for (hasMainModifier in listOf(false, true)) {
                        for (hasModifiers in listOf(false, true)) {
                            for (patternCount in 1..3) {
                                val list = List(patternCount) { LexemPatternContentNodeTest.testExpression }
                                var text = list.joinToString("") {
                                    if (hasModifiers) {
                                        "$it${QuantifiedGroupLexemeNode.patternToken}${QuantifierLexemeNodeTest.testExpression}"

                                    } else {
                                        "$it${QuantifiedGroupLexemeNode.patternToken}"
                                    }
                                }

                                if (hasMainModifier) {
                                    text = QuantifiedGroupModifierNodeTest.testExpression + text
                                }

                                text =
                                        "${QuantifiedGroupLexemeNode.startToken}$text${QuantifiedGroupLexemeNode.endToken}"

                                if (isNegated) {
                                    text = QuantifiedGroupLexemeNode.notOperator + text
                                }

                                yield(Arguments.of(text, isNegated, hasMainModifier, hasModifiers, patternCount))

                                // With whitespaces
                                text = list.joinToString(" ") {
                                    if (hasModifiers) {
                                        "$it ${QuantifiedGroupLexemeNode.patternToken}${QuantifierLexemeNodeTest.testExpression}"

                                    } else {
                                        "$it ${QuantifiedGroupLexemeNode.patternToken}"
                                    }
                                }

                                if (hasMainModifier) {
                                    text = "${QuantifiedGroupModifierNodeTest.testExpression} $text"
                                    text =
                                            "${QuantifiedGroupLexemeNode.startToken}$text ${QuantifiedGroupLexemeNode.endToken}"
                                } else {
                                    text =
                                            "${QuantifiedGroupLexemeNode.startToken} $text ${QuantifiedGroupLexemeNode.endToken}"
                                }

                                if (isNegated) {
                                    text = QuantifiedGroupLexemeNode.notOperator + text
                                }

                                yield(Arguments.of(text, isNegated, hasMainModifier, hasModifiers, patternCount))
                            }
                        }
                    }
                }
            }

            return sequence.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as QuantifiedGroupLexemeNode

            Assertions.assertFalse(node.isNegated, "The isNegated property is incorrect")
            Assertions.assertNull(node.mainModifier, "The mainModifier property must be null")
            Assertions.assertEquals(1, node.patterns.size, "The number of patterns is incorrect")

            node.patterns.forEach {
                LexemPatternContentNodeTest.checkTestExpression(it)
            }

            Assertions.assertEquals(1, node.modifiers.size, "The number of modifiers is incorrect")

            node.modifiers.forEach {
                Assertions.assertNull(it, "The modifier must be null")
            }
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct nodes`(text: String, isNegated: Boolean, hasMainModifier: Boolean, hasModifiers: Boolean,
            patternCount: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = QuantifiedGroupLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as QuantifiedGroupLexemeNode

        Assertions.assertEquals(isNegated, res.isNegated, "The isNegated property is incorrect")

        if (hasMainModifier) {
            Assertions.assertNotNull(res.mainModifier, "The mainModifier property cannot be null")
            QuantifiedGroupModifierNodeTest.checkTestExpression(res.mainModifier!!)
        } else {
            Assertions.assertNull(res.mainModifier, "The mainModifier property must be null")
        }

        Assertions.assertEquals(patternCount, res.patterns.size, "The number of patterns is incorrect")

        res.patterns.forEach {
            LexemPatternContentNodeTest.checkTestExpression(it)
        }

        Assertions.assertEquals(patternCount, res.modifiers.size, "The number of modifiers is incorrect")

        res.modifiers.forEach {
            if (hasModifiers) {
                Assertions.assertNotNull(it, "The modifier property cannot be null")
                QuantifierLexemeNodeTest.checkTestExpression(it!!)
            } else {
                Assertions.assertNull(it, "The modifier must be null")
            }
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect node without pattern token after token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.QuantifiedGroupPatternWithoutLexemes) {
            val text =
                    "${QuantifiedGroupLexemeNode.startToken}${LexemPatternContentNodeTest.testExpression}${QuantifiedGroupLexemeNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            parser.isDescriptiveCode = true
            QuantifiedGroupLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect node without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.QuantifiedGroupWithoutEndToken) {
            val text =
                    "${QuantifiedGroupLexemeNode.startToken}${LexemPatternContentNodeTest.testExpression}${QuantifiedGroupLexemeNode.patternToken}"
            val parser = LexemParser(IOStringReader.from(text))
            parser.isDescriptiveCode = true
            QuantifiedGroupLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect node without patterns`() {
        TestUtils.assertParserException(AngmarParserExceptionType.QuantifiedGroupWithoutEndToken) {
            val text = "${QuantifiedGroupLexemeNode.startToken}${QuantifiedGroupLexemeNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            parser.isDescriptiveCode = true
            QuantifiedGroupLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = QuantifiedGroupLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

