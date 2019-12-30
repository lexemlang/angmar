package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.bignode.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*

/**
 * A big node that represents an differential view of the memory.
 */
internal class BigNode constructor(var previousNode: BigNode?, var nextNode: BigNode?) {
    private val stack: BigNodeStack = previousNode?.stack?.clone(this) ?: BigNodeStack(this)
    private val heap: BigNodeHeap = previousNode?.heap?.clone(this) ?: BigNodeHeap(this, 0)

    /**
     * The number of elements in the current [BigNode]'s stack.
     */
    val stackSize get() = stack.size

    /**
     * The number of cells in the current [BigNode]'s heap.
     */
    val heapSize get() = heap.cellCount

    /**
     * The position of the last empty cell that can be used to hold new information.
     * Used to avoid fragmentation.
     */
    var lastFreePosition: Int = previousNode?.lastFreePosition ?: actualHeapSize
        private set

    /**
     * Gets the free space percentage.
     */
    val freeSpacePercentage get() = 100 - (actualUsedCellCount * 100.0 / maxOf(actualHeapSize, garbageThreshold))

    // METHODS ----------------------------------------------------------------

    /**
     * Adds a new value into the stack by a name.
     */
    fun addToStack(name: String, value: LexemPrimitive, memory: LexemMemory) {
        // Increase the reference count of the incoming value.
        value.increaseReferences(memory)

        // Get the last stack level.
        var level = getStackLevelRecursively(actualStackLevelSize - 1) ?: let {
            val level = BigNodeStackLevel.new(0)
            stackLevels[0] = level
            actualStackLevelSize += 1
            level
        }

        // Increase a level if name is inside.
        level = if (level.hasCell(name)) {
            val nextLevel = BigNodeStackLevel.new(actualStackLevelSize)
            stackLevels[actualStackLevelSize] = nextLevel
            actualStackLevelSize += 1
            nextLevel
        } else {
            // Shifts the stack level.
            if (level.position !in stackLevels) {
                level = level.shiftLevel()
                stackLevels[level.position] = level
            }

            level
        }

        // Add the value to the stack.
        level.setCellValue(name, value)
        actualStackSize += 1
    }

