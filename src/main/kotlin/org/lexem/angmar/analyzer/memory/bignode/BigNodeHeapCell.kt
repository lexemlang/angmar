package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import java.util.concurrent.atomic.*

/**
 * The representation of a memory heap cell.
 */
internal class BigNodeHeapCell(val bigNode: BigNode, val position: Int, private var value: LexemReferenced) {
    var isValueCloned = true
    var referenceCount = AtomicInteger(0)
        private set

    // METHODS ----------------------------------------------------------------

    /**
     * Increases the reference count.
     */
    fun increaseReferences(memory: LexemMemory, count: Int = 1) {
        if (memory.lastNode != bigNode) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.ForbiddenMemoryAccess,
                    "No modification operation can be performed in a past BigNode") {}
        }

        referenceCount.addAndGet(count)
    }

    /**
     * Decreases the reference count freeing the cell if it reaches 0.
     */
    fun decreaseReferences(memory: LexemMemory, count: Int = 1) {
        if (memory.lastNode != bigNode) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.ForbiddenMemoryAccess,
                    "No modification operation can be performed in a past BigNode") {}
        }

        val newReferenceCount = referenceCount.addAndGet(-count)

        if (newReferenceCount < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.ReferenceCountUnderflow,
                    "A cell in the memory has an underflow reference count") {}
        }

        if (newReferenceCount == 0) {
            // TODO free async
            memory.lastNode.free(memory, position)
        }
    }

    /**
     * Clones this [BigNodeHeapCell].
     */
    fun clone(newBigNode: BigNode): BigNodeHeapCell {
        val res = BigNodeHeapCell(newBigNode, position, value)
        res.referenceCount = referenceCount
        res.isValueCloned = false

        return res
    }

    /**
     * Clones the value.
     */
    private fun clonePages(bigNode: BigNode) {
        if (!isValueCloned) {
            value = value.clone(bigNode)
            isValueCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "[$position] Refs<$referenceCount> = $value"
}

