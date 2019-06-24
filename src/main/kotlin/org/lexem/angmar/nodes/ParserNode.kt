package org.lexem.angmar.nodes

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.ITextReaderCursor
import org.lexem.angmar.io.printer.ITreeLikePrintable

/**
 * Generic object for parser nodes.
 */
abstract class ParserNode(val type: NodeType, val parser: LexemParser) :
    ITreeLikePrintable {
    var from: ITextReaderCursor = ITextReaderCursor.Empty
    var to: ITextReaderCursor = ITextReaderCursor.Empty

    val content by lazy {
        parser.substring(from, to)
    }
}