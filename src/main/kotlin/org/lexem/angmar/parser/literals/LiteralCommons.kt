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
    fun parseAnyString(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            UnescapedStringNode.parse(parser, parent, parentSignal) ?: StringNode.parse(parser, parent, parentSignal)

    /**
     * Parses any object literal.
     */
    fun parseAnyObject(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            ObjectNode.parse(parser, parent, parentSignal) ?: PropertyStyleObjectNode.parse(parser, parent,
                    parentSignal)

    /**
     * Parses any interval literal.
     */
    fun parseAnyInterval(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            UnicodeIntervalNode.parse(parser, parent, parentSignal) ?: IntervalNode.parse(parser, parent, parentSignal)

    /**
     * Parses any interval literal and the unicode interval without prefix.
     */
    fun parseAnyIntervalForLexem(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            UnicodeIntervalAbbrNode.parse(parser, parent, parentSignal) ?: UnicodeIntervalNode.parse(parser, parent,
                    parentSignal) ?: IntervalNode.parse(parser, parent, parentSignal)
}
