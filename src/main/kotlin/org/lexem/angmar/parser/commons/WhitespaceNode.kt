package org.lexem.angmar.parser.commons

import org.lexem.angmar.*


/**
 * Parser for whitespaces.
 */
object WhitespaceNode {
    val endOfLineChars = setOf('\u000A', '\u000D', '\u0085', '\u2028', '\u2029')
    val whitespaceChars =
            setOf('\u0000', '\u0009', '\u000B', '\u000C', '\u0020', '\u00A0', '\u1680', '\u2000', '\u2001', '\u2002',
                    '\u2003', '\u2004', '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200A', '\u202F', '\u205F',
                    '\u3000', '\uFFFF')
    const val windowsEndOfLine = "\u000D\u000A"

    // METHODS ------------------------------------------------------------

    /**
     * Parses whitespace characters, including comments.
     */
    fun parse(parser: LexemParser): Boolean {
        val initPosition = parser.reader.currentPosition()

        while (CommentSingleLineNode.parse(parser) || CommentMultilineNode.parse(parser) || parseSimpleWhitespaces(
                        parser)) {
        }

        return parser.reader.currentPosition() > initPosition
    }

    /**
     * Parses only whitespaces.
     */
    fun parseSimpleWhitespaces(parser: LexemParser): Boolean {
        val initPosition = parser.reader.currentPosition()

        while (true) {
            parser.readAnyChar(whitespaceChars) ?: parser.readAnyChar(endOfLineChars) ?: break
        }

        return parser.reader.currentPosition() > initPosition
    }

    /**
     * Checks whether there is an end-of-line character in the current position.
     */
    fun readLineBreak(parser: LexemParser): String? {
        if (parser.reader.isEnd()) {
            return ""
        }

        if (parser.readText(windowsEndOfLine)) {
            return windowsEndOfLine
        }

        return parser.readAnyChar(endOfLineChars)?.toString()
    }
}
