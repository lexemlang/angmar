package org.lexem.angmar.parser

import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.io.readers.*

/**
 * Generic object for parser nodes.
 */
internal abstract class ParserNode(val parser: LexemParser, var parent: ParserNode?) : JsonSerializable {
    var from: ITextReaderCursor = ITextReaderCursor.Empty
    var to: ITextReaderCursor = ITextReaderCursor.Empty

    val content by lazy {
        parser.reader.substring(from, to)
    }

    /**
     * Compiles the code returning a new [CompiledNode].
     */
    abstract fun compile(parent: CompiledNode, parentSignal: Int): CompiledNode

    // STATIC -----------------------------------------------------------------

    companion object {
        object EmptyParserNode : ParserNode(LexemParser(IOStringReader.from("")), null) {
            override fun compile(parent: CompiledNode, parentSignal: Int) = throw AngmarUnreachableException()
            override fun toTree() = throw AngmarUnreachableException()
        }
    }
}
