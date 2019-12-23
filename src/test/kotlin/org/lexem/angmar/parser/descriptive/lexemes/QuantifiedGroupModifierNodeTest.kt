package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class QuantifiedGroupModifierNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${QuantifiedGroupModifierNode.startToken}${ExpressionsCommonsTest.testExpression}${QuantifiedGroupModifierNode.endToken}"

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (hasMinimum in listOf(false, true)) {
                    for (hasMaximum in listOf(false, true)) {
                        if (hasMaximum) {
                            for (hasMaximumProperty in listOf(false, true)) {
                                if (!hasMinimum && !hasMaximumProperty) {
                                    continue
                                }

                                var text = if (hasMinimum) {
                                    ExpressionsCommonsTest.testExpression
                                } else {
                                    ""
                                }

                                text += QuantifiedGroupModifierNode.elementSeparator

                                if (hasMaximumProperty) {
                                    text += ExpressionsCommonsTest.testExpression
                                }

                                yield(Arguments.of(
                                        "${QuantifiedGroupModifierNode.startToken}$text${QuantifiedGroupModifierNode.endToken}",
                                        hasMinimum, hasMaximum, hasMaximumProperty))

                                // With whitespaces
                                text = if (hasMinimum) {
                                    ExpressionsCommonsTest.testExpression
                                } else {
                                    ""
                                }

                                text += " " + QuantifiedGroupModifierNode.elementSeparator

                                if (hasMaximumProperty) {
                                    text += " " + ExpressionsCommonsTest.testExpression
                                }

                                yield(Arguments.of(
                                        "${QuantifiedGroupModifierNode.startToken} $text ${QuantifiedGroupModifierNode.endToken}",
                                        hasMinimum, hasMaximum, hasMaximumProperty))
                            }
                        } else {
                            var text = if (hasMinimum) {
                                ExpressionsCommonsTest.testExpression
                            } else {
                                ""
                            }

                            if (hasMaximum) {
                                text += QuantifiedGroupModifierNode.elementSeparator
                            }

                            yield(Arguments.of(
                                    "${QuantifiedGroupModifierNode.startToken}$text${QuantifiedGroupModifierNode.endToken}",
                                    hasMinimum, hasMaximum, false))

                            // With whitespaces
                            text = if (hasMinimum) {
                                ExpressionsCommonsTest.testExpression
                            } else {
                                ""
                            }

                            if (hasMaximum) {
                                text += " " + QuantifiedGroupModifierNode.elementSeparator
                            }

                            yield(Arguments.of(
                                    "${QuantifiedGroupModifierNode.startToken} $text ${QuantifiedGroupModifierNode.endToken}",
                                    hasMinimum, hasMaximum, false))
                        }
                    }
                }
            }

            return sequence.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as QuantifiedGroupModifierNode

            Assertions.assertNotNull(node.minimum, "The minimum property cannot be null")
            ExpressionsCommonsTest.checkTestExpression(node.minimum!!)
            Assertions.assertFalse(node.hasMaximum, "The hasMaximum property is incorrect")
            Assertions.assertNull(node.maximum, "The maximum property must be null")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct nodes`(text: String, hasMinimum: Boolean, hasMaximum: Boolean, hasMaximumProperty: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuantifiedGroupModifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as QuantifiedGroupModifierNode

        if (hasMinimum) {
            Assertions.assertNotNull(res.minimum, "The minimum property cannot be null")
            ExpressionsCommonsTest.checkTestExpression(res.minimum!!)
        } else {
            Assertions.assertNull(res.minimum, "The minimum property must be null")
        }

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
    fun `parse incorrect node without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.QuantifierWithoutEndToken) {
            val text = "${QuantifiedGroupModifierNode.startToken}${IdentifierNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            QuantifiedGroupModifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect node without maximum value`() {
        TestUtils.assertParserException(AngmarParserExceptionType.QuantifierWithoutMaximumValue) {
            val text =
                    "${QuantifiedGroupModifierNode.startToken}${QuantifiedGroupModifierNode.elementSeparator}${QuantifiedGroupModifierNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            QuantifiedGroupModifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = QuantifiedGroupModifierNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
