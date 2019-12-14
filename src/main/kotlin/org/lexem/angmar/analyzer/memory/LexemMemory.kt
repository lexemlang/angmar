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
    fun addToStack(name: String, primitive: LexemPrimitive) = lastNode.addToStack(name, primitive, this)

    /**
     * Adds a new primitive into the stack with '[AnalyzerCommons.Identifiers.Last]' as name.
     */
    fun addToStackAsLast(primitive: LexemPrimitive) = addToStack(AnalyzerCommons.Identifiers.Last, primitive)

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
    fun replaceStackCell(name: String, newValue: LexemPrimitive) = lastNode.replaceStackCell(name, newValue, this)

    /**
     * Replace the '[AnalyzerCommons.Identifiers.Last]' stack cell by another primitive.
     */
    fun replaceLastStackCell(newValue: LexemPrimitive) = replaceStackCell(AnalyzerCommons.Identifiers.Last, newValue)

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
            lastNode.getCell(newValue.position, forceShift = true).increaseReferences()
        }

        // Removes the previous reference.
        if (oldValue is LxmReference) {
            lastNode.getCell(oldValue.position, forceShift = true).decreaseReferences(this)
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

        val nodeToRemove = lastNode
        lastNode = lastNode.previousNode!!
        lastNode.nextNode = null

        nodeToRemove.destroy()
    }

    /**
     * Restores the specified copy, removing all changes since then.
     */
    fun restoreCopy(bigNode: BigNode) {
        bigNode.nextNode?.destroy()
        bigNode.nextNode = null
        lastNode = bigNode
    }

    /**
     * Collapses all the big nodes from the current one to the specified.
     */
    fun collapseTo(bigNode: BigNode, forceGarbageCollection: Boolean = false) {
        lastNode.collapseTo(bigNode)
        lastNode = bigNode
        spatialGarbageCollect(forceGarbageCollection)
    }

    /**
     * Collects all the garbage of the current big node.
     */
    fun spatialGarbageCollect(forced: Boolean = false) {
        isInGarbageCollectionMode = true
        lastNode.spatialGarbageCollect(this, forced)
        isInGarbageCollectionMode = false
    }
}
