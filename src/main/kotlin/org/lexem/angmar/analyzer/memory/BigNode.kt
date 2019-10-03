package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*

/**
 * A big node that represents an differential view of the memory.
 */
internal class BigNode constructor(val previousNode: BigNode?) {
    private val stack = mutableMapOf<Int, LexemPrimitive>()
    private val heap = mutableMapOf<Int, BigNodeCell>()

    /**
     * The number of elements in the current [BigNode]'s stack.
     */
    val stackSize get() = stack.size

    /**
     * The number of elements in the whole stack.
     */
    var actualStackSize: Int = previousNode?.actualStackSize ?: 0
        private set

    /**
     * The number of cells in the current [BigNode]'s heap.
     */
    val heapSize get() = heap.size

    /**
     * The number of cells in the whole heap.
     */
    var actualHeapSize: Int = previousNode?.actualHeapSize ?: 0
        private set

    /**
     * The number of stored elements in the whole heap.
     */
    var actualUsedCellCount: Int = previousNode?.actualUsedCellCount ?: 0
        private set

    /**
     * The position of the last empty cell that can be used to hold new information.
     * Used to avoid fragmentation.
     */
    var lastFreePosition: Int = previousNode?.lastFreePosition ?: actualHeapSize
        private set

    // Methods ----------------------------------------------------------------

    /**
     * Pushes a new value into the stack.
     */
    fun pushStack(value: LexemPrimitive) {
        if (value is LxmReference) {
            getCell(value.position).increaseReferenceCount()
        }

        pushStackIgnoringReferenceCount(value)
    }

    /**
     * Pushes a new primitive into the stack ignoring the reference count.
     */
    fun pushStackIgnoringReferenceCount(value: LexemPrimitive) {
        stack[actualStackSize] = value
        actualStackSize += 1
    }

    /**
     * Pops the last value of the stack recursively.
     */
    fun popStack(): LexemPrimitive {
        // Prevent to get a value if the last index of the stack is lower than 0.
        if (actualStackSize <= 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.StackUnderflow,
                    "The analyzer has tried to get a value from an empty stack") {}
        }

        actualStackSize -= 1
        val res = getStackRecursively(actualStackSize) ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.StackNotFoundElement,
                "Not found element in the stack at position ($actualStackSize)") {}

        if (stack.containsKey(actualStackSize)) {
            stack.remove(actualStackSize)
        }

        return res
    }

    /**
     * Gets a position in the stack recursively.
     */
    private fun getStackRecursively(position: Int): LexemPrimitive? =
            stack[position] ?: previousNode?.getStackRecursively(position)

    /**
     * Gets a cell recursively in the [BigNode]'s chain.
     */
    fun getCell(position: Int): BigNodeCell {
        if (position >= actualHeapSize) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "The analyzer is trying to access a forbidden memory position") {}
        }

        var res = heap[position]
        if (res != null) {
            return res
        }

        res = previousNode?.getCellRecursive(position)
        if (res != null) {
            res = res.shiftCell()
            heap[position] = res
            return res
        }

        // This should never happened.
        throw AngmarUnreachableException()
    }

    /**
     * Gets a value recursively without shifting the value in newer nodes.
     */
    private fun getCellRecursive(position: Int): BigNodeCell? =
            heap[position] ?: previousNode?.getCellRecursive(position)

    /**
     * Sets a new value to the cell at the specified position.
     */
    fun setCell(position: Int, value: LexemReferenced) {
        if (position >= actualHeapSize) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "The analyzer is trying to access a forbidden memory position") {}
        }

        val cell = getCell(position)
        cell.setValue(value)
    }

    /**
     * Adds a new cell (or reuses a free one) to hold the specified value
     * returning the cell itself.
     */
    fun alloc(memory: LexemMemory, value: LexemReferenced): BigNodeCell {
        // No free cell.
        if (lastFreePosition == actualHeapSize) {
            val cell = BigNodeCell.new(lastFreePosition, value)
            heap[lastFreePosition] = cell
            lastFreePosition += 1
            actualHeapSize += 1
            actualUsedCellCount += 1

            return cell
        }

        // Reuse a free cell.
        val cell = getCell(lastFreePosition)
        lastFreePosition = cell.referenceCount
        cell.reallocCell(memory, value)
        actualUsedCellCount += 1

        return cell
    }

    /**
     * Frees a memory cell to reuse it in the future.
     */
    fun free(memory: LexemMemory, position: Int) {
        val cell = getCell(position)
        cell.freeCell(memory, lastFreePosition)
        lastFreePosition = cell.position
        actualUsedCellCount -= 1
    }

    /**
     * Frees a memory cell to reuse it in the future.
     */
    fun freeAsGarbage(memory: LexemMemory, position: Int) {
        val cell = getCell(position)
        cell.freeCellAsGarbage(memory, lastFreePosition)
        lastFreePosition = cell.position
        actualUsedCellCount -= 1
    }

    /**
     * Clears the [BigNode] destroying its cells to reuse them.
     */
    fun destroy() {
        // Clears the stack.
        repeat(stackSize) {
            popStack()
        }

        stack.clear()

        // Destroys all cells to reuse them.
        for (cell in heap) {
            cell.value.destroy()
        }

        actualStackSize = previousNode?.actualStackSize ?: 0
        actualHeapSize = previousNode?.actualHeapSize ?: 0
        actualUsedCellCount = previousNode?.actualUsedCellCount ?: 0
        lastFreePosition = previousNode?.lastFreePosition ?: actualHeapSize
        stack.clear()
        heap.clear()
    }
}
