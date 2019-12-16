package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.memory.*

/**
 * The common part of every referenced value in lexem.
 */
internal abstract class LexemReferenced(memory: LexemMemory) : LexemMemoryValue {
    /**
     * Holds a reference to the node that it belongs to.
     */
    var bigNode = memory.lastNode

    /**
     * Indicates whether the value is an immutable view of the memory value or can be modified.
     */
    fun isMemoryImmutable(memory: LexemMemory) = bigNode != memory.lastNode

    /**
     * Clones the current value.
     */
    abstract fun clone(memory: LexemMemory): LexemReferenced

    /**
     * Clears the memory value.
     */
    abstract fun memoryDealloc(memory: LexemMemory)

    /**
     * Collects all the garbage of the current big node.
     */
    abstract fun spatialGarbageCollect(memory: LexemMemory)
}