    /**
     * Gets the specified value of the stack.
     */
    fun getFromStack(name: String): LexemPrimitive {
        for (i in actualStackLevelSize - 1 downTo 0) {
            val level = getStackLevelRecursively(i)!!
            val value = level.getCellValue(name)
            if (value != null) {
                return value
            }
        }

        return previousNode?.getFromStack(name) ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.StackNotFoundElement, "Not found element called '$name' in the stack.") {}
    }

    /**
     * Removes the specified value of the stack recursively.
     */
    fun removeFromStack(name: String, memory: LexemMemory) {
        // Get the stack level.
        var level: BigNodeStackLevel? = null
        var value: LexemPrimitive? = null
        for (i in actualStackLevelSize - 1 downTo 0) {
            level = getStackLevelRecursively(i)!!
            value = level.getCellValue(name)
            if (value != null) {
                // Shifts the level.
                level = level.shiftLevel()
                stackLevels[i] = level
                break
            }
        }

        if (level == null || value == null) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement,
                    "Not found element called '$name' in the stack.") {}
        }

        // Shift the level to the current big node.
        if (level.position !in stackLevels) {
            level = level.shiftLevel()
            stackLevels[level.position] = level
        }

        // Remove cell.
        level.removeCell(name)
        actualStackSize -= 1

        // Remove the level if it is empty and it is the last one.
        if (level.cellCount == 0 && level.position == actualStackLevelSize - 1) {
            stackLevels.remove(level.position)
            actualStackLevelSize -= 1
            level.destroy()

            // Remove empty stack levels.
            for (i in actualStackLevelSize - 1 downTo 0) {
                val level = getStackLevelRecursively(i)!!
                if (level.cellCount != 0) {
                    break
                }

                if (i in stackLevels) {
                    stackLevels.remove(i)
                }

                actualStackLevelSize -= 1
                level.destroy()
            }
        }

        // Decrease reference count.
        value.decreaseReferences(memory)
    }

    /**
     * Replace the specified stack cell by another primitive.
     */
    fun replaceStackCell(name: String, newValue: LexemPrimitive, memory: LexemMemory) {
        // Get the stack level.
        var level: BigNodeStackLevel? = null
        var value: LexemPrimitive? = null
        for (i in actualStackLevelSize - 1 downTo 0) {
            level = getStackLevelRecursively(i)!!
            value = level.getCellValue(name)
            if (value != null) {
                break
            }
        }

        if (level == null || value == null) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement,
                    "Not found element called '$name' in the stack") {}
        }

        // Shift the level to the current big node.
        if (level.position !in stackLevels) {
            level = level.shiftLevel()
            stackLevels[level.position] = level
        }

        // Increase the reference count of the incoming value.
        if (newValue is LxmReference) {
            getCell(memory, newValue.position, forceShift = true).increaseReferences()
        }

        // Replace the cell.
        level.setCellValue(name, newValue)

        // Decrease reference count.
        value.decreaseReferences(memory)
    }

    /**
     * Gets the specified stack level.
     */
    private fun getStackLevelRecursively(position: Int): BigNodeStackLevel? {
        var distance = 0
        var node: BigNode? = this
        while (node != null) {
            val value = node.stackLevels[position]
            if (value != null) {
                return value
            }

            distance += 1
            node = node.previousNode
        }

        return null
    }

    /**
     * Gets a cell recursively in the [BigNode]'s chain.
     */
    fun getCell(memory: LexemMemory, position: Int, forceShift: Boolean = false): BigNodeHeapCell {
        if (position >= actualHeapSize) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "The analyzer is trying to access a forbidden memory position") {}
        }

        val res = heap[position]
        if (res != null) {
            return res
        }

        val (distance, cell) = previousNode?.getCellRecursive(position) ?: throw AngmarUnreachableException()

        return if (forceShift) {
            val cell2 = cell.shiftCell(memory)
            heap[position] = cell2
            cell2
        } else {
            cell
        }
    }

    /**
     * Gets a value recursively without shifting the value in newer nodes.
     */
    private fun getCellRecursive(position: Int): Pair<Int, BigNodeHeapCell>? {
        var distance = 0
        var node: BigNode? = this
        while (node != null) {
            val value = node.heap[position]
            if (value != null) {
                return Pair(distance, value)
            }

            distance += 1
            node = node.previousNode
        }

        return null
    }

    /**
     * Adds a new cell (or reuses a free one) to hold the specified value
     * returning the cell itself.
     */
    fun alloc(memory: LexemMemory, value: LexemReferenced): BigNodeHeapCell {
        // Prevent errors regarding the BigNode link.
        if (value.bigNode != this) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapBigNodeLinkFault,
                    "The analyzer is trying to save a value in a different bigNode") {}
        }

        // No free cell.
        if (lastFreePosition == actualHeapSize) {
            // Execute the garbage collector to free space.
            if (actualHeapSize == garbageThreshold) {
                spatialGarbageCollectorMark = true
            }

            val cell = BigNodeHeapCell.new(lastFreePosition, value)
            heap[lastFreePosition] = cell
            lastFreePosition += 1
            actualHeapSize += 1
            actualUsedCellCount += 1

            return cell
        }

        // Reuse a free cell.
        val cell = getCell(memory, lastFreePosition, forceShift = true)
        lastFreePosition = cell.referenceCount
        cell.reallocCell(memory, value)
        actualUsedCellCount += 1

        return cell
    }

    /**
     * Frees a memory cell to reuse it in the future.
     */
    fun free(memory: LexemMemory, position: Int) {
        var cell = getCell(memory, position)
        if (!cell.isFreed) {
            cell = getCell(memory, position, forceShift = true)
            cell.freeCell(memory)
            lastFreePosition = cell.position
            actualUsedCellCount -= 1
        }
    }

    /**
     * Clears the [BigNode] destroying its cells to reuse them.
     */
    fun destroy() {
        // Clears the stack.
        for ((_, level) in stackLevels) {
            level.destroy()
        }

        // Destroys all cells to reuse them.
        for ((_, cell) in heap) {
            cell.destroy()
        }

        actualStackSize = 0
        actualStackLevelSize = 0
        actualHeapSize = 0
        actualUsedCellCount = 0
        lastFreePosition = 0
        stackLevels.clear()
        heap.clear()
        previousNode = null
        nextNode?.destroy()
        nextNode = null
    }

    /**
     * Collects all the garbage of the current big node.
     */
    fun spatialGarbageCollect(memory: LexemMemory, forced: Boolean = false) {
        // Avoid to execute the garbage collector when there are enough free space.
        if (!forced && freeSpacePercentage >= Consts.Memory.spatialGarbageCollectorMinimumFreeSpace) {
            return
        }

        // Track from the main context and stack.
        val gcFifo = GarbageCollectorFifo(actualHeapSize)

        // Track the stdlib and hidden contexts.
        LxmReference.StdLibContext.spatialGarbageCollect(memory, gcFifo)
        LxmReference.HiddenContext.spatialGarbageCollect(memory, gcFifo)

        // Track the stack.
        for (i in actualStackLevelSize - 1 downTo 0) {
            val level = getStackLevelRecursively(i)!!
            for ((_, value) in level.cellValues) {
                value.spatialGarbageCollect(memory, gcFifo)
            }
        }

        // Track.
        var position = gcFifo.pop()
        while (position != null) {
            getCell(memory, position, false).value?.spatialGarbageCollect(memory, gcFifo)

            position = gcFifo.pop()
        }

        // Clean memory.
        for (i in gcFifo) {
            free(memory, i)
        }

        // Update the threshold only under the minimum quantity of free space.
        if (freeSpacePercentage < Consts.Memory.spatialGarbageCollectorMinimumFreeSpace) {
            garbageThreshold = (garbageThreshold * Consts.Memory.spatialGarbageCollectorThresholdIncrement).toInt()
        }

        spatialGarbageCollectorMark = false
    }
}
