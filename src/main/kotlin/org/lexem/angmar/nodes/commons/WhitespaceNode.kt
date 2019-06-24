package org.lexem.angmar.nodes.commons

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.ITreeLikePrintable
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode


/**
 * Parser for whitespaces.
 */
class WhitespaceNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
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
        private val nodeType = NodeType.Whitespace
        val endOfLineChars = setOf('\u000A', '\u000D', '\u0085', '\u2028', '\u2029')
        val whitespaceChars =
                setOf('\u0000', '\u0009', '\u000B', '\u000C', '\u0020', '\u00A0', '\u1680', '\u2000', '\u2001',
                        '\u2002', '\u2003', '\u2004', '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200A',
                        '\u202F', '\u205F', '\u3000', '\uFFFF')
        val complexEndOfLine = "\u000D\u000A"

        // METHODS ------------------------------------------------------------

        /**
         * Parses whitespace characters, including comments.
         */
        fun parse(parser: LexemParser): WhitespaceNode? {
            parser.fromBuffer<WhitespaceNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = WhitespaceNode(parser)

            while (true) {
                result.whitespaces.add(parseWhitespace(parser) ?: "")
                result.comments.add(CommentSingleLineNode.parse(parser) ?: CommentMultilineNode.parse(parser) ?: break)
            }

            if (result.whitespaces.size == 1 && result.whitespaces[0].isEmpty()) {
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses whitespace characters, including comments. In case of not parsing anything, it returns an empty node.
         */
        fun parseOrEmpty(parser: LexemParser): WhitespaceNode {
            var result = parse(parser)

            if (result == null) {
                val initCursor = parser.reader.saveCursor()
                val node = WhitespaceNode(parser)
                node.whitespaces.add("")
                result = parser.finalizeNode(node, initCursor)
            }

            return result
        }

        /**
         * Parse only whitespaces.
         */
        private fun parseWhitespace(parser: LexemParser): String? {
            val res = StringBuilder()
            while (true) {
                val ch = parser.readAnyChar(whitespaceChars) ?: parser.readAnyChar(endOfLineChars) ?: break
                res.append(ch)
            }

            return if (res.isEmpty()) {
                null
            } else {
                res.toString()
            }
        }

        /**
         * Checks whether there is an end-of-line character in the current position.
         */
        fun readEndOfLine(parser: LexemParser): String? {
            if (parser.reader.isEnd()) {
                return ""
            }

            if (parser.readText(complexEndOfLine)) {
                return complexEndOfLine
            }

            return parser.readAnyChar(endOfLineChars)?.toString()
        }
    }

    /**
     * Container for exponent section.
     */
    data class CommentOrWhitespace(var letter: Char, var sign: Char?, var value: String) :
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