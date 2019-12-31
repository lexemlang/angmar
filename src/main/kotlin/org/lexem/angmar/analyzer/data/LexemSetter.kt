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
    fun getSetterPrimitive(bigNode: BigNode): LexemPrimitive

    /**
     * Gets a direct reference to the value of the setter.
     */
    fun getSetterPrimitive(memory: LexemMemory) = getSetterPrimitive(memory.lastNode)

    /**
     * Sets a value in the setter.
     */
    fun setSetterValue(bigNode: BigNode, value: LexemMemoryValue)

    /**
     * Sets a value in the setter.
     */
    fun setSetterValue(memory: LexemMemory, value: LexemMemoryValue) = setSetterValue(memory.lastNode, value)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun dereference(bigNode: BigNode, toWrite: Boolean) =
            getSetterPrimitive(bigNode).dereference(bigNode, toWrite)

    override fun getHashCode() = throw AngmarUnreachableException()
}
