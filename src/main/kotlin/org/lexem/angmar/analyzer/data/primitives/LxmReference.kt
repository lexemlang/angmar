package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem reference value, i.e. a pointer.
 */
internal class LxmReference(val position: Int) : LexemPrimitive {
    /**
     * Dereferences the value to the specified type.
     */
    inline fun <reified T : LexemMemoryValue> dereferenceAs(memory: LexemMemory) = dereference(memory) as? T

    /**
     * Gets the memory cell that this reference points to.
     */
    fun getCell(memory: LexemMemory) = memory.lastNode.getCell(position)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun dereference(memory: LexemMemory) = memory.get(this)

    override fun increaseReferences(memory: LexemMemory) {
        memory.replacePrimitives(LxmNil, this)
    }

    override fun decreaseReferences(memory: LexemMemory) {
        memory.replacePrimitives(this, LxmNil)
    }

    override fun spatialGarbageCollect(memory: LexemMemory) {
        getCell(memory).spatialGarbageCollect(memory)
    }

    override fun getHashCode(memory: LexemMemory) = position.hashCode()

    override fun toString() = "Ref($position)"

    // STATIC -----------------------------------------------------------------

    companion object {
        val StdLibContext = LxmReference(0)
    }
}
