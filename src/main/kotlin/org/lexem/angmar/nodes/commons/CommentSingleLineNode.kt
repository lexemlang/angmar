package org.lexem.angmar.nodes.commons

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.ITextReaderCursor
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode

/**
 * Parser for single-line comments.
 */
class CommentSingleLineNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    var initTextCursor: ITextReaderCursor = ITextReaderCursor.Empty
    var endTextCursor: ITextReaderCursor = ITextReaderCursor.Empty
    var eol: String = ""

    val text by lazy {
        parser.substring(initTextCursor, endTextCursor)
    }

    override fun toString() = StringBuilder().apply {
        append(SingleLineStartToken)
        append(text)
        append(eol)
    }.toString()


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("text", text)
        printer.addField("eol", eol)
    }

    companion object {
        private val nodeType = NodeType.CommentSingleLine
        const val SingleLineStartToken = "#-"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a single-line comment.
         */
        fun parse(parser: LexemParser): CommentSingleLineNode? {
            parser.fromBuffer<CommentSingleLineNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = CommentSingleLineNode(parser)

            if (!parser.readText(SingleLineStartToken)) {
                return null
            }

            result.initTextCursor = parser.reader.saveCursor()
            result.endTextCursor = result.initTextCursor

            while (true) {
                val eol = WhitespaceNode.readEndOfLine(parser)
                if (eol != null) {
                    result.eol = eol
                    break
                }

                parser.reader.advance()
                result.endTextCursor = parser.reader.saveCursor()
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}