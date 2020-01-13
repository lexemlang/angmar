package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*

/**
 * The representation of a memory heap.
 */
internal class BigNodeHeap {
    private var pages = hashMapOf<Int, BigNodeL3HeapPage>()

    /**
     * The number of [BigNodeL3HeapPage]s in this [BigNodeHeap].
     */
    val size get() = pages.size

    /**
     * The number of [BigNodeHeapCell]s in this [BigNodeHeap].
     */
    var cellCount = 0
        private set

    /**
     * The mask for a [BigNodeHeap].
     */
    private val mask get() = Consts.Memory.heapPageL5Mask

    /**
     * The minimum number of cells to call the garbage collector in synchronous mode.
     */
    var garbageCollectorThreshold = Consts.Memory.garbageCollectorInitialThreshold

    // METHODS ----------------------------------------------------------------

    /**
     * Gets a [BigNodeHeapCell].
     */
    fun getCell(bigNode: BigNode, position: Int, toWrite: Boolean): BigNodeHeapCell {
        val index = position and mask
        val page = pages[index] ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                "The analyzer is trying to access a forbidden memory position") {}

        return page.getCell(bigNode, position, toWrite)
    }

    /**
     * Sets a [BigNodeHeapCell].
     */
    fun setCell(position: Int, newCell: BigNodeHeapCell) {
        val index = position and mask
        val page = pages[index] ?: let {
            val page = BigNodeL3HeapPage(index)
            pages[index] = page
            page
        }

        if (page.setCell(position, newCell)) {
            cellCount += 1
        }
    }

    /**
     * Removes a [BigNodeHeapCell] from the memory.
     */
    fun removeCell(position: Int) {
        val index = position and mask
        val page = pages[index] ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                "The analyzer is trying to access a forbidden memory position") {}
        page.removeCell(position)

        if (page.size == 0) {
            pages.remove(index)
        }

        cellCount -= 1
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "[Heap mask: 0x${mask.toString(16)}] Cells: $cellCount, Pages: $size"
}

