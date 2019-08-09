package org.lexem.angmar.parser.commons

import org.lexem.angmar.*


/**
 * Parser for eol whitespaces optionally preceded with a no-eol whitespace.
 */
object WhitespaceEOLNode {
    /**
     * Parses an eol whitespace optionally preceded with a no-eol whitespace.
     */
    fun parse(parser: LexemParser): Boolean {
        val initCursor = parser.reader.saveCursor()

        WhitespaceNoEOLNode.parse(parser)

        if (CommentSingleLineNode.parse(parser)) {
            return true
        }

        if (WhitespaceNode.readLineBreak(parser) != null) {
            return true
        }

        initCursor.restore()
        return false
    }
}
