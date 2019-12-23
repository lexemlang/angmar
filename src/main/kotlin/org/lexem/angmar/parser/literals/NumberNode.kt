package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*

/**
 * Parser for numbers in different formats.
 */
internal class NumberNode private constructor(parser: LexemParser, parent: ParserNode) : ParserNode(parser, parent) {
    var radix = 2
    var integer = ""
    var decimal: String? = null
    var exponentSign = true
    var exponent: String? = null

    override fun toString() = StringBuilder().apply {
        when (radix) {
            2 -> append(binaryPrefix)
            8 -> append(octalPrefix)
            10 -> append(decimalPrefix)
            16 -> append(hexadecimalPrefix)
            else -> throw AngmarUnreachableException()
        }

        append(integer)

        if (decimal != null) {
            append(decimalSeparator)
            append(decimal)
        }

        if (exponent != null) {
            append(hexadecimalExponentSeparator.first())
            append(if (exponentSign) {
                exponentPositiveSign
            } else {
                exponentNegativeSign
            })
            append(exponent)
        }

    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("radix", radix.toLong())
        result.addProperty("integer", integer)
        result.addProperty("decimal", decimal)

        if (exponent != null) {
            result.addProperty("exponentSign", exponentSign)
            result.addProperty("exponent", exponent)
        }

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) = NumberCompiler.compile(parent, parentSignal, this)

    companion object {
        const val digitSeparator = "_"
        const val decimalSeparator = "."
        const val binaryPrefix = "0b"
        const val octalPrefix = "0o"
        const val decimalPrefix = "0d"
        const val hexadecimalPrefix = "0x"
        const val binaryExponentSeparator = "eEpP"
        const val octalExponentSeparator = "eEpP"
        const val decimalExponentSeparator = "eEpP"
        const val hexadecimalExponentSeparator = "pP"
        const val exponentPositiveSign = "+"
        const val exponentNegativeSign = "-"
        val binaryDigits = "01"
        val octalDigits = "01234567"
        val decimalDigits = "0123456789"
        val hexadecimalDigits = "0123456789abcdefABCDEF"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a number in any radix with the Decimal(radix 10) as a default (without prefix).
         */
        fun parseAnyNumberDefaultDecimal(parser: LexemParser, parent: ParserNode): NumberNode? {
            val initCursor = parser.reader.saveCursor()
            val result =
                    parseDecimal(parser, parent, 2, binaryPrefix, true, binaryExponentSeparator, ::readBinaryInteger)
                            ?: parseDecimal(parser, parent, 8, octalPrefix, true, octalExponentSeparator,
                                    ::readOctalInteger) ?: parseDecimal(parser, parent, 16, hexadecimalPrefix, true,
                                    hexadecimalExponentSeparator, ::readHexadecimalInteger) ?: parseDecimal(parser,
                                    parent, 10, decimalPrefix, false, decimalExponentSeparator, ::readDecimalInteger)
                            ?: return null

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses a number in the specified radix with or without prefix.
         */
        fun parseAnyNumberInSpecifiedRadix(parser: LexemParser, parent: ParserNode, radix: Int): NumberNode? {
            val initCursor = parser.reader.saveCursor()
            val result = when (radix) {
                2 -> parseDecimal(parser, parent, 2, binaryPrefix, false, binaryExponentSeparator, ::readBinaryInteger)
                8 -> parseDecimal(parser, parent, 8, octalPrefix, false, octalExponentSeparator, ::readOctalInteger)
                10 -> parseDecimal(parser, parent, 10, decimalPrefix, false, decimalExponentSeparator,
                        ::readDecimalInteger)
                16 -> parseDecimal(parser, parent, 16, hexadecimalPrefix, false, hexadecimalExponentSeparator,
                        ::readHexadecimalInteger)
                else -> null
            } ?: return null

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses an integer number in any radix with the Decimal(radix 10) as a default (without prefix).
         */
        fun parseAnyIntegerDefaultDecimal(parser: LexemParser, parent: ParserNode): NumberNode? {
            val initCursor = parser.reader.saveCursor()
            val result =
                    parseInteger(parser, parent, 2, binaryPrefix, true, ::readBinaryInteger) ?: parseInteger(parser,
                            parent, 8, octalPrefix, true, ::readOctalInteger) ?: parseInteger(parser, parent, 16,
                            hexadecimalPrefix, true, ::readHexadecimalInteger) ?: parseInteger(parser, parent, 10,
                            decimalPrefix, false, ::readDecimalInteger) ?: return null

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses an integer in the specified radix with or without prefix.
         */
        fun parseAnyIntegerInSpecifiedRadix(parser: LexemParser, parent: ParserNode, radix: Int): NumberNode? {
            val initCursor = parser.reader.saveCursor()
            val result = when (radix) {
                2 -> parseInteger(parser, parent, 2, binaryPrefix, false, ::readBinaryInteger)
                8 -> parseInteger(parser, parent, 8, octalPrefix, false, ::readOctalInteger)
                10 -> parseInteger(parser, parent, 10, decimalPrefix, false, ::readDecimalInteger)
                16 -> parseInteger(parser, parent, 16, hexadecimalPrefix, false, ::readHexadecimalInteger)
                else -> null
            } ?: return null

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Reads a binary integer.
         */
        fun readBinaryInteger(parser: LexemParser) = readInteger(parser, binaryDigits)

        /**
         * Reads an octal integer.
         */
        fun readOctalInteger(parser: LexemParser) = readInteger(parser, octalDigits)

        /**
         * Reads a decimal integer.
         */
        fun readDecimalInteger(parser: LexemParser) = readInteger(parser, decimalDigits)

        /**
         * Reads an hexadecimal integer.
         */
        fun readHexadecimalInteger(parser: LexemParser) = readInteger(parser, hexadecimalDigits)

        private fun parseDecimal(parser: LexemParser, parent: ParserNode, radix: Int, prefixText: String,
                isPrefixCompulsory: Boolean, exponentSeparator: String,
                integerParser: (LexemParser) -> String?): NumberNode? {
            val initCursor = parser.reader.saveCursor()
            val result = NumberNode(parser, parent)
            result.radix = radix

            val prefix = parser.readText(prefixText)

            if (!prefix && isPrefixCompulsory) {
                return null
            }

            // Integer
            val integer = integerParser(parser)
            if (integer == null) {
                if (prefix) {
                    throw AngmarParserException(AngmarParserExceptionType.NumberWithoutDigitAfterPrefix,
                            "Numbers require at least one digit after the prefix '$prefix'") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            message = "Try removing the prefix '$prefix'"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding a '0' here"
                        }
                    }
                }

                return null
            }

            result.integer = integer

            // Decimal
            val preDecimalDotCursor = parser.reader.saveCursor()
            if (parser.readText(decimalSeparator)) {
                val decimal = integerParser(parser)
                if (decimal == null) {
                    // If it is followed by an identifier, it returns because it could be an access expression, i.e. 15.px
                    if (!Commons.checkIdentifier(parser)) {
                        throw AngmarParserException(AngmarParserExceptionType.NumberWithoutDigitAfterDecimalSeparator,
                                "Numbers require at least one digit after the decimal separator '$decimalSeparator'") {
                            val fullText = parser.reader.readAllText()
                            addSourceCode(fullText, parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(fullText, null) {
                                title = Consts.Logger.hintTitle
                                highlightSection(parser.reader.currentPosition() - 1)
                                message = "Try removing the digit separator '$digitSeparator'"
                            }
                            addSourceCode(fullText, null) {
                                title = Consts.Logger.hintTitle
                                highlightCursorAt(parser.reader.currentPosition())
                                message = "Try adding a '0' here"
                            }
                        }
                    }

                    preDecimalDotCursor.restore()
                    return result
                }

                result.decimal = decimal
            }

            // Exponent
            val exponentLetter = parser.readAnyChar(exponentSeparator)
            if (exponentLetter != null) {
                val sign = parser.readAnyChar("+-")
                val value = integerParser(parser) ?: throw AngmarParserException(
                        AngmarParserExceptionType.NumberWithoutDigitAfterExponentSeparator,
                        "Numbers require at least one digit after the exponent separator '$exponentLetter${sign
                                ?: ""}'") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }

                    if (sign != null) {
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(parser.reader.currentPosition() - 2, parser.reader.currentPosition() - 1)
                            message = "Try removing the '$exponentLetter$sign'"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding a '0' here"
                        }
                    } else {
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(parser.reader.currentPosition() - 1)
                            message = "Try removing the '$exponentLetter'"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding a '0' here"
                        }
                    }
                }

                result.exponentSign = sign != '-'
                result.exponent = value
            }

            return result
        }

        private fun parseInteger(parser: LexemParser, parent: ParserNode, radix: Int, prefixText: String,
                isPrefixCompulsory: Boolean, integerParser: (LexemParser) -> String?): NumberNode? {
            val initCursor = parser.reader.saveCursor()
            val result = NumberNode(parser, parent)
            result.radix = radix

            val prefix = parser.readText(prefixText)

            if (!prefix && isPrefixCompulsory) {
                return null
            }

            // Integer
            val integer = integerParser(parser)
            if (integer == null) {
                if (prefix) {
                    throw AngmarParserException(AngmarParserExceptionType.NumberIntegerWithoutDigitAfterPrefix,
                            "Numbers require at least one digit after the prefix '$prefix'") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            message = "Try removing the prefix '$prefix'"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding a '0' here"
                        }
                    }
                }

                return null
            }

