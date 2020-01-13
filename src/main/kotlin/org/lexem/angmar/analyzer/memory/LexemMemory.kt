package org.lexem.angmar.analyzer.memory

import kotlinx.coroutines.channels.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.bignode.*
import org.lexem.angmar.errors.*
import java.util.concurrent.atomic.*

/**
 * The representation of the memory of the analyzer. Initiates with the standard library loaded.
 */
internal class LexemMemory : IMemory {
    var nextId = 0
        private set
    private val firstNode = BigNode(nextId)
    private val lastNodeProperty = AtomicReference(firstNode)
    var gcChannel: Channel<Unit>? = null

    init {
        nextId += 1
    }

    val lastNode get() = lastNodeProperty.get()!!

    // METHODS ----------------------------------------------------------------

    /**
     * Clears the memory.
     */
    fun clear() {
        firstNode.nextNode?.destroyFromAlive(gcChannel)
        firstNode.nextNode = null
        lastNodeProperty.set(firstNode)
    }

    /**
     * Freezes the memory creating a differential copy of the memory.
     */
    fun freezeCopy(rollbackCodePoint: LxmRollbackCodePoint) {
        // Set rollback code point.
        lastNode.rollbackCodePoint = rollbackCodePoint

        // Make copy.
        val oldLastNode = lastNode
        lastNodeProperty.set(BigNode(nextId, previousNode = lastNode))
        oldLastNode.nextNode = lastNode
        nextId += 1
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
        bigNode.nextNode?.destroyFromAlive(gcChannel)
        bigNode.nextNode = null
        lastNodeProperty.set(bigNode)

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

        // Unlink.
        var newPreviousNode = lastNode.previousNode!!
        newPreviousNode.nextNode = null

        // Find the node to get.
        while (newPreviousNode.id >= bigNode.id) {
            newPreviousNode = newPreviousNode.previousNode!!
        }

        // Destroy the intermediate node chain.
        newPreviousNode.nextNode?.destroyCollapsing()

        // Link again.
        newPreviousNode.nextNode = lastNode
        lastNode.previousNode = newPreviousNode
    }

    /**
     * Executes the garbage collector in soft mode, removing unused cells and
     * collapsing others.
     */
    fun temporalGarbageCollector() {
        // Iterate over all the cells.
        var index = 0
        while (index < lastNode.heapCapacity) {
            // Get first alive cell.
            var mainCell = findAliveCellInOldSequence(lastNode.getHeapCell(index, false))

            // Set the cell as freed because there is no alive for this position.
            if (mainCell == null) {
                // Free the cell.
                mainCell = lastNode.freeHeapCell(index)

                // Remove the old ones.
                mainCell.oldCell.set(null)
            } else {
                // Get the last alive/collapsed before a dead or another collapsed.
                var lastAlive: BigNodeHeapCell = mainCell
                var isLastCollapsed = lastNode.aliveBigNodes.isCollapsed(this, lastAlive.bigNodeIndex)
                var current = lastAlive.oldCell.get()

                while (current != null) {
                    if (isLastCollapsed) {
                        // Alive.
                        if (lastNode.aliveBigNodes.isAlive(this, current.bigNodeIndex)) {
                            lastAlive.oldCell.set(current)
                            lastAlive = current
                            isLastCollapsed = false
                        }
                    } else {
                        // Alive.
                        if (!lastNode.aliveBigNodes.isRemoved(this, current.bigNodeIndex)) {
                            lastAlive.oldCell.set(current)
                            lastAlive = current
                            isLastCollapsed = lastNode.aliveBigNodes.isCollapsed(this, lastAlive.bigNodeIndex)
                        }
                    }

                    current = current.oldCell.get()
                }

                lastAlive.oldCell.set(current)
            }

            index += 1
        }
    }

    /**
     * Finds a cell that is alive.
     */
    private fun findAliveCellInOldSequence(cell: BigNodeHeapCell?): BigNodeHeapCell? {
        var result = cell
        while (result != null && !lastNode.aliveBigNodes.isAliveOrCollapsed(this, result.bigNodeIndex)) {
            result = result.oldCell.get()
        }

        return result
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
