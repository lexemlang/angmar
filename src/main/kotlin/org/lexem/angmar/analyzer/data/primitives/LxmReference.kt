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
    inline fun <reified T : LexemMemoryValue> dereferenceAs(memory: IMemory, toWrite: Boolean) =
            dereference(memory, toWrite) as? T

    /**
     * Gets the cell which this [LxmReference] points to.
     */
    fun getCell(memory: IMemory, toWrite: Boolean) = memory.getCell(this, toWrite)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun dereference(memory: IMemory, toWrite: Boolean) = memory.get(this, toWrite)

    override fun increaseReferences(memory: IMemory) {
        memory.getCell(this, toWrite = true).increaseReferences()
    }

    override fun decreaseReferences(memory: IMemory) {
        memory.getCell(this, toWrite = true).decreaseReferences()
    }

    override fun spatialGarbageCollect(memory: IMemory, gcFifo: GarbageCollectorFifo) {
        gcFifo.push(position)
    }

    override fun getHashCode() = position.hashCode()

    override fun toString() = "Ref($position)"

    // STATIC -----------------------------------------------------------------

    companion object {
        val StdLibContext = LxmReference(0)
        val HiddenContext = LxmReference(1)
    }
}
