package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * The Lexem reference value, i.e. a pointer.
 */
internal class LxmReference constructor(val position: Int) : LexemPrimitive {
    init {
        if (position < 0) {
            throw AngmarException("The position of a reference cannot be negative")
        }
    }

    /**
     * Dereferences the value to the specified type.
     */
    inline fun <reified T : LexemMemoryValue> dereferenceAs(memory: LexemMemory, toWrite: Boolean) =
            dereference(memory, toWrite) as? T

    /**
     * Gets the memory cell that this reference points to.
     */
    fun getCell(memory: LexemMemory) = memory.lastNode.getCell(memory, position)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun dereference(memory: LexemMemory, toWrite: Boolean) = memory.get(this, toWrite)!!

    override fun increaseReferences(memory: LexemMemory) {
        memory.replacePrimitives(LxmNil, this)
    }

    override fun decreaseReferences(memory: LexemMemory) {
        memory.replacePrimitives(this, LxmNil)
    }

    override fun spatialGarbageCollect(memory: LexemMemory, gcFifo: GarbageCollectorFifo) {
        gcFifo.push(position)
    }

    override fun getType(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun getPrototype(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun getHashCode(memory: LexemMemory) = position.hashCode()

    override fun toString() = "Ref($position)"

    // STATIC -----------------------------------------------------------------

    companion object {
        val StdLibContext = LxmReference(0)
    }
}
