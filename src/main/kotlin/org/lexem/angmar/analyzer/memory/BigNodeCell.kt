package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import java.util.*

/**
 * The representation of a memory cell.
 */
internal class BigNodeCell private constructor(position: Int, value: LexemReferenced) {
    var isNotGarbage = false
        private set
    var position = position
        private set
    var referenceCount = 0
        private set
    var value = value
        private set

    // PROPERTIES -------------------------------------------------------------

    val isFreed get() = value == EmptyCell

    // METHODS ----------------------------------------------------------------

    /**
     * Sets a value to the cell.
     */
    fun setValue(value: LexemReferenced) {
        this.value = value
    }

    /**
     * Destroys the cell to be reused.
     * MUST ONLY BE CALLED WHENEVER THE CELL IS REMOVED FROM MEMORY.
     */
    fun destroy() {
        position = -1
        referenceCount = 0
        value = EmptyCell

        if (instances.size < Consts.Memory.maxPoolSize) {
            instances.add(this)
        }
    }

    /**
     * Shifts the memory cell to a forward [BigNode].
     */
    fun shiftCell(): BigNodeCell {
        val newCell = new(position, value)
        newCell.referenceCount = referenceCount
        newCell.value = value.clone()

        return newCell
    }

    /**
     * Undoes the free operation over the cell setting a new value. CAN ONLY BE USED IN [BigNode].
     */
    fun reallocCell(memory: LexemMemory, value: LexemReferenced) {
        referenceCount = 0
        this.value.memoryDealloc(memory)
        this.value = value
    }

    /**
     * Frees the cell clearing all its fields but the reference which points to the next free cell.
     */
    fun freeCell(memory: LexemMemory, nextFreeCell: Int) {
        if (referenceCount > 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.ReferencedHeapCellFreed,
                    "Cannot free a referenced memory cell") {}
        }

        freeCellAsGarbage(memory, nextFreeCell)
    }

    /**
     * Frees the cell clearing all its fields but the reference which points to the next free cell.
     */
    fun freeCellAsGarbage(memory: LexemMemory, nextFreeCell: Int) {
        isNotGarbage = false
        referenceCount = nextFreeCell
        value.memoryDealloc(memory)
        setValue(EmptyCell)
    }

    /**
     * Clears the garbage flag to reset it.
     */
    fun clearGarbageFlag(memory: LexemMemory) {
        isNotGarbage = false
    }

    /**
     * Increases the reference.
     */
    fun increaseReferenceCount(count: Int = 1) {
        referenceCount += count
    }

    /**
     * Decreases the reference count freeing the cell if it reaches 0.
     */
    fun decreaseReferenceCount(memory: LexemMemory, count: Int = 1) {
        referenceCount -= count

        if (referenceCount < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.ReferenceCountUnderflow,
                    "A cell in the memory has an underflow reference count") {}
        }

        if (referenceCount == 0) {
            memory.lastNode.free(memory, position)
        }
    }

    /**
     * Collects all the garbage of the current big node.
     */
    fun spatialGarbageCollect(memory: LexemMemory) {
        // Avoid to parse the cell if it is already parsed.
        if (!this.isNotGarbage) {
            // Mark as reached.
            this.isNotGarbage = true

            // Collect.
            this.value.spatialGarbageCollect(memory)
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = when {
        referenceCount < 0 -> "<REMOVED>"
        isFreed -> "[$position] Free -> $referenceCount"
        else -> "[$position]${if (isNotGarbage) {
            "*"
        } else {
            ""
        }} Refs<$referenceCount> = $value"
    }

    // STATIC -----------------------------------------------------------------

    internal object EmptyCell : LexemReferenced {
        override val isImmutable = true
        override fun clone() = this
        override fun memoryDealloc(memory: LexemMemory) = Unit
        override fun spatialGarbageCollect(memory: LexemMemory) = throw AngmarUnreachableException()
    }

    companion object {
        private val instances = Stack<BigNodeCell>()

        /**
         * Creates a new [BigNodeCell] for a specified position with a default value value.
         */
        fun new(position: Int, value: LexemReferenced): BigNodeCell {
            val instance = if (instances.size > 0) {
                instances.pop()!!
            } else {
                BigNodeCell(position, EmptyCell)
            }

            instance.position = position
            instance.setValue(value)
            instance.referenceCount = 0

            return instance
        }
    }
}

