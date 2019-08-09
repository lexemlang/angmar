package org.lexem.angmar.parser

import org.lexem.angmar.*
import org.lexem.angmar.io.*
import org.lexem.angmar.io.printer.*

/**
 * Generic object for parser nodes.
 */
abstract class ParserNode(val parser: LexemParser) : ITreeLikePrintable {
    var from: ITextReaderCursor = ITextReaderCursor.Empty
    var to: ITextReaderCursor = ITextReaderCursor.Empty

    val content by lazy {
        parser.substring(from, to)
    }
}
