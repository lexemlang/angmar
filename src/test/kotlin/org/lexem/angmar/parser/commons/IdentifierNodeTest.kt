package org.lexem.angmar.parser.commons

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import java.util.stream.*
import kotlin.random.*
import kotlin.streams.*

internal class IdentifierNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "testId"

        @JvmStatic
        private fun provideCorrectSimpleIdentifiers(): Stream<Arguments> {
            val sequence = sequence {
                // 1 char: H
                for (hChar in IdentifierNode.headChars) {
                    yield(Arguments.of("${hChar.first}"))
                }

                // 2 chars: H H
                for (hChar in IdentifierNode.headChars) {
                    yield(Arguments.of("${hChar.first}${hChar.last}"))
                }

                // 2 chars: H E
                for (hChar in IdentifierNode.headChars) {
                    for (eChar in IdentifierNode.endChars) {
                        yield(Arguments.of("${hChar.first}${hChar.first}"))
                    }
                }

                // n chars
                for (size in 1..100) {
                    for (repetitions in 0..100) {
                        yield(Arguments.of(generateSimpleIdentifier(size)))
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provide100ComplexIdentifiers(): Stream<Arguments> {
            val sequence = sequence {
                for (size in 1..100) {
                    yield(Arguments.of(generateComplexIdentifier(size)))
                }
            }

            return sequence.asStream()
        }


        @JvmStatic
        private fun provideBadIdentifiers(): Stream<Arguments> {
            val sequence = sequence {
                for (nextCharRange in IdentifierNode.endChars) {
                    yield(Arguments.of(nextCharRange.first.toString()))
                    yield(Arguments.of(nextCharRange.last.toString()))
                    yield(Arguments.of(
                            ((nextCharRange.last.toInt() + nextCharRange.first.toInt()) / 2).toChar().toString()))
                }

                // macro name
                yield(Arguments.of("${IdentifierNode.headChars.first().first()}${MacroExpression.macroSuffix}"))
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideKeywords() = CommonsTest.provideKeywords()

        // AUX METHODS ------------------------------------------------------------

        private fun generateComplexIdentifier(size: Int): String {
            var res = generateSimpleIdentifier(Random.nextInt(12))

            for (i in 0..size) {
                res += "${IdentifierNode.middleChar}${generateSimpleIdentifier(Random.nextInt(12))}"
            }

            return res
        }

        private fun generateSimpleIdentifier(size: Int): String {
            var range = IdentifierNode.headChars[Random.nextInt(0, IdentifierNode.headChars.size)]
            var res = "${Random.nextInt(range.first.toInt(), range.last.toInt() + 1).toChar()}"

            while (res.length < size) {
                if (Random.nextBoolean()) {
                    range = IdentifierNode.headChars[Random.nextInt(0, IdentifierNode.headChars.size)]
                    res += "${Random.nextInt(range.first.toInt(), range.last.toInt() + 1).toChar()}"
                } else {
                    range = IdentifierNode.endChars[Random.nextInt(0, IdentifierNode.endChars.size)]
                    res += "${Random.nextInt(range.first.toInt(), range.last.toInt() + 1).toChar()}"
                }
            }

            return res
        }

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is IdentifierNode, "The node is not an IdentifierNode")
            node as IdentifierNode

            Assertions.assertEquals(1, node.simpleIdentifiers.size,
                    "The length of the simple identifier list is not correct")
            Assertions.assertNull(node.quotedIdentifier, "The quotedIdentifier property is not correct")
            Assertions.assertEquals(testExpression, node.simpleIdentifiers.first(),
                    "The simpleIdentifiers[0] property is not correct.")
        }
    }


    // TESTS ------------------------------------------------------------------

    @Nightly
    @ParameterizedTest
    @MethodSource("provideCorrectSimpleIdentifiers")
    fun `parse simple identifier`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = IdentifierNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as IdentifierNode

        Assertions.assertFalse(res.isQuotedIdentifier, "The isQuotedIdentifier is not correct")
        Assertions.assertNull(res.quotedIdentifier, "The quotedIdentifier is not correct")
        Assertions.assertEquals(text.split(IdentifierNode.middleChar).size, res.simpleIdentifiers.size,
                "The count of simple identifiers is not correct")

        for (word in text.split(IdentifierNode.middleChar).withIndex()) {
            Assertions.assertEquals(word.value, res.simpleIdentifiers[word.index], "A simple identifier is not correct")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provide100ComplexIdentifiers")
    fun `parse complex identifier`(identifier: String) {
        `parse simple identifier`(identifier)
    }

    @ParameterizedTest
    @ValueSource(strings = ["this is an example with escapes \\k \\ll"])
    fun `parse quoted identifier`(identifier: String) {
        val text = "${QuotedIdentifierNode.startQuote}$identifier${QuotedIdentifierNode.endQuote}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = IdentifierNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as IdentifierNode

        Assertions.assertTrue(res.isQuotedIdentifier, "The isQuotedIdentifier is not correct")
        Assertions.assertNotNull(res.quotedIdentifier, "The quotedIdentifier is not correct")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideBadIdentifiers")
    fun `not parse the node`(identifier: String) {
        val parser = LexemParser(CustomStringReader.from(identifier))
        val res = IdentifierNode.parse(parser)

        Assertions.assertNull(res, "The parser must not capture anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser has advanced the cursor")
    }
}
