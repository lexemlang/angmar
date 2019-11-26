package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*

/**
 * The Lexem value of a position of the text.
 */
internal class LxmReader(val primitive: IReader) : LexemPrimitive {

    // METHODS ----------------------------------------------------------------

    /**
     * Restores the reader into the analyzer.
     */
    fun restore(analyzer: LexemAnalyzer) {
        analyzer.text = primitive
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun toString() = "[Reader](primitive: $primitive)"
}
