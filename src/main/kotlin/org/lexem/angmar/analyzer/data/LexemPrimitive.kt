package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The common part of every primitive in lexem.
 */
internal interface LexemPrimitive : LexemMemoryValue {
    /**
     * Dereferences all indirect references until get a value that is not a [LxmReference] or [LexemSetter].
     */
    fun dereference(memory: LexemMemory, toWrite: Boolean): LexemMemoryValue = this

    /**
     * Increase the internal references of the primitive.
     */
    fun increaseReferences(memory: LexemMemory) = Unit

    /**
     * Decrease the internal references of the primitive.
     */
    fun decreaseReferences(memory: LexemMemory) = Unit

    /**
     * Collects all the garbage of the current big node.
     */
    fun spatialGarbageCollect(memory: LexemMemory) = Unit

    /**
     * Gets the hash of the current value.
     */
    fun getHashCode(memory: LexemMemory): Int
}
