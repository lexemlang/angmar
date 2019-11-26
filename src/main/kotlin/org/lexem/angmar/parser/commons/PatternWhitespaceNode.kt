package org.lexem.angmar.parser.commons

import org.lexem.angmar.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Parser for whitespaces in patterns.
 */
internal object PatternWhitespaceNode {
    /**
     * Parses whitespace characters, multiline comments and continuation lexemes.
     */
    fun parse(parser: LexemParser): Boolean {
        val initPosition = parser.reader.currentPosition()

        while (WhitespaceNoEOLNode.parse(parser) || ContinuationLexemeNode.parse(parser)) {
        }

        return parser.reader.currentPosition() > initPosition
    }
}
