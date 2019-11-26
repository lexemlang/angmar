package org.lexem.angmar.parser.commons

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.commons.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.UnicodeEscapeNode.Companion.endBracket


/**
 * Parser for quoted identifiers i.e `like this`.
 */
internal class QuotedIdentifierNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    val texts = mutableListOf<String>()
    val escapes = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(startQuote)
        for (i in 0 until texts.size - 1) {
            append(texts[i])
            append(escapes[i])
        }
        append(texts.last())
        append(endQuote)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("texts", SerializationUtils.stringListToTest(texts))
        result.add("escapes", SerializationUtils.listToTest(escapes))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            QuotedIdentifierAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startQuote = "`"
        const val endQuote = "`"
        private const val notAllowedChars = "`${EscapeNode.startToken}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a quoted identifier.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): QuotedIdentifierNode? {
            parser.fromBuffer(parser.reader.currentPosition(), QuotedIdentifierNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = QuotedIdentifierNode(parser, parent, parentSignal)

            if (!parser.readText(startQuote)) {
                return null
            }

            result.texts.add(readStringSection(parser) ?: "")

            while (true) {
                result.escapes.add(Commons.parseSimpleEscape(parser, result,
                        result.escapes.size + QuotedIdentifierAnalyzer.signalEndFirstEscape) ?: break)
                result.texts.add(readStringSection(parser) ?: "")
            }

            if (result.texts.size == 1 && result.texts.first().isEmpty()) {
                parser.readText(endBracket)
                throw AngmarParserException(AngmarParserExceptionType.QuotedIdentifiersEmpty,
                        "Quoted identifiers require at least one valid character.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition())
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding a character here"
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition())
                        message = "Try removing the quoted identifier"
                    }
                }
            }

            if (!parser.readText(endQuote)) {
                if (!parser.readText(endBracket)) {
                    throw AngmarParserException(AngmarParserExceptionType.QuotedIdentifiersWithoutEndQuote,
                            "Quoted identifiers require the end quote '$endQuote' to finish the identifier.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding the end quote '$endQuote' here"
                        }
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Reads a valid string section.
         */
        private fun readStringSection(parser: LexemParser): String? {
            val initCursor = parser.reader.saveCursor()
            val result = StringBuilder()

            var ch = parser.readNegativeAnyChar(notAllowedChars) ?: return null
            if (ch in WhitespaceNode.endOfLineChars) {
                initCursor.restore()
                return null
            }
            result.append(ch)

            while (true) {
                ch = parser.readNegativeAnyChar(notAllowedChars) ?: return result.toString()
                if (ch in WhitespaceNode.endOfLineChars) {
                    throw AngmarParserException(AngmarParserExceptionType.QuotedIdentifiersMultilineNotAllowed,
                            "Quoted identifiers cannot be multiline.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(parser.reader.currentPosition() - 1)
                            message = "Try removing this end-of-line character"
                        }
                    }
                }
                result.append(ch)
            }
        }
    }
}
