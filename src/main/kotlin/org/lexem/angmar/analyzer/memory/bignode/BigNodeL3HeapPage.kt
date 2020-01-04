package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

/**
 * The representation of a level-3 heap page.
 */
internal class BigNodeL3HeapPage(val bigNode: BigNode, val position: Int) {
    private var pages = hashMapOf<Int, BigNodeL2HeapPage>()
    private var isPagesCloned = true

    /**
     * The number of [BigNodeL2HeapPage]s in this [BigNodeL3HeapPage].
     */
    val size get() = pages.size

    /**
     * The mask for a [BigNodeL3HeapPage].
     */
    private val mask get() = Consts.Memory.heapPageL3Mask

    /**
     * The last position of this [BigNodeL3HeapPage].
     */
    val lastIndex get() = mask.inv() + 1

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
     * @return Whether a new cell has been added.
     */
    fun setCell(newCell: BigNodeHeapCell): Boolean {
        if (newCell.bigNode != bigNode) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.DifferentBigNodeLink,
                    "Cannot set a cell into a page with different bigNode.") {}
        }

        val index = newCell.position and mask
        clonePages()

        var page = pages[index] ?: let {
            val page = BigNodeL2HeapPage(bigNode, index)
            pages[index] = page
            page
        }

        if (page.bigNode != bigNode) {
            page = page.clone(bigNode)
            pages[index] = page
        }

        return page.setCell(newCell)
    }

    /**
     * Clones this [BigNodeL3HeapPage].
     */
    fun clone(newBigNode: BigNode): BigNodeL3HeapPage {
        if (newBigNode == bigNode) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CloneOverTheSameBigNode,
                    "Cannot clone a page over the same bigNode.") {}
        }

        val res = BigNodeL3HeapPage(newBigNode, position)
        res.isPagesCloned = false
        res.pages = pages

        return res
    }

    /**
     * Clones the pages.
     */
    private fun clonePages() {
        if (!isPagesCloned) {
            pages = pages.toHashMap()
            isPagesCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "[$position..$lastIndex] Pages: $size"
}

