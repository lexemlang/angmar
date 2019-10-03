package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*

/**
 * The representation of the memory of the analyzer. Initiates with the standard library loaded.
 */
internal class LexemMemory {
    var lastNode = BigNode(null)
        private set

    // METHODS ----------------------------------------------------------------

    /**
     * Pushes a new primitive into the stack.
     */
    fun pushStack(primitive: LexemPrimitive) = lastNode.pushStack(primitive)

    /**
     * Pushes a new primitive into the stack ignoring the reference count.
     */
    fun pushStackIgnoringReferenceCount(primitive: LexemPrimitive) = lastNode.pushStackIgnoringReferenceCount(primitive)

    /**
     * Pops the last value of the stack.
     */
    fun popStack() = lastNode.popStack()

    /**
     * Gets a value from the memory.
     */
    fun get(reference: LxmReference) = lastNode.getCell(reference.position).value

    /**
     * Sets a value in the memory.
     */
    fun set(reference: LxmReference, value: LexemReferenced) = lastNode.setCell(reference.position, value)

    /**
     * Adds a value in the memory returning the position in which it has been added.
     */
    fun add(value: LexemReferenced) = LxmReference(lastNode.alloc(this, value).position)

    /**
     * Removes a value in the memory.
     */
    fun remove(reference: LxmReference) = lastNode.free(this, reference.position)

    /**
     * Converts a [LexemMemoryValue] to a [LexemPrimitive].
     */
    fun valueToPrimitive(value: LexemMemoryValue) = when (value) {
        is LexemReferenced -> add(value)
        is LexemPrimitive -> value
        else -> throw AngmarUnreachableException()
    }

    /**
     * Replaces a primitive with a new one handling the pointer changes.
     */
    fun replacePrimitives(oldValue: LexemPrimitive, newValue: LexemPrimitive) {
        // Increases the new reference.
        if (newValue is LxmReference) {
            lastNode.getCell(newValue.position).increaseReferenceCount()
        }

        // Removes the previous reference.
        if (oldValue is LxmReference) {
            lastNode.getCell(oldValue.position).decreaseReferenceCount(this)
        }
    }

    /**
     * Clears the memory.
     */
    fun clear() {
        var node = lastNode
        while (node.previousNode != null) {
            node.destroy()
            node = node.previousNode!!
        }

        lastNode = node
        freezeCopy()
    }

    /**
     * Freezes the memory creating a differential copy of the memory.
     */
    fun freezeCopy(): BigNode {
        val res = lastNode
        lastNode = BigNode(lastNode)
        return res
    }

    /**
     * Rollback the last copy, removing all changes since then.
     */
    fun rollbackCopy() {
        // Prevents the deletion if it is the root node.
        if (lastNode.previousNode == null) {
            throw  AngmarAnalyzerException(AngmarAnalyzerExceptionType.FirstBigNodeRollback,
                    "The memory cannot rollback the first BigNode") {}
        }

        val nodeToRemove = lastNode
        lastNode = lastNode.previousNode!!

        nodeToRemove.destroy()
    }

    /**
     * Restores the specified copy, removing all changes since then.
     */
    fun restoreCopy(bigNode: BigNode) {
        while (lastNode.previousNode != null && lastNode != bigNode) {
            val nodeToRemove = lastNode
            lastNode = lastNode.previousNode!!
            nodeToRemove.destroy()
        }

        if (lastNode.previousNode == null) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BigNodeDoesNotBelongToMemoryChain,
                    "The specified bigNode does not belong to this memory chain") {}
        }
    }

    /**
     * Collects all the garbage of the current big node.
     */
    fun spatialGarbageCollect() {
        val stdLibCell = LxmReference.StdLibContext.getCell(this)
        stdLibCell.spatialGarbageCollect(this)

        for (i in 0 until lastNode.actualHeapSize) {
            val cell = lastNode.getCell(i)

            if (!cell.isNotGarbage && !cell.isFreed) {
                lastNode.freeAsGarbage(this, cell.position)
            } else {
                cell.clearGarbageFlag(this)
            }
        }
    }
}
