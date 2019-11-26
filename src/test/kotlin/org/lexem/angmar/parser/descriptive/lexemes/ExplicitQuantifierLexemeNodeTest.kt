package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class ExplicitQuantifierLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ExplicitQuantifierLexemeNode.startToken}${ExpressionsCommonsTest.testExpression}${ExplicitQuantifierLexemeNode.endToken}"

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (hasMaximum in listOf(false, true)) {
                    if (hasMaximum) {
                        for (hasMaximumProperty in listOf(false, true)) {
                            if (hasMaximumProperty) {
                                var text =
                                        "${ExplicitQuantifierLexemeNode.startToken}${ExpressionsCommonsTest.testExpression}${ExplicitQuantifierLexemeNode.elementSeparator}${ExpressionsCommonsTest.testExpression}${ExplicitQuantifierLexemeNode.endToken}"

                                yield(Arguments.of(text, true, true))

                                // With whitespaces
                                text =
                                        "${ExplicitQuantifierLexemeNode.startToken} ${ExpressionsCommonsTest.testExpression} ${ExplicitQuantifierLexemeNode.elementSeparator} ${ExpressionsCommonsTest.testExpression} ${ExplicitQuantifierLexemeNode.endToken}"

                                yield(Arguments.of(text, true, true))
                            } else {
                                var text =
                                        "${ExplicitQuantifierLexemeNode.startToken}${ExpressionsCommonsTest.testExpression}${ExplicitQuantifierLexemeNode.elementSeparator}${ExplicitQuantifierLexemeNode.endToken}"

                                yield(Arguments.of(text, true, false))

                                // With whitespaces
                                text =
                                        "${ExplicitQuantifierLexemeNode.startToken} ${ExpressionsCommonsTest.testExpression} ${ExplicitQuantifierLexemeNode.elementSeparator} ${ExplicitQuantifierLexemeNode.endToken}"

                                yield(Arguments.of(text, true, false))
                            }
                        }
                    } else {
                        var text =
                                "${ExplicitQuantifierLexemeNode.startToken}${ExpressionsCommonsTest.testExpression}${ExplicitQuantifierLexemeNode.endToken}"

                        yield(Arguments.of(text, false, false))

                        // With whitespaces
                        text =
                                "${ExplicitQuantifierLexemeNode.startToken} ${ExpressionsCommonsTest.testExpression} ${ExplicitQuantifierLexemeNode.endToken}"

                        yield(Arguments.of(text, false, false))
                    }
                }
            }

            return sequence.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as ExplicitQuantifierLexemeNode

            ExpressionsCommonsTest.checkTestExpression(node.minimum)
            Assertions.assertFalse(node.hasMaximum, "The hasMaximum property is incorrect")
            Assertions.assertNull(node.maximum, "The maximum property must be null")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct nodes`(text: String, hasMaximum: Boolean, hasMaximumProperty: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ExplicitQuantifierLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ExplicitQuantifierLexemeNode

        ExpressionsCommonsTest.checkTestExpression(res.minimum)
        Assertions.assertEquals(hasMaximum, res.hasMaximum, "The hasMaximum property is incorrect")

        if (hasMaximumProperty) {
            Assertions.assertNotNull(res.maximum, "The maximum property cannot be null")
            ExpressionsCommonsTest.checkTestExpression(res.maximum!!)
        } else {
            Assertions.assertNull(res.maximum, "The maximum property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect node without selector`() {
        TestUtils.assertParserException(AngmarParserExceptionType.QuantifierWithoutMinimumExpression) {
            val text = "${ExplicitQuantifierLexemeNode.startToken}${ExplicitQuantifierLexemeNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            ExplicitQuantifierLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect node without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.QuantifierWithoutEndToken) {
            val text = "${ExplicitQuantifierLexemeNode.startToken}${SelectorNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            ExplicitQuantifierLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ExplicitQuantifierLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
