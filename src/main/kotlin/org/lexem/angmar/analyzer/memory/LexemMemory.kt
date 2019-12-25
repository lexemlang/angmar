package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*

/**
 * The representation of the memory of the analyzer. Initiates with the standard library loaded.
 */
internal class LexemMemory {
    var isInGarbageCollectionMode = false
        private set

    val firstNode = BigNode(previousNode = null, nextNode = null)
    var lastNode = firstNode
        private set

    // METHODS ----------------------------------------------------------------

    /**
     * Adds a new primitive into the stack.
     */
    fun addToStack(name: String, primitive: LexemMemoryValue) =
            lastNode.addToStack(name, primitive.getPrimitive(), this)

    /**
     * Adds a new primitive into the stack with '[AnalyzerCommons.Identifiers.Last]' as name.
     */
    fun addToStackAsLast(primitive: LexemMemoryValue) = addToStack(AnalyzerCommons.Identifiers.Last, primitive)

    /**
     * Gets the specified primitive from the stack.
     */
    fun getFromStack(name: String) = lastNode.getFromStack(name)

    /**
     * Gets the '[AnalyzerCommons.Identifiers.Last]' primitive from the stack.
     */
    fun getLastFromStack() = getFromStack(AnalyzerCommons.Identifiers.Last)

    /**
     * Removes the specified primitive from the stack.
     */
    fun removeFromStack(name: String) = lastNode.removeFromStack(name, this)

    /**
     * Removes the '[AnalyzerCommons.Identifiers.Last]' primitive from the stack.
     */
    fun removeLastFromStack() = removeFromStack(AnalyzerCommons.Identifiers.Last)

    /**
     * Renames the specified stack cell by another name.
     */
    fun renameStackCell(oldName: String, newName: String) {
        if (oldName == newName) {
            return
        }

        val currentCell = getFromStack(oldName)
        addToStack(newName, currentCell)
        removeFromStack(oldName)
    }

    /**
     * Renames the '[AnalyzerCommons.Identifiers.Last]' stack cell by another name.
     */
    fun renameLastStackCell(newName: String) = renameStackCell(AnalyzerCommons.Identifiers.Last, newName)

    /**
     * Renames the specified cell to '[AnalyzerCommons.Identifiers.Last]'.
     */
    fun renameStackCellToLast(oldName: String) = renameStackCell(oldName, AnalyzerCommons.Identifiers.Last)

    /**
     * Replace the specified stack cell by another primitive.
     */
    fun replaceStackCell(name: String, newValue: LexemMemoryValue) =
            lastNode.replaceStackCell(name, newValue.getPrimitive(), this)

    /**
     * Replace the '[AnalyzerCommons.Identifiers.Last]' stack cell by another primitive.
     */
    fun replaceLastStackCell(newValue: LexemMemoryValue) = replaceStackCell(AnalyzerCommons.Identifiers.Last, newValue)

    /**
     * Gets a value from the memory.
     */
    fun get(reference: LxmReference, toWrite: Boolean) =
            lastNode.getCell(this, reference.position, forceShift = toWrite).value

    /**
     * Adds a value in the memory returning the position in which it has been added.
     */
    fun add(value: LexemReferenced) = LxmReference(lastNode.alloc(this, value).position)

    /**
     * Removes a value in the memory.
     */
    fun remove(reference: LxmReference) = lastNode.free(this, reference.position)

    /**
     * Replaces a primitive with a new one handling the pointer changes.
     */
    fun replacePrimitives(oldValue: LexemPrimitive, newValue: LexemPrimitive) {
        // Increases the new reference.
        if (newValue is LxmReference) {
            lastNode.getCell(this, newValue.position, forceShift = true).increaseReferences()
        }

        // Removes the previous reference.
        if (oldValue is LxmReference) {
            lastNode.getCell(this, oldValue.position, forceShift = true).decreaseReferences(this)
        }
    }

    /**
     * Clears the memory.
     */
    fun clear() {
        firstNode.nextNode?.destroy()
        firstNode.nextNode = null
        lastNode = firstNode
    }

    /**
     * Counts the number of [BigNode]s in the memory.
     */
    fun countBigNodes(): Int {
        var count = 0
        var bn: BigNode? = lastNode
        while (bn != null) {
            count += 1
            bn = bn.previousNode
        }

        return count
    }

