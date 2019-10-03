package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * An iterator for an element.
 */
internal interface LexemIterator : LexemPrimitive {
    /**
     * The current position of the iterator.
     */
    var index: Int

    /**
     * The number of iteratiorns.
     */
    val size: Long

    /**
     * Whether the iterator is ended or not.
     */
    val isEnded get() = index >= size

    /**
     * Returns the current element in a index-value pair.
     */
    fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>?

    /**
     * Advance one iteration.
     */
    fun advance(memory: LexemMemory) {
        index += 1
    }

    /**
     * Restarts the iterator.
     */
    fun restart(memory: LexemMemory) {
        index = 0
    }

    /**
     * Finalizes the iterator.
     */
    fun finalize(memory: LexemMemory) = Unit

    // OVERRIDE METHODS -------------------------------------------------------

    override fun dereference(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()
}
