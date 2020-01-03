package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import java.util.concurrent.atomic.*

/**
 * The representation of a memory heap cell.
 */
internal class BigNodeHeapCell(val bigNode: BigNode, val position: Int, private var value: LexemReferenced?) {
    var isValueCloned = true
    var referenceCount = AtomicInteger(0)
        private set

    /**
     * Whether the cell is freed or not.
     */
    val isFreed get() = value == null

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of the cell.
     */
    fun getValue(toWrite: Boolean): LexemReferenced? {
        if (toWrite) {
            cloneValue(bigNode)
        }

        return value
    }

    /**
     * Increases the reference count.
     */
    fun increaseReferences(count: Int = 1) {
        if (isFreed) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "No modification operation can be performed in a freed cell") {}
        }

        referenceCount.addAndGet(count)
    }

    /**
     * Decreases the reference count freeing the cell if it reaches 0.
     */
    fun decreaseReferences(count: Int = 1) {
        val inGarbageCollectionMode = bigNode.inGarbageCollectionMode.get()
        if (isFreed) {
            if (inGarbageCollectionMode) {
                return
            }

            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "No modification operation can be performed in a freed cell") {}
        }

        val newReferenceCount = referenceCount.addAndGet(-count)

        if (newReferenceCount < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.ReferenceCountUnderflow,
                    "A cell in the memory has an underflow reference count") {}
        }

        // Free in-chain only if not in garbage collection mode.
        if (newReferenceCount == 0 && !inGarbageCollectionMode) {
            // TODO free async
            bigNode.freeHeapCell(position)
        }
    }

    /**
     * Frees a cell.
     */
    fun freeCell() {
        if (isFreed) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "Cannot free an already freed cell") {}
        }

        if (!bigNode.inGarbageCollectionMode.get() && referenceCount.get() > 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotFreeAReferencedHeapCell,
                    "Cannot free a yet referenced cell") {}
        }

        val oldValue = value ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                "Not freed cell without a value") {}
        value = null
        referenceCount.set(bigNode.lastFreePosition.get())
        bigNode.lastFreePosition.set(position)

        oldValue.memoryDealloc(bigNode)
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
        referenceCount.set(0)
    }

    /**
     * Clones this [BigNodeHeapCell].
     */
    fun clone(newBigNode: BigNode): BigNodeHeapCell {
        val res = BigNodeHeapCell(newBigNode, position, value)
        res.referenceCount = AtomicInteger(referenceCount.get())
        res.isValueCloned = false

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
        "[$position] Freed -> $referenceCount"
    } else {
        "[$position] Refs<$referenceCount> = $value"
    }
}

