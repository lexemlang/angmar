package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import java.util.stream.*
import kotlin.streams.*

internal class QuantifierLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = QuantifierLexemeNode.lazyAbbreviation

        @JvmStatic
        private fun provideAbbreviation(): Stream<Arguments> {
            val sequence = sequence {
                for (abbreviation in QuantifierLexemeNode.abbreviations) {
                    val text = "$abbreviation"

                    yield(Arguments.of(text, abbreviation, null))

                    for (modifier in QuantifierLexemeNode.abbreviations) {
                        val text2 = text + modifier

                        yield(Arguments.of(text2, abbreviation, modifier))
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                val text = ExplicitQuantifierLexemeNodeTest.testExpression

                yield(Arguments.of(text, null))

                for (modifier in QuantifierLexemeNode.abbreviations) {
                    val text2 = text + modifier

                    yield(Arguments.of(text2, modifier))
                }
            }

            return sequence.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as QuantifierLexemeNode

            Assertions.assertEquals(QuantifierLexemeNode.lazyAbbreviation[0], node.abbreviation,
                    "The abbreviation property is incorrect")
            Assertions.assertNull(node.quantifier, "The quantifier property must be null")
            Assertions.assertNull(node.modifier, "The modifier property is incorrect")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideAbbreviation")
    fun `parse correct abbreviation`(text: String, abbreviation: Char, modifier: Char?) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuantifierLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as QuantifierLexemeNode

        Assertions.assertEquals(abbreviation, res.abbreviation, "The abbreviation property is incorrect")
        Assertions.assertNull(res.quantifier, "The quantifier property must be null")
        Assertions.assertEquals(modifier, res.modifier, "The modifier property is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct nodes`(text: String, modifier: Char?) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuantifierLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as QuantifierLexemeNode

        Assertions.assertNull(res.abbreviation, "The abbreviation property must be null")
        Assertions.assertNotNull(res.quantifier, "The quantifier property cannot be null")
        ExplicitQuantifierLexemeNodeTest.checkTestExpression(res.quantifier!!)
        Assertions.assertEquals(modifier, res.modifier, "The modifier property must be null")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuantifierLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

