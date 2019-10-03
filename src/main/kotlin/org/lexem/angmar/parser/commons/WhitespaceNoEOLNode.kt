package org.lexem.angmar.parser.commons

import org.lexem.angmar.*

/**
 * Parser for whitespaces and multiline comments without including end-of-line characters.
 */
internal object WhitespaceNoEOLNode {
    /**
     * Parses whitespace characters and multiline comments.
     */
    fun parse(parser: LexemParser): Boolean {
        val initPosition = parser.reader.currentPosition()

        while (readInlineWhitespace(parser) || CommentMultilineNode.parse(parser)) {
        }

        return parser.reader.currentPosition() > initPosition
    }

    /**
     * Parses only inline whitespaces.
     */
    private fun readInlineWhitespace(parser: LexemParser): Boolean {
        val initPosition = parser.reader.currentPosition()

        while (true) {
            parser.readAnyChar(WhitespaceNode.whitespaceChars) ?: break
        }

        return parser.reader.currentPosition() > initPosition
    }
}
