package org.lexem.angmar.nodes.commons

import es.jtp.kterm.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.config.Consts
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.literals.NumberNode

/**
 * Parser for Unicode escapes.
 */
class UnicodeEscapeNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    var hasBrackets = false
    var leftWhitespace: WhitespaceNoEOLNode? = null
    var rightWhitespace: WhitespaceNoEOLNode? = null
    var value = ""

    override fun toString() = if (hasBrackets) {
        "$escapeStart$startBracket$leftWhitespace$value$rightWhitespace$endBracket"
    } else {
        "$escapeStart$value"
    }


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("hasBrackets", hasBrackets)

        if (hasBrackets) {
            printer.addField("leftWhitespace", value)
            printer.addField("rightWhitespace", value)
        }

        printer.addField("value", value)
    }

    companion object {
        private val nodeType = NodeType.UnicodeEscape
        internal const val escapeStart = "${EscapeNode.escapeStart}u"
        internal const val startBracket = "{"
        internal const val endBracket = "}"
        private const val unicodePointInBytes = 8

        // METHODS ------------------------------------------------------------

        /**
         * Parses an Unicode escape.
         */
        fun parse(parser: LexemParser): UnicodeEscapeNode? {
            parser.fromBuffer<UnicodeEscapeNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = UnicodeEscapeNode(parser)

            if (!parser.readText(escapeStart)) {
                return null
            }

            if (parser.readText(startBracket)) {
                result.hasBrackets = true
                result.leftWhitespace = WhitespaceNoEOLNode.parseOrEmpty(parser)

                result.value = StringBuilder().apply {
                    repeat(unicodePointInBytes) { index ->
                        val ch = parser.readAnyChar(NumberNode.HexadecimalDigits) ?: let {
                            if (index == 0) {
                                // Try getting the whole escape.
                                val cursor = parser.reader.saveCursor()
                                WhitespaceNoEOLNode.parseOrEmpty(parser)
                                if (!parser.readText(endBracket)) {
                                    cursor.restore()
                                }

                                // Get the ws and the close bracket.
                                throw AngmarParserException(Logger.build(
                                    "Unicode escapes with brackets require at least one hexadecimal character after the open bracket '$startBracket'."
                                ) {
                                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                        title(Consts.Logger.codeTitle)
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    }
                                    addSourceCode(parser.reader.readAllText(), null) {
                                        title(Consts.Logger.hintTitle)
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                        message("Try removing the escape")
                                    }
                                })
                            }

                            return@repeat
                        }
                        append(ch)
                    }
                }.toString()

                result.rightWhitespace = WhitespaceNoEOLNode.parseOrEmpty(parser)

                if (!parser.readText(endBracket)) {
                    val cursor = parser.reader.saveCursor()
                    if (parser.readAnyChar(NumberNode.HexadecimalDigits) != null) {
                        while (parser.readAnyChar(NumberNode.HexadecimalDigits) != null);

                        throw AngmarParserException(Logger.build(
                            "Unicode escapes with brackets can have at most $unicodePointInBytes hexadecimal digits."
                        ) {
                            addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                title(Consts.Logger.codeTitle)
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightSection(cursor.position(), parser.reader.currentPosition() - 1)
                                message(
                                    "Try removing ${if (cursor.position() == parser.reader.currentPosition() - 1) {
                                        "this digit"
                                    } else {
                                        "these digits"
                                    }}"
                                )
                            }
                        })
                    }

                    throw AngmarParserException(Logger.build(
                        "Unicode escapes with brackets require the end bracket '$endBracket' to finish the escape."
                    ) {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightAt(parser.reader.currentPosition())
                            message("Try adding the end bracket '$endBracket' here")
                        }
                    })
                }
            } else {
                result.value = StringBuilder().apply {
                    repeat(unicodePointInBytes) { index ->
                        val ch = parser.readAnyChar(NumberNode.HexadecimalDigits) ?: let {
                            if (index == 0) {
                                throw AngmarParserException(Logger.build(
                                    "Unicode escapes require $unicodePointInBytes hexadecimal characters after the escape token '$escapeStart'."
                                ) {
                                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                        title(Consts.Logger.codeTitle)
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    }
                                    addSourceCode(parser.reader.readAllText(), null) {
                                        title(Consts.Logger.hintTitle)
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                        message("Try removing the escape")
                                    }
                                })
                            } else {
                                throw AngmarParserException(Logger.build(
                                    "Unicode escapes require $unicodePointInBytes hexadecimal characters after the escape token '$startBracket', but only ${index} ${if (index == 1) {
                                        "was"
                                    } else {
                                        "were"
                                    }} encountered."
                                ) {
                                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                        title(Consts.Logger.codeTitle)
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    }
                                    addSourceCode(parser.reader.readAllText(), null) {
                                        title(Consts.Logger.hintTitle)
                                        highlightAt(initCursor.position() + escapeStart.length)
                                        message("Try adding ${unicodePointInBytes - index} zeroes '0' here to complete the Unicode point")
                                    }
                                    addSourceCode(parser.reader.readAllText(), null) {
                                        title(Consts.Logger.hintTitle)
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                        message("Try removing the escape")
                                    }
                                })
                            }
                        }
                        append(ch)
                    }
                }.toString()
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}