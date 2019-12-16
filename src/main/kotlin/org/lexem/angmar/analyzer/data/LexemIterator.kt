package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * An iterator for an element.
 */
internal abstract class LexemIterator : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory) {
        restart(memory)
    }

    constructor(memory: LexemMemory, oldIterator: LexemIterator) : super(memory, oldIterator)

    // METHODS ----------------------------------------------------------------

    /**
     * The current position of the iterator.
     */
    fun getIndex(memory: LexemMemory) = getPropertyValue(memory, AnalyzerCommons.Identifiers.Index) as LxmInteger

    /**
     * The number of iterations.
     */
    abstract val size: Long

    /**
     * Whether the iterator is ended or not.
     */
    fun isEnded(memory: LexemMemory) = getIndex(memory).primitive >= size

    /**
     * Returns the current element in a index-value pair.
     */
    abstract fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>?

    /**
     * Advance one iteration.
     */
    fun advance(memory: LexemMemory) {
        val prev = getPropertyValue(memory, AnalyzerCommons.Identifiers.Index) as LxmInteger
        val new = LxmInteger.from(prev.primitive + 1)
        setProperty(memory, AnalyzerCommons.Identifiers.Index, new)
    }

    /**
     * Restarts the iterator.
     */
    fun restart(memory: LexemMemory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Index, LxmInteger.Num0)
    }
}
