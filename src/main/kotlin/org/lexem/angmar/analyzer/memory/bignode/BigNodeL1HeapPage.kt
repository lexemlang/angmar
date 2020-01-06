package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*

/**
 * The representation of a level-1 heap page.
 */
internal class BigNodeL1HeapPage(val position: Int) {
    private val cells = hashMapOf<Int, BigNodeHeapCell>()

    /**
     * The number of [BigNodeHeapCell]s in this [BigNodeL1HeapPage].
     */
    val size get() = cells.size

    /**
     * The mask for a [BigNodeL1HeapPage].
     */
    private val mask get() = Consts.Memory.heapPageL1Mask

    /**
     * The last position of this [BigNodeL1HeapPage].
     */
    val lastIndex get() = mask.inv() + 1

    // METHODS ----------------------------------------------------------------

    /**
     * Gets a [BigNodeHeapCell].
     */
    fun getCell(bigNode: BigNode, position: Int, toWrite: Boolean): BigNodeHeapCell {
        val index = position and mask
        var cell = cells[index] ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                "The analyzer is trying to access a forbidden memory position") {}

        if (cell.bigNodeIndex != bigNode.id) {
            // Remove old.
            var hasChanged = false
            while (cell.bigNodeIndex > bigNode.id) {
                cell = cell.oldCell ?: BigNodeHeapCell(bigNode.id, bigNode.lastFreePosition.getAndSet(position))
                hasChanged = true
            }

            // Set new if in toWrite mode.
            if (toWrite && cell.bigNodeIndex != bigNode.id) {
                cell = cell.clone(bigNode.id)
                hasChanged = true
            }

            if (hasChanged) {
                cells[index] = cell
            }
        }

        return cell
    }

    /**
     * Sets a [BigNodeHeapCell].
     * @return Whether a new cell has been added.
     */
    fun setCell(position: Int, newCell: BigNodeHeapCell): Boolean {
        val result = position !in cells
        cells[position] = newCell

        return result
    }

    /**
     * Removes a [BigNodeHeapCell] from the memory.
     */
    fun removeCell(position: Int) {
        val index = position and mask
        if (index !in cells) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "The analyzer is trying to access a forbidden memory position") {}
        }

        cells.remove(index)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "[$position..$lastIndex] Cells: $size"
}

