package org.lexem.angmar.parser.descriptive

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import java.util.stream.*
import kotlin.streams.*

internal class LexemePatternNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${LexemePatternNode.patternToken}${IdentifierNodeTest.testExpression}${LexemePatternNode.unionNameRelationalToken}"

        @JvmStatic
        private fun providePatterns(): Stream<Arguments> {
            val sequence = sequence {
                for (type in LexemePatternNode.Companion.PatternType.values()) {
                    for (hasUnionName1 in listOf(false, true)) {
                        val hasUnionName =
                                hasUnionName1 && (type == LexemePatternNode.Companion.PatternType.Additive || type == LexemePatternNode.Companion.PatternType.Selective || type == LexemePatternNode.Companion.PatternType.Alternative || type == LexemePatternNode.Companion.PatternType.Quantified)

                        // Skip duplicates
                        if (hasUnionName1 && !hasUnionName) {
                            break
                        }

                        for (hasPatternContent in listOf(false, true)) {
                            var text = if (hasPatternContent) {
                                LexemPatternContentNodeTest.testExpression
                            } else {
                                ""
                            }

                            if (hasUnionName) {
                                text =
                                        IdentifierNodeTest.testExpression + LexemePatternNode.unionNameRelationalToken + text
                            }

                            if (type == LexemePatternNode.Companion.PatternType.Quantified) {
                                text = ExplicitQuantifierLexemeNodeTest.testExpression + text
                            } else {
                                text = type.token + text
                            }

                            text = LexemePatternNode.patternToken + text

                            yield(Arguments.of(text, type, hasUnionName, hasPatternContent))

                            // With whitespaces
                            text = if (hasPatternContent) {
                                LexemPatternContentNodeTest.testExpression
                            } else {
                                ""
                            }

                            if (hasUnionName) {
                                text =
                                        IdentifierNodeTest.testExpression + " " + LexemePatternNode.unionNameRelationalToken + " " + text
                            }

                            if (type == LexemePatternNode.Companion.PatternType.Quantified) {
                                text = ExplicitQuantifierLexemeNodeTest.testExpression + " " + text
                            } else {
                                text = type.token + " " + text
                            }

                            text = LexemePatternNode.patternToken + text

                            yield(Arguments.of(text.trimEnd(), type, hasUnionName, hasPatternContent))
                        }
                    }
                }
            }

            return sequence.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as LexemePatternNode

            Assertions.assertEquals(LexemePatternNode.Companion.PatternType.Alternative, node.type,
                    "The type property is incorrect")
            Assertions.assertNotNull(node.unionName, "The unionName property must be null")
            IdentifierNodeTest.checkTestExpression(node.unionName!!)
            Assertions.assertNull(node.patternContent, "The patternContent property must be null")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("providePatterns")
    fun `parse correct nodes`(text: String, type: LexemePatternNode.Companion.PatternType, hasUnionName: Boolean,
            hasPatternContent: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = LexemePatternNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LexemePatternNode

        Assertions.assertEquals(type, res.type, "The type property is incorrect")

        if (type == LexemePatternNode.Companion.PatternType.Quantified) {
            Assertions.assertNotNull(res.quantifier, "The quantifier property cannot be null")
            ExplicitQuantifierLexemeNodeTest.checkTestExpression(res.quantifier!!)
        }

        if (hasUnionName) {
            Assertions.assertNotNull(res.unionName, "The unionName property cannot be null")
            IdentifierNodeTest.checkTestExpression(res.unionName!!)
        } else {
            Assertions.assertNull(res.unionName, "The unionName property must be null")
        }

        if (hasPatternContent) {
            Assertions.assertNotNull(res.patternContent, "The patternContent property cannot be null")
            LexemPatternContentNodeTest.checkTestExpression(res.patternContent!!)
        } else {
            Assertions.assertNull(res.patternContent, "The patternContent property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = LexemePatternNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

