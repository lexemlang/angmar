package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*

/**
 * The representation of a memory heap.
 */
internal class BigNodeHeap(val bigNode: BigNode) {
    private var pages = mutableMapOf<Int, BigNodeL3HeapPage>()
    private var isPagesCloned = true

    /**
     * The number of [BigNodeL3HeapPage]s in this [BigNodeHeap].
     */
    val size get() = pages.size

    /**
     * The number of [BigNodeHeapCell]s in this [BigNodeHeap].
     */
    val cellCount get() = pages.asSequence().map { it.value.cellCount }.sum()

    /**
     * The mask for a [BigNodeHeap].
     */
    private val mask get() = 1 shl Consts.Memory.heapPageL4Mask

    // METHODS ----------------------------------------------------------------

    /**
     * Gets a [BigNodeHeapCell].
     */
    fun getCell(position: Int, toWrite: Boolean): BigNodeHeapCell {
        val index = position and mask
        var page = pages[index] ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                "The analyzer is trying to access a forbidden memory position") {}

        // If the page is not in the current bigNode, copy it.
        if (toWrite && page.bigNode != bigNode) {
            clonePages()

            page = page.clone(bigNode)
            pages[index] = page
        }

        return page.getCell(position, toWrite)
    }

    /**
     * Sets a [BigNodeHeapCell].
     */
    fun setCell(position: Int, newCell: BigNodeHeapCell) {
        val index = position and mask
        clonePages()

        var page = pages[index] ?: let {
            val page = BigNodeL3HeapPage(bigNode, index)
            pages[index] = page
            page
        }

        if (page.bigNode != bigNode) {
            page = page.clone(bigNode)
            pages[index] = page
        }

        page.setCell(position, newCell)
    }

    /**
     * Removes a [BigNodeHeapCell].
     */
    fun removeCell(position: Int) {
        val index = position and mask
        clonePages()

        var page = pages[index] ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                "The analyzer is trying to access a forbidden memory position") {}

        if (page.bigNode != bigNode) {
            page = page.clone(bigNode)
            pages[index] = page
        }

        page.removeCell(position)

        if (page.size == 0) {
            pages.remove(index)
        }
    }

    /**
     * Clones this [BigNodeHeap].
     */
    fun clone(newBigNode: BigNode): BigNodeHeap {
        val res = BigNodeHeap(newBigNode)
        res.isPagesCloned = false
        res.pages = pages

        return res
    }

    /**
     * Clones the pages.
     */
    private fun clonePages() {
        if (!isPagesCloned) {
            pages = pages.toMutableMap()
            isPagesCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "[Heap] Size: $size"
}

