package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

/**
 * The representation of a memory stack.
 */
internal class BigNodeStack(val bigNode: BigNode) {
    private var cells = hashMapOf<String, BigNodeStackCell>()
    private var isCellsCloned = true

    /**
     * The number of [BigNodeStackCell]s in this [BigNodeStack].
     */
    var size = 0
        private set

    // METHODS ----------------------------------------------------------------

    /**
     * Gets a [LexemPrimitive] from the stack by a name.
     */
    fun getCell(name: String) =
            cells[name]?.value ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement,
                    "Not found element called '$name' in the stack.") {}

    /**
     * Adds a [LexemPrimitive] into the stack by a name.
     */
    fun addCell(name: String, value: LexemPrimitive) {
        cloneCells()

        // Increase the reference count of the incoming value.
        value.increaseReferences(bigNode)

        // Get the cell.
        val previousCell = cells[name]
        cells[name] = BigNodeStackCell(value, previousCell)
        size += 1
    }

    /**
     * Removes a [LexemPrimitive] from the stack by a name.
     */
    fun removeCell(name: String) {
        cloneCells()

        val cell = cells[name] ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement,
                "Not found element called '$name' in the stack.") {}
        if (cell.previousCell != null) {
            cells[name] = cell.previousCell
        } else {
            cells.remove(name)
        }

        size -= 1

        // Decrease reference count.
        cell.value.decreaseReferences(bigNode)
    }

    /**
     * Replaces a [LexemPrimitive] into the stack by a name.
     */
    fun replaceCell(name: String, newValue: LexemPrimitive) {
        cloneCells()

        // Increase the reference count of the incoming value.
        newValue.increaseReferences(bigNode)

        val cell = cells[name] ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement,
                "Not found element called '$name' in the stack.") {}
        val previous = cell.previousCell
        val newCell = BigNodeStackCell(newValue, previous)
        cells[name] = newCell // Critical operation, may interfere with the GC.

        // Decrease reference count.
        cell.value.decreaseReferences(bigNode)
    }

    /**
     * Clones this [BigNodeStack].
     */
    fun clone(newBigNode: BigNode): BigNodeStack {
        if (newBigNode == bigNode) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CloneOverTheSameBigNode,
                    "Cannot the heap over the same bigNode.") {}
        }

        val res = BigNodeStack(newBigNode)
        res.isCellsCloned = false
        res.cells = cells
        res.size = size

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

    /**
     * Iterator for garbage collector.
     */
    fun gcIterator() = sequence {
        for ((_, cellAux) in cells) {
            var cell: BigNodeStackCell? = cellAux
            while (cell != null) {
                yield(cell.value)
                cell = cell.previousCell
            }
        }
    }.iterator()

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "[Stack] Size: $size"
}
