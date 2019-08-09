package org.lexem.angmar.parser.literals

import org.lexem.angmar.*


/**
 * Generic commons for literals.
 */
object LiteralCommons {
    /**
     * Parses any string literal.
     */
    fun parseAnyString(parser: LexemParser) = UnescapedStringNode.parse(parser) ?: StringNode.parse(parser)

    /**
     * Parses any object literal.
     */
    fun parseAnyObject(parser: LexemParser) = ObjectNode.parse(parser) ?: PropertyStyleObjectNode.parse(parser)

    /**
     * Parses any interval literal.
     */
    fun parseAnyInterval(parser: LexemParser) = UnicodeIntervalNode.parse(parser) ?: IntervalNode.parse(parser)
}
