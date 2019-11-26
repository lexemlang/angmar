package org.lexem.angmar.parser.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*


/**
 * Generic commons for expressions.
 */
internal object LexemeCommons {
    /**
     * Parses both [RelativeAnchorLexemeNode] and [AbsoluteAnchorLexemeNode].
     */
    fun parseAnyAnchorLexeme(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            RelativeAnchorLexemeNode.parse(parser, parent, parentSignal) ?: AbsoluteAnchorLexemeNode.parse(parser,
                    parent, parentSignal)
}
