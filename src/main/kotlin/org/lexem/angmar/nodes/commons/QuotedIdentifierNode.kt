package org.lexem.angmar.nodes.commons

import es.jtp.kterm.Logger
import org.lexem.angmar.LexemParser
import org.lexem.angmar.config.Consts
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.UnicodeEscapeNode.Companion.endBracket


/**
 * Parser for uoted identifiers i.e `like this`.
 */
class QuotedIdentifierNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    val texts = mutableListOf<String>()
    val escapes = mutableListOf<EscapeNode>()

    override fun toString() = StringBuilder().apply {
        append(startQuote)
        for (i in 0 until texts.size - 1) {
            append(texts[i])
            append(escapes[i])
        }
        append(texts.last())
        append(endQuote)
    }.toString()


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("texts", texts)
        printer.addField("escapes", escapes)
    }

    companion object {
        private val nodeType = NodeType.QuotedIdentifier
        const val startQuote = "`"
        const val endQuote = "`"
        private const val notAllowedChars = "`${EscapeNode.escapeStart}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a quoted identifier.
         */
        fun parse(parser: LexemParser): QuotedIdentifierNode? {
            parser.fromBuffer<QuotedIdentifierNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = QuotedIdentifierNode(parser)

            if (!parser.readText(startQuote)) {
                return null
            }

            result.texts.add(readStringSection(parser) ?: "")

            while (true) {
                result.escapes.add(EscapeNode.parse(parser) ?: break)
                result.texts.add(readStringSection(parser) ?: "")
            }

            if (result.texts.size == 1 && result.texts.first().isEmpty()) {
                parser.readText(endBracket)
                throw AngmarParserException(
                    Logger.build("Quoted identifiers require at least one valid character.") {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition())
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightAt(parser.reader.currentPosition())
                            message("Try adding a character here")
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition())
                            message("Try removing the quoted identifier")
                        }
                    })
            }

            if (!parser.readText(endQuote)) {
                if (!parser.readText(endBracket)) {
                    throw AngmarParserException(
                        Logger.build("Quoted identifiers the end quote '$endQuote' to finish the identifier.") {
                            addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                title(Consts.Logger.codeTitle)
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightAt(parser.reader.currentPosition())
                                message("Try adding the end quote '$endQuote' here")
                            }
                        })
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
                    throw AngmarParserException(Logger.build("Quoted identifiers cannot be multiline.") {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightAt(parser.reader.currentPosition() - 1)
                            message("Try removing this end-of-line character")
                        }
                    })
                }
                result.append(ch)
            }
        }
    }
}