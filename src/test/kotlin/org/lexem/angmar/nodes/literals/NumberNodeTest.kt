package org.lexem.angmar.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.readers.CustomStringReader
import org.lexem.angmar.nodes.expressions.modifiers.AccessExpressionNode

internal class NumberNodeTest {
    private val radix = listOf(2, 8, 10, 16)
    private val prefixes = listOf(NumberNode.BinaryPrefix, NumberNode.OctalPrefix, NumberNode.DecimalPrefix,
            NumberNode.HexadecimalPrefix)
    private val integers = listOf(0, 1, 7, 8, 9, 10, 11, 15, 16, 17, 11234, 5467, 159, 317, 228, 3368, 155686)

    @Test
    fun `parse correct prefixed numbers`() {
        // Integer
        for (radixPrefix in radix.zip(prefixes)) {
            for (integer in integers) {
                for (underscore in 0..1) {
                    var integerText = integer.toString(radixPrefix.first)

                    if (underscore == 1) {
                        integerText = underscoreNumber(integerText)
                    }

                    val numberText = "${radixPrefix.second}$integerText"

                    val res = NumberNode.parseAnyNumberDefaultDecimal(
                        LexemParser(
                            CustomStringReader.from(
                                numberText
                            )
                        )
                    )

                    Assertions.assertNotNull(res, numberText)
                    res as NumberNode

                    Assertions.assertEquals(numberText, res.content)
                    Assertions.assertEquals(true, res.prefix)
                    Assertions.assertEquals(radixPrefix.first, res.radix)
                    Assertions.assertEquals(integerText, res.integer)
                    Assertions.assertNull(res.decimal)
                    Assertions.assertNull(res.exponent)
                    Assertions.assertEquals(numberText, res.toString())
                }
            }
        }

        // Integer + Decimal
        for (radixPrefix in radix.zip(prefixes)) {
            for (integer in integers) {
                for (decimal in integers) {
                    for (underscore in 0..1) {
                        var integerText = integer.toString(radixPrefix.first)
                        var decimalText = decimal.toString(radixPrefix.first)

                        if (underscore == 1) {
                            integerText = underscoreNumber(integerText)
                            decimalText = underscoreNumber(decimalText)
                        }

                        val numberText = "${radixPrefix.second}$integerText${NumberNode.DecimalSeparator}$decimalText"

                        val res = NumberNode.parseAnyNumberDefaultDecimal(
                            LexemParser(CustomStringReader.from(numberText))
                        )

                        Assertions.assertNotNull(res, numberText)
                        res as NumberNode

                        Assertions.assertEquals(numberText, res.content)
                        Assertions.assertEquals(true, res.prefix)
                        Assertions.assertEquals(radixPrefix.first, res.radix)
                        Assertions.assertEquals(integerText, res.integer)
                        Assertions.assertEquals(decimalText, res.decimal)
                        Assertions.assertNull(res.exponent)
                        Assertions.assertEquals(numberText, res.toString())
                    }
                }
            }
        }

        // Integer + Exponent
        for (radixPrefix in radix.zip(prefixes)) {
            for (integer in integers) {
                for (exponent in integers) {
                    val expLetters = when (radixPrefix.first) {
                        2 -> NumberNode.BinaryExponentSeparator
                        8 -> NumberNode.OctalExponentSeparator
                        10 -> NumberNode.DecimalExponentSeparator
                        else -> NumberNode.HexadecimalExponentSeparator
                    }

                    for (exponentLetter in expLetters) {
                        for (sign in 0..2) {
                            for (underscore in 0..1) {
                                var integerText = integer.toString(radixPrefix.first)
                                var exponentText = exponent.toString(radixPrefix.first)

                                if (underscore == 1) {
                                    integerText = underscoreNumber(integerText)
                                    exponentText = underscoreNumber(exponentText)
                                }

                                val signText = when (sign) {
                                    2 -> "-"
                                    1 -> "+"
                                    else -> ""
                                }

                                val numberText =
                                        "${radixPrefix.second}$integerText$exponentLetter$signText$exponentText"

                                val res = NumberNode.parseAnyNumberDefaultDecimal(
                                    LexemParser(
                                        CustomStringReader.from(
                                            numberText
                                        )
                                    )
                                )

                                Assertions.assertNotNull(res, numberText)
                                res as NumberNode

                                Assertions.assertEquals(numberText, res.content)
                                Assertions.assertEquals(true, res.prefix)
                                Assertions.assertEquals(radixPrefix.first, res.radix)
                                Assertions.assertEquals(integerText, res.integer)
                                Assertions.assertNull(res.decimal)
                                Assertions.assertNotNull(res.exponent)
                                Assertions.assertEquals(exponentLetter, res.exponent!!.letter)
                                Assertions.assertEquals(when (sign) {
                                    2 -> '-'
                                    1 -> '+'
                                    else -> null
                                }, res.exponent!!.sign)
                                Assertions.assertEquals(exponentText, res.exponent!!.value)
                                Assertions.assertEquals(numberText, res.toString())
                            }
                        }
                    }
                }
            }
        }

        // Integer + Decimal + Exponent
        for (radixPrefix in radix.zip(prefixes)) {
            for (integer in integers) {
                for (decimal in integers) {
                    for (exponent in integers) {
                        val expLetters = when (radixPrefix.first) {
                            2 -> NumberNode.BinaryExponentSeparator
                            8 -> NumberNode.OctalExponentSeparator
                            10 -> NumberNode.DecimalExponentSeparator
                            else -> NumberNode.HexadecimalExponentSeparator
                        }

                        for (exponentLetter in expLetters) {
                            for (sign in 0..2) {
                                for (underscore in 0..1) {
                                    var integerText = integer.toString(radixPrefix.first)
                                    var decimalText = decimal.toString(radixPrefix.first)
                                    var exponentText = exponent.toString(radixPrefix.first)

                                    if (underscore == 1) {
                                        integerText = underscoreNumber(integerText)
                                        decimalText = underscoreNumber(decimalText)
                                        exponentText = underscoreNumber(exponentText)
                                    }

                                    val signText = when (sign) {
                                        2 -> "-"
                                        1 -> "+"
                                        else -> ""
                                    }

                                    val numberText =
                                            "${radixPrefix.second}$integerText${NumberNode.DecimalSeparator}$decimalText$exponentLetter$signText$exponentText"

                                    val res = NumberNode.parseAnyNumberDefaultDecimal(
                                        LexemParser(
                                            CustomStringReader.from(
                                                numberText
                                            )
                                        )
                                    )

                                    Assertions.assertNotNull(res, numberText)
                                    res as NumberNode

                                    Assertions.assertEquals(numberText, res.content)
                                    Assertions.assertEquals(true, res.prefix)
                                    Assertions.assertEquals(radixPrefix.first, res.radix)
                                    Assertions.assertEquals(integerText, res.integer)
                                    Assertions.assertEquals(decimalText, res.decimal)
                                    Assertions.assertNotNull(res.exponent)
                                    Assertions.assertEquals(exponentLetter, res.exponent!!.letter)
                                    Assertions.assertEquals(when (sign) {
                                        2 -> '-'
                                        1 -> '+'
                                        else -> null
                                    }, res.exponent!!.sign)
                                    Assertions.assertEquals(exponentText, res.exponent!!.value)
                                    Assertions.assertEquals(numberText, res.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `parse correct no-prefixed decimal numbers`() {
        // Integer
        for (integer in integers) {
            for (underscore in 0..1) {
                var integerText = integer.toString()

                if (underscore == 1) {
                    integerText = underscoreNumber(integerText)
                }

                val numberText = integerText

                val res = NumberNode.parseAnyNumberDefaultDecimal(
                    LexemParser(
                        CustomStringReader.from(
                            numberText
                        )
                    )
                )

                Assertions.assertNotNull(res, numberText)
                res as NumberNode

                Assertions.assertEquals(numberText, res.content)
                Assertions.assertEquals(false, res.prefix)
                Assertions.assertEquals(10, res.radix)
                Assertions.assertEquals(integerText, res.integer)
                Assertions.assertNull(res.decimal)
                Assertions.assertNull(res.exponent)
                Assertions.assertEquals(numberText, res.toString())
            }
        }

        // Integer + Decimal
        for (integer in integers) {
            for (decimal in integers) {
                for (underscore in 0..1) {
                    var integerText = integer.toString()
                    var decimalText = decimal.toString()

                    if (underscore == 1) {
                        integerText = underscoreNumber(integerText)
                        decimalText = underscoreNumber(decimalText)
                    }

                    val numberText = "$integerText${NumberNode.DecimalSeparator}$decimalText"

                    val res = NumberNode.parseAnyNumberDefaultDecimal(
                        LexemParser(
                            CustomStringReader.from(
                                numberText
                            )
                        )
                    )

                    Assertions.assertNotNull(res, numberText)
                    res as NumberNode

                    Assertions.assertEquals(numberText, res.content)
                    Assertions.assertEquals(false, res.prefix)
                    Assertions.assertEquals(10, res.radix)
                    Assertions.assertEquals(integerText, res.integer)
                    Assertions.assertEquals(decimalText, res.decimal)
                    Assertions.assertNull(res.exponent)
                    Assertions.assertEquals(numberText, res.toString())
                }
            }
        }

        // Integer + Exponent
        for (integer in integers) {
            for (exponent in integers) {
                for (exponentLetter in NumberNode.DecimalExponentSeparator) {
                    for (sign in 0..2) {
                        for (underscore in 0..1) {
                            var integerText = integer.toString()
                            var exponentText = exponent.toString()

                            if (underscore == 1) {
                                integerText = underscoreNumber(integerText)
                                exponentText = underscoreNumber(exponentText)
                            }

                            val signText = when (sign) {
                                2 -> "-"
                                1 -> "+"
                                else -> ""
                            }

                            val numberText = "$integerText$exponentLetter$signText$exponentText"

                            val res = NumberNode.parseAnyNumberDefaultDecimal(
                                LexemParser(
                                    CustomStringReader.from(
                                        numberText
                                    )
                                )
                            )

                            Assertions.assertNotNull(res, numberText)
                            res as NumberNode

                            Assertions.assertEquals(numberText, res.content)
                            Assertions.assertEquals(false, res.prefix)
                            Assertions.assertEquals(10, res.radix)
                            Assertions.assertEquals(integerText, res.integer)
                            Assertions.assertNull(res.decimal)
                            Assertions.assertNotNull(res.exponent)
                            Assertions.assertEquals(exponentLetter, res.exponent!!.letter)
                            Assertions.assertEquals(when (sign) {
                                2 -> '-'
                                1 -> '+'
                                else -> null
                            }, res.exponent!!.sign)
                            Assertions.assertEquals(exponentText, res.exponent!!.value)
                            Assertions.assertEquals(numberText, res.toString())
                        }
                    }
                }
            }
        }

        // Integer + Decimal + Exponent
        for (integer in integers) {
            for (decimal in integers) {
                for (exponent in integers) {
                    for (exponentLetter in NumberNode.DecimalExponentSeparator) {
                        for (sign in 0..2) {
                            for (underscore in 0..1) {
                                var integerText = integer.toString()
                                var decimalText = decimal.toString()
                                var exponentText = exponent.toString()

                                if (underscore == 1) {
                                    integerText = underscoreNumber(integerText)
                                    decimalText = underscoreNumber(decimalText)
                                    exponentText = underscoreNumber(exponentText)
                                }

                                val signText = when (sign) {
                                    2 -> "-"
                                    1 -> "+"
                                    else -> ""
                                }

                                val numberText =
                                        "$integerText${NumberNode.DecimalSeparator}$decimalText$exponentLetter$signText$exponentText"

                                val res = NumberNode.parseAnyNumberDefaultDecimal(
                                    LexemParser(
                                        CustomStringReader.from(
                                            numberText
                                        )
                                    )
                                )

                                Assertions.assertNotNull(res, numberText)
                                res as NumberNode

                                Assertions.assertEquals(numberText, res.content)
                                Assertions.assertEquals(false, res.prefix)
                                Assertions.assertEquals(10, res.radix)
                                Assertions.assertEquals(integerText, res.integer)
                                Assertions.assertEquals(decimalText, res.decimal)
                                Assertions.assertNotNull(res.exponent)
                                Assertions.assertEquals(exponentLetter, res.exponent!!.letter)
                                Assertions.assertEquals(when (sign) {
                                    2 -> '-'
                                    1 -> '+'
                                    else -> null
                                }, res.exponent!!.sign)
                                Assertions.assertEquals(exponentText, res.exponent!!.value)
                                Assertions.assertEquals(numberText, res.toString())
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `parse correct number followed by an access expression`() {
        val numberText = "356"
        val text = "$numberText${AccessExpressionNode.accessCharacter}a"
        val res = NumberNode.parseAnyNumberDefaultDecimal(
            LexemParser(
                CustomStringReader.from(
                    text
                )
            )
        )

        Assertions.assertNotNull(res, numberText)
        res as NumberNode

        Assertions.assertEquals(numberText, res.content)
        Assertions.assertEquals(false, res.prefix)
        Assertions.assertEquals(10, res.radix)
        Assertions.assertEquals(numberText, res.integer)
        Assertions.assertNull(res.decimal)
        Assertions.assertNull(res.exponent)
        Assertions.assertEquals(numberText, res.toString())
    }

    @Test
    fun `parse incorrect numbers with bad underscores`() {
        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0_"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }

        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0d_0"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }

        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0d0_"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }

        // Note: "0._0" is interpreted as a Number(0) followed by a property access (._0)

        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0.0_"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }

        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0.0e+_0"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }

        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0.0e0_"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }
    }

    @Test
    fun `parse incorrect numbers with no integer before decimal or exponent`() {
        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0d.0"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }

        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0de+0"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }
    }

    @Test
    fun `parse incorrect numbers with no decimal after separator`() {
        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0d0."
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }

        // Note: "0d0.e+0" is interpreted as a Number(0) followed by a property access (.e), an operator(+) and another number(0)
    }

    @Test
    fun `parse incorrect numbers with no exponent after the letter or sign`() {
        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0d0.0e"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }

        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0d0.0e+"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }

        Assertions.assertThrows(AngmarParserException::class.java) {
            val numberText = "0d0.0e-"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        }
    }

    @Test
    @Disabled
    fun `test to see the logs`() {
        try {
            // Change this code by one of the tests above that throw an error.
            val numberText = "0de+0"
            NumberNode.parseAnyNumberDefaultDecimal(
                LexemParser(
                    CustomStringReader.from(
                        numberText
                    )
                )
            )
        } catch (e: AngmarParserException) {
            e.logMessage()
        }
    }

    // AUXILIARY METHODS ------------------------------------------------------

    /**
     * Underscores a number.
     */
    private fun underscoreNumber(number: String): String {
        val res = number.split("").joinToString(NumberNode.DigitSeparator)
        return res.substring(1 until res.length - 1)
    }
}