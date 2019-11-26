package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LexemPropertyPostfixNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = LexemPropertyPostfixNode.properties

        @JvmStatic
        private fun provideProperties(): Stream<String> {
            val sequence = sequence {
                for (c in LexemPropertyPostfixNode.properties) {
                    yield("$c")

                    for (c2 in LexemPropertyPostfixNode.properties) {
                        yield("$c$c2")
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun providePropertyPostfix(): Stream<Arguments> {
            val sequence = sequence {
                for (hasPositives in listOf(false, true)) {
                    for (hasNegatives in listOf(false, true)) {
                        for (hasReversed in listOf(false, true)) {
                            if (!hasPositives && !hasNegatives && !hasReversed) {
                                continue
                            }

                            for (i in provideProperties()) {
                                var text = ""

                                if (hasPositives) {
                                    text += i
                                }

                                if (hasNegatives) {
                                    text += "${LexemPropertyPostfixNode.negativeToken}$i"
                                }

                                if (hasReversed) {
                                    text += "${LexemPropertyPostfixNode.reversedToken}$i"
                                }

                                yield(Arguments.of(text, hasPositives, hasNegatives, hasReversed, i))
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
            node as LexemPropertyPostfixNode

            Assertions.assertEquals(LexemPropertyPostfixNode.properties.length, node.positiveElements.size,
                    "The number of positive elements is incorrect")
            for ((index, element) in node.positiveElements.withIndex()) {
                val property = "${LexemPropertyPostfixNode.properties[index]}"
                Assertions.assertEquals(property, element, "The property is incorrect")
            }

            Assertions.assertEquals(0, node.negativeElements.size, "The number of negative elements is incorrect")
            Assertions.assertEquals(0, node.reversedElements.size, "The number of reversed elements is incorrect")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("providePropertyPostfix")
    fun `parse correct properties`(text: String, hasPositives: Boolean, hasNegatives: Boolean, hasReversed: Boolean,
            properties: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = LexemPropertyPostfixNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LexemPropertyPostfixNode

        if (hasPositives) {
            Assertions.assertEquals(properties.length, res.positiveElements.size,
                    "The number of positive elements is incorrect")
            for ((index, element) in res.positiveElements.withIndex()) {
                val property = "${properties[index]}"
                Assertions.assertEquals(property, element, "The property is incorrect")
            }
        } else {
            Assertions.assertEquals(0, res.positiveElements.size, "The number of positive elements is incorrect")
        }

        if (hasNegatives) {
            Assertions.assertEquals(properties.length, res.negativeElements.size,
                    "The number of negative elements is incorrect")
            for ((index, element) in res.negativeElements.withIndex()) {
                val property = "${properties[index]}"
                Assertions.assertEquals(property, element, "The property is incorrect")
            }
        } else {
            Assertions.assertEquals(0, res.negativeElements.size, "The number of negative elements is incorrect")
        }

        if (hasReversed) {
            Assertions.assertEquals(properties.length, res.reversedElements.size,
                    "The number of reversed elements is incorrect")
            for ((index, element) in res.reversedElements.withIndex()) {
                val property = "${properties[index]}"
                Assertions.assertEquals(property, element, "The property is incorrect")
            }
        } else {
            Assertions.assertEquals(0, res.reversedElements.size, "The number of reversed elements is incorrect")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect property postfix without whitespace at the end`() {
        TestUtils.assertParserException(AngmarParserExceptionType.InlinePropertyPostfixWithBadEnd) {
            val text = "${LexemPropertyPostfixNode.properties}a"
            val parser = LexemParser(IOStringReader.from(text))
            LexemPropertyPostfixNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect property postfix without properties after negative token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.InlinePropertyPostfixWithoutProperty) {
            val text = LexemPropertyPostfixNode.negativeToken
            val parser = LexemParser(IOStringReader.from(text))
            LexemPropertyPostfixNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect property postfix without properties after reversed token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.InlinePropertyPostfixWithoutProperty) {
            val text = LexemPropertyPostfixNode.reversedToken
            val parser = LexemParser(IOStringReader.from(text))
            LexemPropertyPostfixNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = LexemPropertyPostfixNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