            result.integer = integer

            return result
        }

        private fun readInteger(parser: LexemParser, digits: String): String? {
            val initCursor = parser.reader.saveCursor()
            val sb = StringBuilder()

            if (parser.readText(digitSeparator)) {
                throw AngmarParserException(AngmarParserExceptionType.NumberWithSequenceStartedWithADigitSeparator,
                        "A digit sequence cannot start with a digit separator '$digitSeparator'") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(parser.reader.currentPosition() - 1)
                        message = "Try removing the digit separator '$digitSeparator'"
                    }
                }
            }

            sb.append(parser.readAnyChar(digits) ?: return null)

            while (true) {
                val separator = parser.readText(digitSeparator)

                val ch = parser.readAnyChar(digits)
                if (ch == null) {
                    if (separator) {
                        throw AngmarParserException(
                                AngmarParserExceptionType.NumberWithSequenceEndedWithADigitSeparator,
                                "A digit sequence cannot end with a digit separator '$digitSeparator'") {
                            val fullText = parser.reader.readAllText()
                            addSourceCode(fullText, parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }

                            addSourceCode(fullText, null) {
                                title = Consts.Logger.hintTitle
                                highlightSection(parser.reader.currentPosition() - 1)
                                message = "Try removing the digit a digit separator '$digitSeparator'"
                            }
                        }
                    }

                    break
                }

                sb.append(ch)
            }


            return sb.toString()
        }
    }
}
