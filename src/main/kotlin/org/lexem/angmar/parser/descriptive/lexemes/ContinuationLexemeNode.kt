package org.lexem.angmar.parser.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for continuation lexemes.
 */
internal object ContinuationLexemeNode {
    const val token = "\\"

    // METHODS ------------------------------------------------------------

    /**
     * Parses a continuation lexeme.
     */
    fun parse(parser: LexemParser): Boolean {
        val initCursor = parser.reader.saveCursor()

        if (!parser.readText(token) || !WhitespaceEOLNode.parse(parser)) {
            initCursor.restore()
            return false
        }

        return true
    }

}
