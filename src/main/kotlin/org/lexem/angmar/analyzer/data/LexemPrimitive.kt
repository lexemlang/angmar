package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.memory.*

/**
 * The common part of every primitive in lexem.
 */
internal interface LexemPrimitive : LexemMemoryValue {

    /**
     * Increase the internal references of the primitive.
     */
    fun increaseReferences(memory: LexemMemory) = Unit

    /**
     * Decrease the internal references of the primitive.
     */
    fun decreaseReferences(memory: LexemMemory) = Unit

    /**
     * Gets the hash of the current value.
     */
    fun getHashCode(memory: LexemMemory): Int

    /**
     * Gets the reference of this value.
     */
    override fun getPrimitive() = this
}
