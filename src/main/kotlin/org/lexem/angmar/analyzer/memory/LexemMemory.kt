package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*

/**
 * The representation of the memory of the analyzer. Initiates with the standard library loaded.
 */
internal class LexemMemory : IMemory {
    val firstNode = BigNode(previousNode = null, nextNode = null)
    var lastNode = firstNode
        private set

    // METHODS ----------------------------------------------------------------

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
    fun freezeCopy(rollbackCodePoint: LxmRollbackCodePoint) {
        // Set rollback code point.
        lastNode.rollbackCodePoint = rollbackCodePoint

        // Make copy.
        val oldLastNode = lastNode
        lastNode = BigNode(previousNode = lastNode, nextNode = null)
        oldLastNode.nextNode = lastNode
    }

    /**
     * Rollback the last copy, removing all changes since then.
     */
    fun rollbackCopy(): LxmRollbackCodePoint {
        // Prevents the deletion if it is the root node.
        if (lastNode == firstNode) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FirstBigNodeRollback,
                    "The memory cannot rollback the first BigNode") {}
        }

        return restoreCopy(lastNode.previousNode ?: throw AngmarUnreachableException())
    }

    /**
     * Restores the specified copy, removing all changes since then.
     */
    fun restoreCopy(bigNode: BigNode): LxmRollbackCodePoint {
        bigNode.nextNode?.destroy()
        bigNode.nextNode = null
        lastNode = bigNode

        return bigNode.rollbackCodePoint ?: throw AngmarUnreachableException()
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
    fun spatialGarbageCollect() {
        lastNode.spatialGarbageCollect()
    }

    // OVERRIDDEN METHODS ------------------------------------------------------

    override fun getBigNodeId() = lastNode.id

    override fun addToStack(name: String, primitive: LexemMemoryValue) = lastNode.addToStack(name, primitive)

    override fun addToStackAsLast(primitive: LexemMemoryValue) = addToStack(AnalyzerCommons.Identifiers.Last, primitive)

    override fun getFromStack(name: String) = lastNode.getFromStack(name)

    override fun getLastFromStack() = getFromStack(AnalyzerCommons.Identifiers.Last)

    override fun removeFromStack(name: String) = lastNode.removeFromStack(name)

    override fun removeLastFromStack() = removeFromStack(AnalyzerCommons.Identifiers.Last)

    override fun renameStackCell(oldName: String, newName: String) = lastNode.renameStackCell(oldName, newName)

    override fun renameLastStackCell(newName: String) = renameStackCell(AnalyzerCommons.Identifiers.Last, newName)

    override fun renameStackCellToLast(oldName: String) = renameStackCell(oldName, AnalyzerCommons.Identifiers.Last)

    override fun replaceStackCell(name: String, newValue: LexemMemoryValue) = lastNode.replaceStackCell(name, newValue)

    override fun replaceLastStackCell(newValue: LexemMemoryValue) =
            replaceStackCell(AnalyzerCommons.Identifiers.Last, newValue)

    override fun get(reference: LxmReference, toWrite: Boolean) = lastNode.get(reference, toWrite)

    override fun getCell(reference: LxmReference, toWrite: Boolean) = lastNode.getCell(reference, toWrite)

    override fun add(value: LexemReferenced) = lastNode.add(value)

    override fun remove(reference: LxmReference) = lastNode.remove(reference)
}
