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
    fun parseAnyEscape(parser: LexemParser, parent: ParserNode) =
            EscapedExpressionNode.parse(parser, parent) ?: parseSimpleEscape(parser, parent)

    /**
     * Parses any simple escape.
     */
    fun parseSimpleEscape(parser: LexemParser, parent: ParserNode) =
            UnicodeEscapeNode.parse(parser, parent) ?: EscapeNode.parse(parser, parent)

    /**
     * Parses a dynamic identifier.
     */
    fun parseDynamicIdentifier(parser: LexemParser, parent: ParserNode): ParserNode? =
            EscapedExpressionNode.parse(parser, parent) ?: IdentifierNode.parse(parser, parent)


    /**
     * Checks whether an identifier is present at the reader's current position.
     */
    fun checkIdentifier(parser: LexemParser) = parser.checkAnyChar(IdentifierNode.headChars) != null

    /**
     * Parses the specified keyword.
     */
    fun parseKeyword(parser: LexemParser, keyword: String): Boolean {
        val initCursor = parser.reader.saveCursor()
        val identifier = IdentifierNode.parse(parser, ParserNode.Companion.EmptyParserNode) ?: return false

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
        val identifier = IdentifierNode.parse(parser, ParserNode.Companion.EmptyParserNode) ?: return null

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
