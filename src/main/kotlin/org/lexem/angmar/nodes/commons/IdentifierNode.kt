package org.lexem.angmar.nodes.commons

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.literals.BooleanNode
import org.lexem.angmar.nodes.literals.NilNode


/**
 * Parser for identifiers.
 */
class IdentifierNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    val simpleIdentifiers = mutableListOf<String>()
    var quotedIdentifier: QuotedIdentifierNode? = null

    val isQuotedIdentifier get() = quotedIdentifier != null

    override fun toString() = if (isQuotedIdentifier) {
        quotedIdentifier.toString()
    } else {
        simpleIdentifiers.joinToString(middleChar) { it }
    }


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("simpleIdentifiers", simpleIdentifiers)
    }

    companion object {
        private val nodeType = NodeType.Identifier
        internal val headChars = listOf('\u0041'..'\u005A', '\u005F'..'\u005F', '\u0061'..'\u007A', '\u00A8'..'\u00A8',
                '\u00AA'..'\u00AA', '\u00AD'..'\u00AD', '\u00AF'..'\u00AF', '\u00B2'..'\u00B5', '\u00B7'..'\u00BA',
                '\u00BC'..'\u00BE', '\u00C0'..'\u00D6', '\u00D8'..'\u00F6', '\u00F8'..'\u00FF', '\u0100'..'\u02FF',
                '\u0370'..'\u167F', '\u1681'..'\u180D', '\u180F'..'\u1DBF', '\u1E00'..'\u1FFF', '\u200B'..'\u200D',
                '\u202A'..'\u202E', '\u203F'..'\u2040', '\u2054'..'\u2054', '\u2060'..'\u206F', '\u2070'..'\u20CF',
                '\u2100'..'\u218F', '\u2460'..'\u24FF', '\u2776'..'\u2793', '\u2C00'..'\u2DFF', '\u2E80'..'\u2FFF',
                '\u3004'..'\u3007', '\u3021'..'\u302F', '\u3031'..'\u303F', '\u3040'..'\uD7FF', '\uF900'..'\uFD3D',
                '\uFD40'..'\uFDCF', '\uFDF0'..'\uFDFF', '\uFE10'..'\uFE1F', '\uFE30'..'\uFE44', '\uFE47'..'\uFFFD')
        internal val endChars = listOf('\u0030'..'\u0039', '\u0300'..'\u036F', '\u1DC0'..'\u1DFF', '\u20D0'..'\u20FF',
                '\uFE20'..'\uFE2F')
        internal const val middleChar = "-"
        val keywords = listOf(NilNode.nilLiteral, BooleanNode.trueLiteral, BooleanNode.falseLiteral)

        // METHODS ------------------------------------------------------------

        /**
         * Parses an identifier.
         */
        fun parse(parser: LexemParser): IdentifierNode? {
            parser.fromBuffer<IdentifierNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = IdentifierNode(parser)

            result.quotedIdentifier = QuotedIdentifierNode.parse(parser)
            if (result.quotedIdentifier != null) {
                return parser.finalizeNode(result, initCursor)
            }

            result.simpleIdentifiers.add(parseSimpleIdentifier(parser) ?: return null)

            while (true) {
                val preDashTokenCursor = parser.reader.saveCursor()

                if (!parser.readText(middleChar)) {
                    break
                }

                val simpleIdentifier = parseSimpleIdentifier(parser)
                if (simpleIdentifier == null) {
                    preDashTokenCursor.restore()
                    break
                } else {
                    result.simpleIdentifiers.add(simpleIdentifier)
                }
            }

            if (result.simpleIdentifiers.isEmpty()) {
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Checks whether an identifier is present at the reader's current position.
         */
        fun checkIdentifier(parser: LexemParser) = parser.checkAnyChar(headChars) != null

        /**
         * Parses the specified keyword.
         */
        fun parseKeyword(parser: LexemParser, keyword: String): Boolean {
            val initCursor = parser.reader.saveCursor()
            val identifier =
                    parser.fromBuffer<IdentifierNode>(parser.reader.currentPosition(), nodeType) ?: parse(parser)
                    ?: return false

            if (identifier.simpleIdentifiers.size > 1 || keyword != identifier.simpleIdentifiers.first()) {
                initCursor.restore()
                return false
            }

            return true
        }

        /**
         * Parses any keyword.
         */
        fun parseAnyKeyword(parser: LexemParser): String? {
            val initCursor = parser.reader.saveCursor()
            val identifier =
                    parser.fromBuffer<IdentifierNode>(parser.reader.currentPosition(), nodeType) ?: parse(parser)
                    ?: return null

            if (identifier.isQuotedIdentifier || identifier.simpleIdentifiers.size > 1) {
                initCursor.restore()
                return null
            }

            for (keyword in keywords) {
                if (keyword == identifier.simpleIdentifiers.first()) {
                    return keyword
                }
            }

            initCursor.restore()
            return null
        }

        /**
         * Parses a simple identifier. i.e. without dash tokens '-'.
         */
        private fun parseSimpleIdentifier(parser: LexemParser): String? {
            val result = StringBuilder()
            result.append(parser.readAnyChar(headChars) ?: return null)

            while (true) {
                result.append(parser.readAnyChar(headChars) ?: parser.readAnyChar(endChars) ?: return result.toString())
            }
        }
    }
}