    /**
     * Freezes the memory creating a differential copy of the memory.
     */
    fun freezeCopy(): BigNode {
        val res = lastNode
        lastNode = BigNode(previousNode = lastNode, nextNode = null)
        res.nextNode = lastNode
        return res
    }

    /**
     * Rollback the last copy, removing all changes since then.
     */
    fun rollbackCopy() {
        // Prevents the deletion if it is the root node.
        if (lastNode == firstNode) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FirstBigNodeRollback,
                    "The memory cannot rollback the first BigNode") {}
        }

        // Get the previous recoverable node.
        var node = lastNode.previousNode
        while (node != null) {
            if (node.isRecoverable) {
                break
            }

            node = node.previousNode
        }

        restoreCopy(node ?: throw AngmarUnreachableException())
    }

    /**
     * Restores the specified copy, removing all changes since then.
     */
    fun restoreCopy(bigNode: BigNode) {
        if (!bigNode.isRecoverable) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.NonRecoverableNodeRollback,
                    "The memory cannot rollback a non-recoverable BigNode") {}
        }

        bigNode.nextNode?.destroy()
        bigNode.nextNode = null
        lastNode = bigNode
    }

    /**
     * Collapses all the big nodes from the current one to the specified.
     */
    fun collapseTo(bigNode: BigNode) {
        // Avoid to collapse when the destination is the same bigNode.
        if (lastNode == bigNode) {
            return
        }

        // Mark the nodes counting the heap elements.
        var count = 0
        var node: BigNode? = bigNode
        while (node != null) {
            // Avoid to increase twice a node that in a previous
            // collapse was counted.
            if (node.isRecoverable) {
                node.isRecoverable = false
                count += node.heapSize
            }

            node = node.nextNode
        }

        // Creates a new bigNode that represents the old one.
        freezeCopy()

        // Add the count to the bigNode.
        lastNode.temporalGarbageCollectorCount += count
    }

    /**
     * Collects all the garbage of the current big node.
     */
    fun spatialGarbageCollect(forced: Boolean = false) {
        isInGarbageCollectionMode = true
        lastNode.spatialGarbageCollect(this, forced)
        isInGarbageCollectionMode = false
    }

    /**
     * Collapses all the garbage of the big node history.
     */
    fun temporalGarbageCollect() {
        for ((from, destination) in temporalGarbageCollectFindGroups()) {
            val prev = destination.previousNode
            val next = from.nextNode
            from.temporalGarbageCollect(destination)

            prev?.nextNode = destination
            destination.previousNode = prev
            destination.nextNode = next
            next?.previousNode = destination

            // Update the lastNode.
            if (from == lastNode) {
                lastNode = destination
            }
        }
    }

    /**
     * Find non-recoverable groups.
     */
    private fun temporalGarbageCollectFindGroups() = sequence {
        var state = 0
        var from: BigNode? = lastNode
        var destination: BigNode? = lastNode
        var node: BigNode? = lastNode
        stateLoop@ while (state >= 0) {
            when (state) {
                // Find first.
                0 -> {
                    while (node != null) {
                        if (!node.isRecoverable) {
                            from = node.nextNode ?: throw AngmarAnalyzerException(
                                    AngmarAnalyzerExceptionType.LastBigNodeTemporalGarbageCollection,
                                    "The temporal garbage collector cannot remove a non-recoverable bigNode without a next one.") {}

                            state = 1
                            continue@stateLoop
                        } else {
                            // No more non-recoverable bigNodes before this one.
                            if (node.temporalGarbageCollectorCount == 0) {
                                break@stateLoop
                            }

                            // Clears the count of the recoverable bigNodes.
                            node.temporalGarbageCollectorCount = 0
                        }

                        node = node.previousNode
                    }

                    // Exit
                    state = -1
                }
                // Find end.
                1 -> {
                    while (node != null) {
                        if (node.isRecoverable) {
                            break
                        }

                        destination = node
                        node = node.previousNode
                    }

                    // Collects the garbage.
                    yield(Pair(from!!, destination!!))

                    from = null
                    destination = null

                    state = 0
                }
            }
        }
    }
}
