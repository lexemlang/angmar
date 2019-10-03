package org.lexem.angmar.parser.commons

import org.lexem.angmar.*
import org.lexem.angmar.parser.*


/**
 * Generic commons for literals.
 */
internal object Commons {
    /**
     * Parses any escape.
     */
    fun parseAnyEscape(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            EscapedExpressionNode.parse(parser, parent, parentSignal) ?: parseSimpleEscape(parser, parent, parentSignal)

    /**
     * Parses any simple escape.
     */
    fun parseSimpleEscape(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            UnicodeEscapeNode.parse(parser, parent, parentSignal) ?: EscapeNode.parse(parser, parent, parentSignal)

    /**
     * Parses a dynamic identifier.
     */
    fun parseDynamicIdentifier(parser: LexemParser, parent: ParserNode, parentSignal: Int): ParserNode? =
            EscapedExpressionNode.parse(parser, parent, parentSignal) ?: IdentifierNode.parse(parser, parent,
                    parentSignal)


    /**
     * Checks whether an identifier is present at the reader's current position.
     */
    fun checkIdentifier(parser: LexemParser) = parser.checkAnyChar(IdentifierNode.headChars) != null

    /**
     * Parses the specified keyword.
     */
    fun parseKeyword(parser: LexemParser, keyword: String): Boolean {
        val initCursor = parser.reader.saveCursor()
        val identifier = IdentifierNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0) ?: return false

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
                parser.fromBuffer(parser.reader.currentPosition(), IdentifierNode::class.java) ?: IdentifierNode.parse(
                        parser, ParserNode.Companion.EmptyParserNode, 0) ?: return null

        if (identifier.isQuotedIdentifier || identifier.simpleIdentifiers.size > 1) {
            initCursor.restore()
            return null
        }

        for (keyword in GlobalCommons.keywords) {
            if (keyword == identifier.simpleIdentifiers.first()) {
                return keyword
            }
        }

        initCursor.restore()
        return null
    }
}
