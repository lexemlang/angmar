package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import java.util.concurrent.atomic.*

/**
 * The representation of a memory heap cell.
 */
internal class BigNodeHeapCell {
    val bigNodeIndex: Int
    private var value: LexemReferenced?
    val oldCell: AtomicReference<BigNodeHeapCell?>
    var isValueCloned = true
    var nextRemovedCell = -1
        private set

    /**
     * Whether the cell is freed or not.
     */
    val isFreed get() = value == null

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * Builds a new filled cell.
     */
    constructor(bigNodeIndex: Int, value: LexemReferenced) {
        this.bigNodeIndex = bigNodeIndex
        this.value = value
        this.oldCell = AtomicReference(null)
    }

    /**
     * Builds a new empty cell.
     */
    constructor(bigNodeIndex: Int, nextRemovedCell: Int = -1) {
        this.bigNodeIndex = bigNodeIndex
        this.value = null
        this.oldCell = AtomicReference(null)
        this.nextRemovedCell = nextRemovedCell
    }

    /**
     * FOR CLONE.
     */
    private constructor(bigNodeIndex: Int, value: LexemReferenced?, oldCell: BigNodeHeapCell) {
        this.bigNodeIndex = bigNodeIndex
        this.value = value
        this.oldCell = AtomicReference(oldCell)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of the cell.
     */
    fun getValue(bigNode: BigNode, toWrite: Boolean): LexemReferenced? {
        if (toWrite) {
            cloneValue(bigNode)
        }

        return value
    }

    /**
     * Frees a cell.
     */
    fun freeCell(position: Int, bigNode: BigNode) {
        if (isFreed) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "Cannot free an already freed cell") {}
        }

        if (value == null) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "Cannot free a cell without a value") {}
        }

        value = null
        nextRemovedCell = bigNode.lastFreePosition.getAndSet(position)
    }

    /**
     * Re-allocs a cell.
     */
    fun reallocCell(value: LexemReferenced) {
        if (!isFreed) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "Cannot realloc a used cell") {}
        }

        this.value = value
        isValueCloned = true
        nextRemovedCell = -1
    }

    /**
     * Clones this [BigNodeHeapCell].
     */
    fun clone(newIndex: Int): BigNodeHeapCell {
        val res = BigNodeHeapCell(newIndex, value, oldCell = this)
        res.isValueCloned = false
        res.nextRemovedCell = nextRemovedCell

        return res
    }

    /**
     * Clones the value.
     */
    private fun cloneValue(bigNode: BigNode) {
        if (isFreed) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "No modification operation can be performed in a freed cell") {}
        }

        if (!isValueCloned) {
            value = value!!.memoryClone(bigNode)
            isValueCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = if (isFreed) {
        "[$bigNodeIndex] Freed -> $nextRemovedCell"
    } else {
        "[$bigNodeIndex] $value"
    }
}

