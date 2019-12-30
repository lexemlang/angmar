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

        restoreCopy(lastNode.previousNode ?: throw AngmarUnreachableException())
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
    fun collapseTo(bigNode: BigNode) {
        // Avoid to collapse when the destination is the same bigNode.
        if (lastNode == bigNode) {
            return
        }

        // Un-links the previous bigNode.
        lastNode.previousNode!!.nextNode = null

        // Destroys the bigNode.
        val previousNode = bigNode.previousNode!!
        bigNode.destroy()

        // Link again.
        previousNode.nextNode = lastNode
        lastNode.previousNode = previousNode
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
