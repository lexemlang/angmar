package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.parser.*


/**
 * Generic commons for literals.
 */
internal object LiteralCommons {
    /**
     * Parses any string literal.
     */
    fun parseAnyString(parser: LexemParser, parent: ParserNode) =
            UnescapedStringNode.parse(parser, parent) ?: StringNode.parse(parser, parent)

    /**
     * Parses any object literal.
     */
    fun parseAnyObject(parser: LexemParser, parent: ParserNode) =
            ObjectNode.parse(parser, parent) ?: PropertyStyleObjectNode.parse(parser, parent)

    /**
     * Parses any interval literal.
     */
    fun parseAnyInterval(parser: LexemParser, parent: ParserNode) =
            UnicodeIntervalNode.parse(parser, parent) ?: IntervalNode.parse(parser, parent)

    /**
     * Parses any interval literal and the unicode interval without prefix.
     */
    fun parseAnyIntervalForLexem(parser: LexemParser, parent: ParserNode) =
            UnicodeIntervalAbbrNode.parse(parser, parent) ?: UnicodeIntervalNode.parse(parser, parent)
            ?: IntervalNode.parse(parser, parent)
}
