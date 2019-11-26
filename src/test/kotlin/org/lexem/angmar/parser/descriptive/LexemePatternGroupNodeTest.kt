package org.lexem.angmar.parser.descriptive

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LexemePatternGroupNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = LexemePatternNode.patternToken

        @JvmStatic
        private fun provideUnionPatternsWithoutUnionsFollowedByOthers(): Stream<Arguments> {
            val sequence = sequence {
                for (mainType in LexemePatternNode.unionPatterns) {
                    val text1 =
                            LexemePatternNode.patternToken + if (mainType == LexemePatternNode.Companion.PatternType.Quantified) {
                                ExplicitQuantifierLexemeNodeTest.testExpression
                            } else {
                                mainType.token
                            }

                    yield(Arguments.of(text1, text1, mainType))

                    for (secondType in (LexemePatternNode.singlePatterns + LexemePatternNode.unionPatterns).filter { it != mainType }) {
                        for (hasUnionName1 in listOf(false, true)) {
                            val hasUnionName =
                                    hasUnionName1 && (secondType == LexemePatternNode.Companion.PatternType.Additive || secondType == LexemePatternNode.Companion.PatternType.Selective || secondType == LexemePatternNode.Companion.PatternType.Alternative || secondType == LexemePatternNode.Companion.PatternType.Quantified)

                            // Skip duplicates
                            if (hasUnionName1 && !hasUnionName) {
                                break
                            }

                            var text2 = ""

                            if (hasUnionName) {
                                text2 = IdentifierNodeTest.testExpression + LexemePatternNode.unionNameRelationalToken
                            }

                            if (secondType == LexemePatternNode.Companion.PatternType.Quantified) {
                                text2 = ExplicitQuantifierLexemeNodeTest.testExpression + text2
                            } else {
                                text2 = secondType.token + text2
                            }

                            text2 = LexemePatternNode.patternToken + text2

                            yield(Arguments.of("$text1\n$text2", text1, mainType))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideUnionPatternsWithoutUnionsFollowedBySameWithUnion(): Stream<Arguments> {
            val sequence = sequence {
                for (mainType in LexemePatternNode.unionPatterns) {
                    val text1 =
                            LexemePatternNode.patternToken + if (mainType == LexemePatternNode.Companion.PatternType.Quantified) {
                                ExplicitQuantifierLexemeNodeTest.testExpression
                            } else {
                                mainType.token
                            }


                    var text2 = IdentifierNodeTest.testExpression + LexemePatternNode.unionNameRelationalToken

                    if (mainType == LexemePatternNode.Companion.PatternType.Quantified) {
                        text2 = ExplicitQuantifierLexemeNodeTest.testExpression + text2
                    } else {
                        text2 = mainType.token + text2
                    }

                    text2 = LexemePatternNode.patternToken + text2

                    yield(Arguments.of("$text1\n$text2", text1, mainType))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideUnionPatternsWithSurrogates(): Stream<Arguments> {
            val sequence = sequence {
                for (mainType in LexemePatternNode.unionPatterns) {
                    var text =
                            LexemePatternNode.patternToken + if (mainType == LexemePatternNode.Companion.PatternType.Quantified) {
                                ExplicitQuantifierLexemeNodeTest.testExpression
                            } else {
                                mainType.token
                            }

                    for (i in 0..2) {
                        val text2 =
                                LexemePatternNode.patternToken + if (mainType == LexemePatternNode.Companion.PatternType.Quantified) {
                                    LexemePatternNode.quantifierSlaveToken
                                } else {
                                    mainType.token
                                }

                        text += "\n$text2"

                        yield(Arguments.of(text, mainType, i + 2))
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideNotMatching(): Stream<Arguments> {
            val sequence = sequence {
                for (mainType in LexemePatternNode.unionPatterns) {

                    yield(Arguments.of(""))

                    // Single patterns
                    for (i in LexemePatternNode.singlePatterns) {
                        yield(Arguments.of("${LexemePatternNode.patternToken}${i.token}"))
                    }

                    // Union
                    yield(Arguments.of(
                            "${LexemePatternNode.patternToken}${IdentifierNodeTest.testExpression}${LexemePatternNode.unionNameRelationalToken}"))
                }
            }

            return sequence.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as LexemePatternGroupNode

            Assertions.assertEquals(LexemePatternNode.Companion.PatternType.Alternative, node.type,
                    "The type property is incorrect")
            Assertions.assertNull(node.quantifier, "The quantifier property must be null")
            Assertions.assertEquals(1, node.patterns.size, "The number of patterns is incorrect")
            Assertions.assertNull(node.patterns.first(), "The pattern[0] must be null")
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideUnionPatternsWithoutUnionsFollowedByOthers",
            "provideUnionPatternsWithoutUnionsFollowedBySameWithUnion")
    fun `parse correct union patterns without unions followed by other types or the same with union`(text: String,
            textFirstPattern: String, type: LexemePatternNode.Companion.PatternType) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = LexemePatternGroupNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LexemePatternGroupNode

        Assertions.assertEquals(type, res.type, "The type property is incorrect")
        Assertions.assertEquals(1, res.patterns.size, "The number of patterns is incorrect")

        if (type == LexemePatternNode.Companion.PatternType.Quantified) {
            Assertions.assertNotNull(res.quantifier, "The quantifier property cannot be null")
            ExplicitQuantifierLexemeNodeTest.checkTestExpression(res.quantifier!!)

        } else {
            Assertions.assertNull(res.quantifier, "The quantifier property must be null")
        }

        for ((i, pat) in res.patterns.withIndex()) {
            Assertions.assertNull(pat, "The patterns[$i] must be null")
        }

        Assertions.assertEquals(textFirstPattern.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideUnionPatternsWithSurrogates")
    fun `parse correct union patterns with surrogates`(text: String, type: LexemePatternNode.Companion.PatternType,
            patternCount: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = LexemePatternGroupNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LexemePatternGroupNode

        Assertions.assertEquals(type, res.type, "The type property is incorrect")
        Assertions.assertEquals(patternCount, res.patterns.size, "The number of patterns is incorrect")

        if (type == LexemePatternNode.Companion.PatternType.Quantified) {
            Assertions.assertNotNull(res.quantifier, "The quantifier property cannot be null")
            ExplicitQuantifierLexemeNodeTest.checkTestExpression(res.quantifier!!)

        } else {
            Assertions.assertNull(res.quantifier, "The quantifier property must be null")
        }

        for ((i, pat) in res.patterns.withIndex()) {
            Assertions.assertNull(pat, "The patterns[$i] must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect union started with a quantified slave`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SlaveQuantifiedPatternWithoutMaster) {
            val text = "${LexemePatternNode.patternToken}${LexemePatternNode.quantifierSlaveToken}"
            val parser = LexemParser(IOStringReader.from(text))
            parser.isDescriptiveCode = true
            LexemePatternGroupNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @MethodSource("provideNotMatching")
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = LexemePatternGroupNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

