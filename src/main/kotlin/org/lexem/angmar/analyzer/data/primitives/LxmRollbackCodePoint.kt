package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.parser.*

/**
 * The lexem value of a position in code to rollback.
 */
internal class LxmRollbackCodePoint(node: ParserNode, signal: Int, val readerCursor: ITextReaderCursor) :
        LxmCodePoint(node, signal) {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun toString() = "[Rollback Code Point](cursor: $readerCursor, signal: $signal, node: $node)"
}
