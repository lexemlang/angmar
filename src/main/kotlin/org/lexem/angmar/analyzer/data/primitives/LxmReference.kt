package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The lexem reference value, i.e. a pointer.
 */
internal class LxmReference(val position: Int) : LexemPrimitive {
    /**
     * Dereferences all indirect references until get the value.
     */
    fun dereferenceOnce(memory: LexemMemory) = memory.get(this)

    /**
     * Dereferences the value to the specified type.
     */
    inline fun <reified T : LexemMemoryValue> dereferenceAs(memory: LexemMemory) = dereference(memory) as? T

    /**
     * Increases the reference count.
     */
    fun increaseReferenceCount(memory: LexemMemory) {
        memory.replacePrimitives(LxmNil, this)
    }

    /**
     * Decreases the reference count freeing the cell if it reaches 0.
     */
    fun decreaseReferenceCount(memory: LexemMemory) {
        memory.replacePrimitives(this, LxmNil)
    }

    /**
     * Gets the memory cell that this reference points to.
     */
    fun getCell(memory: LexemMemory) = memory.lastNode.getCell(position)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun dereference(memory: LexemMemory) = memory.get(this)

    override fun getHashCode(memory: LexemMemory) = position.hashCode()

    override fun toString() = "Ref($position)"

    // STATIC -----------------------------------------------------------------

    companion object {
        val StdLibContext = LxmReference(0)
    }
}
