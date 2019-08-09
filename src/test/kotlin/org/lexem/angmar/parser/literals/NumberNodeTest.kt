package org.lexem.angmar.parser.literals

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

internal class NumberNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "35"
        private const val testInteger = 55

        private val radix = listOf(2, 8, 10, 10, 16)
        private val prefixes = listOf(NumberNode.binaryPrefix, NumberNode.octalPrefix, "", NumberNode.decimalPrefix,
                NumberNode.hexadecimalPrefix)
        private val integers = listOf(0, 1, 7, 8, 10, 15, 16, 159, 317, 3368, 155686, Int.MAX_VALUE)


        @JvmStatic
        private fun provideIntegers(): Stream<Arguments> {
            val sequence = sequence {
                for (radixPrefix in radix.zip(prefixes)) {
                    for (integer in integers) {
                        for (underscore in 0..1) {
                            val integerText = testInteger.toString(radixPrefix.first)

                            var formattedInteger = integerText
                            if (underscore == 1) {
                                formattedInteger = underscoreNumber(formattedInteger)
                            }

                            yield(Arguments.of(formattedInteger, radixPrefix.first, integerText))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun providePrefixedIntegers(): Stream<Arguments> {
            val sequence = sequence {
                for (radixPrefix in radix.zip(prefixes)) {
                    for (integer in integers) {
                        for (underscore in 0..1) {
                            val integerText = testInteger.toString(radixPrefix.first)

                            var formattedInteger = integerText
                            if (underscore == 1) {
                                formattedInteger = underscoreNumber(formattedInteger)
                            }

                            yield(Arguments.of("${radixPrefix.second}$formattedInteger", radixPrefix.first,
                                    integerText))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideAll(): Stream<Arguments> {
            val sequence = sequence {
                for (radixPrefix in radix.zip(prefixes)) {
                    for (underscore in 0..1) {
                        val integer = testInteger.toString(radixPrefix.first)

                        var formattedInteger = integer
                        if (underscore == 1) {
                            formattedInteger = underscoreNumber(formattedInteger)
                        }

                        // Integer
                        yield(Arguments.of("${radixPrefix.second}$formattedInteger", radixPrefix.first, integer, null,
                                true, null))

                        // Integer + Decimal
                        yield(Arguments.of(
                                "${radixPrefix.second}$formattedInteger${NumberNode.decimalSeparator}$formattedInteger",
                                radixPrefix.first, integer, integer, true, null))

                        val exponentSeparators = when (radixPrefix.first) {
                            2 -> NumberNode.binaryExponentSeparator
                            8 -> NumberNode.octalExponentSeparator
                            10 -> NumberNode.decimalExponentSeparator
                            16 -> NumberNode.hexadecimalExponentSeparator
                            else -> throw AngmarUnimplementedException()
                        }

                        for (exponentLetter in exponentSeparators) {
                            for (sign in 0..2) {
                                val signText = when (sign) {
                                    2 -> NumberNode.exponentNegativeSign
                                    1 -> NumberNode.exponentPositiveSign
                                    else -> ""
                                }

                                // Integer + Exponent
                                yield(Arguments.of(
                                        "${radixPrefix.second}$formattedInteger$exponentLetter$signText$formattedInteger",
                                        radixPrefix.first, integer, null, sign != 2, integer))

                                // Integer + Decimal + Exponent
                                yield(Arguments.of(
                                        "${radixPrefix.second}$formattedInteger${NumberNode.decimalSeparator}$formattedInteger$exponentLetter$signText$formattedInteger",
                                        radixPrefix.first, integer, integer, sign != 2, integer))
                            }
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUXILIARY METHODS ------------------------------------------------------

        /**
         * Underscores a number.
         */
        private fun underscoreNumber(number: String): String {
            val res = number.split("").joinToString(NumberNode.digitSeparator)
            return res.substring(1 until res.length - 1)
        }

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is NumberNode, "The node is not a NumberNode")
            node as NumberNode

            Assertions.assertEquals(10, node.radix, "The radix property is not correct")
            Assertions.assertEquals("35", node.integer, "The integer property is not correct")
            Assertions.assertNull(node.decimal, "The decimal property must be null")
            Assertions.assertTrue(node.exponentSign, "The exponentSign property is not correct")
            Assertions.assertNull(node.exponent, "The exponent property must be null")
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideIntegers")
    fun `parse correct integers`(text: String, radix: Int, integer: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = when (radix) {
            2 -> NumberNode.readBinaryInteger(parser)
            8 -> NumberNode.readOctalInteger(parser)
            10 -> NumberNode.readDecimalInteger(parser)
            16 -> NumberNode.readHexadecimalInteger(parser)
            else -> throw AngmarUnimplementedException()
        }

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        Assertions.assertEquals(integer, res, "The result is incorrect")
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("providePrefixedIntegers")
    fun `parse correct prefixed integers`(text: String, radix: Int, integer: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = NumberNode.parseAnyIntegerDefaultDecimal(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as NumberNode

        Assertions.assertEquals(radix, res.radix, "The radix property is not correct")
        Assertions.assertEquals(integer, res.integer, "The integer property is not correct")
        Assertions.assertNull(res.decimal, "The decimal property must be null")
        Assertions.assertTrue(res.exponentSign, "The exponentSign property is not correct")
        Assertions.assertNull(res.exponent, "The exponent property must be null")
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideAll")
    fun `parse correct prefixed numbers`(text: String, radix: Int, integer: String, decimal: String?,
            exponentSign: Boolean, exponent: String?) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = NumberNode.parseAnyNumberDefaultDecimal(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as NumberNode

        Assertions.assertEquals(radix, res.radix, "The radix property is not correct")
        Assertions.assertEquals(integer, res.integer, "The integer property is not correct")
        Assertions.assertEquals(decimal, res.decimal, "The decimal property is not correct")
        Assertions.assertEquals(exponentSign, res.exponentSign, "The exponentSign property is not correct")
        Assertions.assertEquals(exponent, res.exponent, "The exponent property is not correct")
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideAll")
    fun `parse correct number followed by an access expression`(number: String, radix: Int, integer: String,
            decimal: String?, exponentSign: Boolean, exponent: String?) {
        val text =
                "$number${NumberNode.decimalSeparator}t" // We use a t because is not inside the hexadecimal range of characters.
        val parser = LexemParser(CustomStringReader.from(text))
        val res = NumberNode.parseAnyNumberDefaultDecimal(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as NumberNode

        Assertions.assertEquals(radix, res.radix, "The radix property is not correct")
        Assertions.assertEquals(integer, res.integer, "The integer property is not correct")
        Assertions.assertEquals(decimal, res.decimal, "The decimal property is not correct")
        Assertions.assertEquals(exponentSign, res.exponentSign, "The exponentSign property is not correct")
        Assertions.assertEquals(exponent, res.exponent, "The exponent property is not correct")
        Assertions.assertEquals(number.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = ["0${NumberNode.digitSeparator}", "${NumberNode.decimalPrefix}${NumberNode.digitSeparator}0", "${NumberNode.decimalPrefix}0${NumberNode.digitSeparator}", "0${NumberNode.decimalSeparator}0${NumberNode.digitSeparator}", "0${NumberNode.decimalSeparator}0e${NumberNode.exponentPositiveSign}${NumberNode.digitSeparator}0", "0${NumberNode.decimalSeparator}0e0${NumberNode.digitSeparator}"])
    fun `parse incorrect numbers with bad underscores`(numberText: String) {
        assertParserException {
            NumberNode.parseAnyNumberDefaultDecimal(LexemParser(CustomStringReader.from(numberText)))
        }

        // Note: "0._0" is interpreted as a Number(0) followed by a property access (._0)
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = ["${NumberNode.decimalPrefix}${NumberNode.decimalSeparator}0", "${NumberNode.decimalPrefix}e${NumberNode.exponentPositiveSign}0"])
    fun `parse incorrect numbers with no integer before decimal or exponent`(numberText: String) {
        assertParserException {
            NumberNode.parseAnyNumberDefaultDecimal(LexemParser(CustomStringReader.from(numberText)))
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect numbers with no decimal after separator`() {
        assertParserException {
            val numberText = "${NumberNode.decimalPrefix}0${NumberNode.decimalSeparator}"
            NumberNode.parseAnyNumberDefaultDecimal(LexemParser(CustomStringReader.from(numberText)))
        }

        // Note: "0d0.e+0" is interpreted as a Number(0) followed by a property access (.e), an operator(+) and another number(0)
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = ["${NumberNode.decimalPrefix}0${NumberNode.decimalSeparator}0e", "${NumberNode.decimalPrefix}0${NumberNode.decimalSeparator}0e${NumberNode.exponentPositiveSign}", "${NumberNode.decimalPrefix}0${NumberNode.decimalSeparator}0e${NumberNode.exponentNegativeSign}"])
    fun `parse incorrect numbers with no exponent after the letter or sign`(numberText: String) {
        assertParserException {
            NumberNode.parseAnyNumberDefaultDecimal(LexemParser(CustomStringReader.from(numberText)))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = NumberNode.parseAnyNumberDefaultDecimal(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
