package org.lexem.angmar.parser.commons

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.commons.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*

/**
 * Parser for Unicode escapes.
 */
internal class UnicodeEscapeNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var hasBrackets = false
    var value = 0

    override fun toString() = if (hasBrackets) {
        "$escapeStart$startBracket $value $endBracket"
    } else {
        "$escapeStart$value"
    }

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("hasBrackets", hasBrackets)
        result.addProperty("value", value.toLong())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            UnicodeEscapeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        internal const val escapeStart = "${EscapeNode.startToken}u"
        internal const val startBracket = "{"
        internal const val endBracket = "}"
        private const val unicodePointInBytes = 6

        // METHODS ------------------------------------------------------------

        /**
         * Parses a unicode escape.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): UnicodeEscapeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = UnicodeEscapeNode(parser, parent, parentSignal)

            if (!parser.readText(escapeStart)) {
                return null
            }

            val value: String

            if (parser.readText(startBracket)) {
                result.hasBrackets = true
                WhitespaceNoEOLNode.parse(parser)

                value = StringBuilder().apply {
                    repeat(unicodePointInBytes) { index ->
                        val ch = parser.readAnyChar(NumberNode.hexadecimalDigits) ?: let {
                            if (index == 0) {
                                // Try getting the whole escape.
                                val cursor = parser.reader.saveCursor()
                                WhitespaceNoEOLNode.parse(parser)
                                if (!parser.readText(endBracket)) {
                                    cursor.restore()
                                }

                                // Get the ws and the close bracket.
                                throw AngmarParserException(
                                        AngmarParserExceptionType.UnicodeEscapeWithBracketsWithoutValue,
                                        "Unicode escapes with brackets require at least one hexadecimal character after the open bracket '$startBracket'.") {
                                    val fullText = parser.reader.readAllText()
                                    addSourceCode(fullText, parser.reader.getSource()) {
                                        title = Consts.Logger.codeTitle
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    }
                                    addSourceCode(fullText, null) {
                                        title = Consts.Logger.hintTitle
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                        message = "Try removing the escape"
                                    }
                                }
                            }

                            return@repeat
                        }
                        append(ch)
                    }
                }.toString()

                WhitespaceNoEOLNode.parse(parser)

                if (!parser.readText(endBracket)) {
                    val cursor = parser.reader.saveCursor()
                    if (parser.readAnyChar(NumberNode.hexadecimalDigits) != null) {
                        while (parser.readAnyChar(NumberNode.hexadecimalDigits) != null);

                        throw AngmarParserException(
                                AngmarParserExceptionType.UnicodeEscapeWithBracketsWithMoreDigitsThanAllowed,
                                "Unicode escapes with brackets can have at most $unicodePointInBytes hexadecimal digits.") {
                            val fullText = parser.reader.readAllText()
                            addSourceCode(fullText, parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(initCursor.position(), cursor.position() - 1)
                            }
                            addSourceCode(fullText, null) {
                                title = Consts.Logger.hintTitle
                                highlightSection(cursor.position(), parser.reader.currentPosition() - 1)
                                message =
                                        "Try removing ${if (cursor.position() == parser.reader.currentPosition() - 1) {
                                            "this digit"
                                        } else {
                                            "these digits"
                                        }}"
                            }
                        }
                    }

                    throw AngmarParserException(AngmarParserExceptionType.UnicodeEscapeWithBracketsWithoutEndToken,
                            "Unicode escapes with brackets require the end bracket '$endBracket' to finish the escape.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding the end bracket '$endBracket' here"
                        }
                    }
                }
            } else {
                value = StringBuilder().apply {
                    repeat(unicodePointInBytes) { index ->
                        val ch = parser.readAnyChar(NumberNode.hexadecimalDigits) ?: let {
                            if (index == 0) {
                                throw AngmarParserException(AngmarParserExceptionType.UnicodeEscapeWithoutValue,
                                        "Unicode escapes require $unicodePointInBytes hexadecimal characters after the escape token '$escapeStart'.") {
                                    val fullText = parser.reader.readAllText()
                                    addSourceCode(fullText, parser.reader.getSource()) {
                                        title = Consts.Logger.codeTitle
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    }
                                    addSourceCode(fullText, null) {
                                        title = Consts.Logger.hintTitle
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                        message = "Try removing the escape"
                                    }
                                }
                            } else {
                                val verb = if (index == 1) {
                                    "was"
                                } else {
                                    "were"
                                }

                                throw AngmarParserException(
                                        AngmarParserExceptionType.UnicodeEscapeWithFewerDigitsThanAllowed,
                                        "Unicode escapes require $unicodePointInBytes hexadecimal characters after the escape token '$startBracket', but only $index $verb encountered.") {
                                    val fullText = parser.reader.readAllText()
                                    addSourceCode(fullText, parser.reader.getSource()) {
                                        title = Consts.Logger.codeTitle
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    }
                                    addSourceCode(fullText, null) {
                                        title = Consts.Logger.hintTitle
                                        highlightCursorAt(initCursor.position() + escapeStart.length)
                                        message =
                                                "Try adding ${unicodePointInBytes - index} ${if (unicodePointInBytes - index > 1) {
                                                    "zeroes"
                                                } else {
                                                    "zero"
                                                }} '0' here to complete the Unicode point"
                                    }
                                    addSourceCode(fullText, null) {
                                        title = Consts.Logger.hintTitle
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                        message = "Try removing the escape"
                                    }
                                }
                            }
                        }
                        append(ch)
                    }
                }.toString()
            }

            result.value = value.toUpperCase().toInt(16)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
