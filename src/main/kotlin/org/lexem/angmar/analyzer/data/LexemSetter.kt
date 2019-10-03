package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * A setter for an element.
 */
internal interface LexemSetter : LexemPrimitive {
    /**
     * Resolves the setter to get a direct reference to the element.
     */
    fun resolve(memory: LexemMemory): LexemPrimitive

    /**
     * Sets a value in the setter.
     */
    fun set(memory: LexemMemory, value: LexemPrimitive)

    /**
     * Increases the reference count.
     */
    fun increaseReferenceCount(memory: LexemMemory)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun dereference(memory: LexemMemory) = resolve(memory).dereference(memory)

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()
}
