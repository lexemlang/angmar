package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * A setter for an element.
 */
internal interface LexemSetter : LexemPrimitive {
    /**
     * Gets a direct reference to the element.
     */
    fun getPrimitive(memory: LexemMemory): LexemPrimitive

    /**
     * Sets a value in the setter.
     */
    fun setPrimitive(memory: LexemMemory, value: LexemPrimitive)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun dereference(memory: LexemMemory, toWrite: Boolean) =
            getPrimitive(memory).dereference(memory, toWrite = true)

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()
}
