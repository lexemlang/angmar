package org.lexem.angmar.nodes.commons

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode


/**
 * Parser for whitespaces and multiline comments without including end-of-line characters until one.
 */
class WhitespaceUntilEOLNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    var whitespace: WhitespaceNoEOLNode? = null
    var eol: String = ""

    override fun toString() = StringBuilder().apply {
        if (whitespace != null) {
            append(whitespace)
        }
        append(eol)
    }.toString()


    override fun toTree(printer: TreeLikePrinter) {
        printer.addOptionalField("whitespace", whitespace)
        printer.addField("eol", eol)
    }

    companion object {
        val nodeType = NodeType.WhitespaceUntilEOL

        // METHODS ------------------------------------------------------------

        /**
         * Parses whitespace characters and multiline comments.
         */
        fun parse(parser: LexemParser): WhitespaceUntilEOLNode? {
            parser.fromBuffer<WhitespaceUntilEOLNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = WhitespaceUntilEOLNode(parser)

            result.whitespace = WhitespaceNoEOLNode.parse(parser)
            result.eol = WhitespaceNode.readEndOfLine(parser) ?: let {
                initCursor.restore()
                return@parse null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}