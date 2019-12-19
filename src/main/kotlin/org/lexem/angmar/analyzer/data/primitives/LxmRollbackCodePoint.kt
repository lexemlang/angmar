package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.parser.*

/**
 * The Lexem value of a position in code to rollback.
 */
internal class LxmRollbackCodePoint(node: ParserNode, signal: Int, val readerCursor: IReaderCursor) :
        LxmCodePoint(node, signal, node, "") {

    // METHODS ----------------------------------------------------------------

    /**
     * Restores the code point specified by this point.
     */
    fun restore(analyzer: LexemAnalyzer) {
        analyzer.nextNode = node
        analyzer.signal = signal
        analyzer.text = readerCursor.getReader()
        readerCursor.restore()
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun toString() = "[Rollback Code Point] (cursor: $readerCursor, signal: $signal, node: $node)"
}
