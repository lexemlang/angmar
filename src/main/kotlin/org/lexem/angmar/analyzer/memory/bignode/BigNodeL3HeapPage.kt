package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*

/**
 * The representation of a level-3 heap page.
 */
internal class BigNodeL3HeapPage(val position: Int) {
    private var pages = hashMapOf<Int, BigNodeL2HeapPage>()

    /**
     * The number of [BigNodeL2HeapPage]s in this [BigNodeL3HeapPage].
     */
    val size get() = pages.size

    /**
     * The mask for a [BigNodeL3HeapPage].
     */
    private val mask get() = Consts.Memory.heapPageL4Mask

    /**
     * The last position of this [BigNodeL3HeapPage].
     */
    val lastIndex get() = mask.inv() + 1

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
     * @return Whether a new cell has been added.
     */
    fun setCell(position: Int, newCell: BigNodeHeapCell): Boolean {
        val index = position and mask
        val page = pages[index] ?: let {
            val page = BigNodeL2HeapPage(index)
            pages[index] = page
            page
        }

        return page.setCell(position, newCell)
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
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "[$position..$lastIndex] Pages: $size"
}

