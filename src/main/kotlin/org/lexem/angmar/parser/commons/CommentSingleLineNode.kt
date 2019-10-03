package org.lexem.angmar.parser.commons

import org.lexem.angmar.*

/**
 * Parser for single-line comments.
 */
internal object CommentSingleLineNode {
    const val SingleLineStartToken = "--"

    // METHODS ------------------------------------------------------------

    /**
     * Parses a single-line comment.
     */
    fun parse(parser: LexemParser): Boolean {
        if (!parser.readText(SingleLineStartToken)) {
            return false
        }

        while (WhitespaceNode.readLineBreak(parser) == null) {
            parser.reader.advance()
        }

        return true
    }
}
