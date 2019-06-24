package org.lexem.angmar.nodes.literals

import es.jtp.kterm.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.config.Consts
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.printer.ITreeLikePrintable
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.IdentifierNode

/**
 * Parser for numbers in different formats.
 */
class NumberNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    var prefix = false
    var radix = -1
    var integer = ""
    var decimal: String? = null
    var exponent: Exponent? = null

    override fun toString() = StringBuilder().apply {
        if (prefix) {
            when (radix) {
                2 -> append("0b")
                8 -> append("0o")
                10 -> append("0d")
                16 -> append("0x")
            }
        }

        append(integer)

        decimal?.let { decimal ->
            append('.')
            append(decimal)
        }

        exponent?.let { exponent ->
            append(exponent.letter)
            append(exponent.sign ?: "")
            append(exponent.value)
        }
    }.toString()


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("prefix", prefix)
        printer.addField("radix", radix)
        printer.addField("integer", integer)
        printer.addOptionalField("decimal", decimal)
        printer.addOptionalField("exponent", exponent)
    }

    companion object {
        private val nodeType = NodeType.Number
        const val DigitSeparator = "_"
        const val DecimalSeparator = "."
        const val BinaryPrefix = "0b"
        const val OctalPrefix = "0o"
        const val DecimalPrefix = "0d"
        const val HexadecimalPrefix = "0x"
        const val BinaryExponentSeparator = "eEpP"
        const val OctalExponentSeparator = "eEpP"
        const val DecimalExponentSeparator = "eEpP"
        const val HexadecimalExponentSeparator = "pP"
        val BinaryDigits = listOf('0'..'1')
        val OctalDigits = listOf('0'..'7')
        val DecimalDigits = listOf('0'..'9')
        val HexadecimalDigits = listOf('0'..'9', 'A'..'F', 'a'..'f')

        // METHODS ------------------------------------------------------------

        /**
         * Parses a number in any radix with the Decimal(radix 10) as a default (without prefix).
         */
        fun parseAnyNumberDefaultDecimal(parser: LexemParser): NumberNode? {
            val initCursor = parser.reader.saveCursor()
            val result = parseDecimal(parser, 2, BinaryPrefix, true, BinaryExponentSeparator) {
                parseInteger(it, BinaryDigits)
            } ?: parseDecimal(parser, 8, OctalPrefix, true, OctalExponentSeparator) {
                parseInteger(it, OctalDigits)
            } ?: parseDecimal(parser, 16, HexadecimalPrefix, true, HexadecimalExponentSeparator) {
                parseInteger(it, HexadecimalDigits)
            } ?: parseDecimal(parser, 10, DecimalPrefix, false, DecimalExponentSeparator) {
                parseInteger(it, DecimalDigits)
            } ?: return null

            return parser.finalizeNode(result, initCursor)
        }

        private fun parseDecimal(parser: LexemParser, radix: Int, prefix: String, isPrefixCompulsory: Boolean,
                                 exponentSeparator: String, integerParser: (LexemParser) -> String?): NumberNode? {
            parser.fromBuffer<NumberNode>(parser.reader.currentPosition(), nodeType)?.let {
                it.to.restore()
                return@parseDecimal it
            }

            val initCursor = parser.reader.saveCursor()
            val result = NumberNode(parser)

            result.prefix = parser.readText(prefix)
            result.radix = radix

            if (!result.prefix && isPrefixCompulsory) {
                return null
            }

            // Integer
            val integer = integerParser(parser)
            if (integer == null) {
                if (result.prefix) {
                    throw AngmarParserException(
                        Logger.build("Numbers require at least one digit after the prefix '$prefix'") {
                            addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                title(Consts.Logger.codeTitle)
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                message("Try removing the prefix '$prefix'")
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightAt(parser.reader.currentPosition())
                                message("Try adding a '0' here")
                            }
                        })
                }

                return null
            }

            result.integer = integer

            // Decimal
            val preDecimalDotCursor = parser.reader.saveCursor()
            if (parser.readText(DecimalSeparator)) {
                result.decimal = integerParser(parser)
                if (result.decimal == null) {
                    // If it is followed by an identifier, it returns because it could be an access expression, i.e. 15.px
                    if (!IdentifierNode.checkIdentifier(parser)) {
                        throw AngmarParserException(Logger.build(
                            "Numbers require at least one digit after the decimal separator '$DecimalSeparator'"
                        ) {
                            addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                title(Consts.Logger.codeTitle)
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightAt(parser.reader.currentPosition() - 1)
                                message("Try removing the digit separator '$DigitSeparator'")
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightAt(parser.reader.currentPosition())
                                message("Try adding a '0' here")
                            }
                        })
                    }

                    preDecimalDotCursor.restore()
                    return result
                }
            }

            // Exponent
            val exponentLetter = parser.readAnyChar(exponentSeparator)
            if (exponentLetter != null) {
                val sign = parser.readAnyChar("+-")
                val value = integerParser(parser) ?: throw AngmarParserException(Logger.build(
                    "Numbers require at least one digit after the exponent separator '$exponentLetter${sign
                        ?: ""}'"
                ) {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }

                    if (sign != null) {
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightSection(
                                parser.reader.currentPosition() - 2,
                                parser.reader.currentPosition() - 1
                            )
                            message("Try removing the '$exponentLetter$sign'")
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightAt(parser.reader.currentPosition())
                            message("Try adding a '0' here")
                        }
                    } else {
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightAt(parser.reader.currentPosition() - 1)
                            message("Try removing the '$exponentLetter'")
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightAt(parser.reader.currentPosition())
                            message("Try adding a '0' here")
                        }
                    }
                })


                val exponent = Exponent(exponentLetter, sign, value)
                result.exponent = exponent
            }

            return result
        }

        private fun parseInteger(parser: LexemParser, digits: List<CharRange>): String? {
            val initCursor = parser.reader.saveCursor()
            val sb = StringBuilder()

            if (parser.readText(DigitSeparator)) {
                throw AngmarParserException(
                    Logger.build("A digit sequence cannot start with a digit separator '$DigitSeparator'") {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightAt(parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightAt(parser.reader.currentPosition() - 1)
                            message("Try removing the digit separator '$DigitSeparator'")
                        }
                    })
            }

            sb.append(parser.readAnyChar(digits) ?: return null)

            while (true) {
                val separator = parser.readText(DigitSeparator)
                if (separator) {
                    sb.append(DigitSeparator)
                }

                val ch = parser.readAnyChar(digits)
                if (ch == null) {
                    if (separator) {
                        throw AngmarParserException(
                            Logger.build("A digit sequence cannot end with the separator '$DigitSeparator'") {
                                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                    title(Consts.Logger.codeTitle)
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                }

                                addSourceCode(parser.reader.readAllText(), null) {
                                    title(Consts.Logger.hintTitle)
                                    highlightAt(parser.reader.currentPosition() - 1)
                                    message("Try removing the digit separator '$DigitSeparator'")
                                }
                            })
                    }

                    break
                }

                sb.append(ch)
            }

            return sb.toString()
        }
    }

    /**
     * Container for exponent section.
     */
    data class Exponent(var letter: Char, var sign: Char?, var value: String) :
        ITreeLikePrintable {
        val signAsBoolean
            get() = sign != '-'

        override fun toTree(printer: TreeLikePrinter) {
            printer.addField("letter", letter)
            printer.addOptionalField("sign", sign)
            printer.addField("value", value)
        }
    }
}