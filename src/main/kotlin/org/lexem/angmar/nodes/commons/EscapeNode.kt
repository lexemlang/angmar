package org.lexem.angmar.nodes.commons

import es.jtp.kterm.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.config.Consts
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode

/**
 * Parser for escaped characters.
 */
class EscapeNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    var unicode: UnicodeEscapeNode? = null
    var value: String? = null

    val isUnicode get() = unicode != null

    override fun toString() = if (!isUnicode) {
        "$escapeStart$value"
    } else {
        unicode.toString()
    }

    override fun toTree(printer: TreeLikePrinter) {
        if (unicode == null) {
            printer.addField("value", value)
        } else {
            printer.addField("value", unicode)
        }
    }

    companion object {
        private val nodeType = NodeType.Escape
        internal const val escapeStart = "\\"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an escape.
         */
        fun parse(parser: LexemParser): EscapeNode? {
            parser.fromBuffer<EscapeNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = EscapeNode(parser)

            val unicode = UnicodeEscapeNode.parse(parser)
            if (unicode != null) {
                result.unicode = unicode
                return parser.finalizeNode(result, initCursor)
            }

            if (!parser.readText(escapeStart)) {
                return null
            }

            result.value = WhitespaceNode.readEndOfLine(parser).takeIf { it != null && it != "" }
                    ?: parser.reader.currentChar()?.toString() ?: let {
                        throw AngmarParserException(Logger.build(
                            "Escapes require a character after the escape token '$escapeStart' but the end-of-file (EOF) was encountered."
                        ) {
                            addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                title(Consts.Logger.codeTitle)
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightAt(initCursor.position() + 1)
                                message("Try adding a character here")
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightAt(initCursor.position())
                                message("Try removing the escape token '$escapeStart' here")
                            }
                        })
                    }

            parser.reader.advance()
            return parser.finalizeNode(result, initCursor)
        }
    }
}