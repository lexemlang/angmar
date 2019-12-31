package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

/**
 * The representation of a level-1 heap page.
 */
internal class BigNodeL1HeapPage(val bigNode: BigNode, val position: Int) {
    private var cells = hashMapOf<Int, BigNodeHeapCell>()
    private var isCellsCloned = true

    /**
     * The number of [BigNodeHeapCell]s in this [BigNodeL1HeapPage].
     */
    val size get() = cells.size

    /**
     * The last position of this [BigNodeL1HeapPage].
     */
    val lastIndex get() = position + (1 shl Consts.Memory.heapPageBits) - 1

    // METHODS ----------------------------------------------------------------

    /**
     * Gets a [BigNodeHeapCell].
     */
    fun getCell(position: Int, toWrite: Boolean): BigNodeHeapCell {
        val index = position and Consts.Memory.heapPageL1Mask
        var cell = cells[index] ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                "The analyzer is trying to access a forbidden memory position") {}

        // If the cell is not in the current bigNode, copy it.
        if (toWrite && cell.bigNode != bigNode) {
            cloneCells()

            cell = cell.clone(bigNode)
            cells[index] = cell
        }

        return cell
    }

    /**
     * Sets a [BigNodeHeapCell].
     * @return Whether a new cell has been added.
     */
    fun setCell(newCell: BigNodeHeapCell): Boolean {
        cloneCells()

        val result = newCell.position in cells
        cells[newCell.position] = newCell

        return result
    }

    /**
     * Clones this [BigNodeL1HeapPage].
     */
    fun clone(newBigNode: BigNode): BigNodeL1HeapPage {
        val res = BigNodeL1HeapPage(newBigNode, position)
        res.isCellsCloned = false
        res.cells = cells

        return res
    }

    /**
     * Clones the cells.
     */
    private fun cloneCells() {
        if (!isCellsCloned) {
            cells = cells.toHashMap()
            isCellsCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "[$position..$lastIndex] Size: $size"
}

