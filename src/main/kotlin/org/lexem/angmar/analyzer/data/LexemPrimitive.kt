package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.memory.*

/**
 * The common part of every primitive in lexem.
 */
internal interface LexemPrimitive : LexemMemoryValue {
    /**
     * Gets the hash of the current value.
     */
    fun getHashCode(memory: LexemMemory): Int

    /**
     * Indicates whether the primitive contains references to other elements.
     */
    fun containsReferences(): Boolean = false

    /**
     * Gets the reference of this value.
     */
    override fun getPrimitive() = this
}
