package org.lexem.angmar.nodes.commons

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode

/**
 * Parser for whitespaces and multiline comments without including end-of-line characters.
 */
class WhitespaceNoEOLNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    val whitespaces = mutableListOf<String>()
    val comments = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        for (i in 0 until whitespaces.size) {
            append(whitespaces[i])

            if (i < comments.size) {
                append(comments[i])
            }
        }
    }.toString()


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("whitespaces", whitespaces)
        printer.addField("comments", comments)
    }

    companion object {
        private val nodeType = NodeType.WhitespaceWithoutEOL

        // METHODS ------------------------------------------------------------

        /**
         * Parses whitespace characters and multiline comments.
         */
        fun parse(parser: LexemParser): WhitespaceNoEOLNode? {
            parser.fromBuffer<WhitespaceNoEOLNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = WhitespaceNoEOLNode(parser)

            while (true) {
                result.whitespaces.add(parseWhitespace(parser) ?: "")
                result.comments.add(CommentMultilineNode.parse(parser) ?: break)
            }

            if (result.whitespaces.size == 1 && result.whitespaces[0].isEmpty()) {
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses whitespace characters and multiline comments. In case of not parsing anything, it returns an empty node.
         */
        fun parseOrEmpty(parser: LexemParser): WhitespaceNoEOLNode {
            var result = parse(parser)

            if (result == null) {
                val initCursor = parser.reader.saveCursor()
                result = parser.finalizeNode(WhitespaceNoEOLNode(parser), initCursor)
            }

            return result
        }

        /**
         * Parses only whitespaces.
         */
        private fun parseWhitespace(parser: LexemParser): String? {
            val res = StringBuilder()
            while (true) {
                val ch = parser.readAnyChar(WhitespaceNode.whitespaceChars) ?: break
                res.append(ch)
            }

            return if (res.isEmpty()) {
                null
            } else {
                res.toString()
            }
        }
    }
}