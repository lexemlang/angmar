package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.memory.*

/**
 * The common part of every referenced value in lexem.
 */
internal interface LexemReferenced : LexemMemoryValue {
    /**
     * Indicates whether the value is immutable or not.
     */
    val isImmutable: Boolean

    /**
     * Clones the current value.
     */
    fun clone(): LexemReferenced

    /**
     * Clears the memory value.
     */
    fun memoryDealloc(memory: LexemMemory)

    /**
     * Collects all the garbage of the current big node.
     */
    fun spatialGarbageCollect(memory: LexemMemory)
}
