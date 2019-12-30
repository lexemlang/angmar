package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * A setter for an element.
 */
internal interface LexemSetter : LexemPrimitive {
    /**
     * Gets a direct reference to the value of the setter.
     */
    fun getSetterPrimitive(memory: LexemMemory): LexemPrimitive

    /**
     * Sets a value in the setter.
     */
    fun setSetterValue(memory: LexemMemory, value: LexemMemoryValue)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun dereference(memory: LexemMemory, toWrite: Boolean) =
            getSetterPrimitive(memory).dereference(memory, toWrite = true)

    override fun getHashCode() = throw AngmarUnreachableException()
}
