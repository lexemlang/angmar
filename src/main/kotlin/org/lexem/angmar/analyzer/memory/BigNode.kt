package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * A big node that represents an differential view of the memory.
 */
internal class BigNode constructor(var previousNode: BigNode?, var nextNode: BigNode?) {
    private var stackLevels: MutableList<MutableMap<String, LexemPrimitive>> =
            previousNode?.stackLevels ?: mutableListOf()
    private var heap: ConcurrentHashMap<Long, LexemReferenced> = previousNode?.heap ?: ConcurrentHashMap(50)

    /**
     * Whether this [BigNode] is cleaned regarding the spatial garbage collector.
     */
    var isCleaned = AtomicBoolean(true)
        private set

    /**
     * Whether this [BigNode] is removed or not.
     */
    var isRemoved = false
        private set

    /**
     * Whether the stack of the [BigNode] is cloned or not.
     */
    var isStackCloned = false
        private set

    /**
     * Whether the map of the [BigNode] is cloned or not.
     */
    var isHeapCloned = false
        private set

    /**
     * The number of levels in the whole stack.
     */
    val actualStackLevelSize get() = stackLevels.size

    /**
     * The number of elements in the whole stack.
     */
    val actualStackSize get() = stackLevels.asSequence().map { it.size }.count()

    /**
     * The number of cells in the whole heap.
     */
    val actualHeapSize get() = heap.size

    /**
     * The next free cell in the heap.
     */
    var nextFreeCell: Long = previousNode?.nextFreeCell ?: 0L
        private set

    // METHODS ----------------------------------------------------------------

    /**
     * Adds a new value into the stack by a name.
     */
    fun addToStack(name: String, value: LexemPrimitive) {
        cloneStack()

        // Get the last stack level.
        var level = stackLevels.lastOrNull() ?: let {
            val level = mutableMapOf<String, LexemPrimitive>()
            stackLevels.add(level)
            level
        }

        // Increase a level if name is inside.
        level = if (name in level) {
            val nextLevel = mutableMapOf<String, LexemPrimitive>()
            stackLevels.add(nextLevel)
            nextLevel
        } else {
            level
        }

        // Add the value to the stack.
        level[name] = value
    }

    /**
     * Gets the specified value of the stack.
     */
    fun getFromStack(name: String): LexemPrimitive {
        for (i in actualStackLevelSize - 1 downTo 0) {
            val level = stackLevels[i]
            val value = level[name]
            if (value != null) {
                return value
            }
        }

        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement,
                "Not found element called '$name' in the stack.") {}
    }

    /**
     * Removes the specified value of the stack recursively.
     */
    fun removeFromStack(name: String) {
        cloneStack()

        // Get the stack level.
        for (i in actualStackLevelSize - 1 downTo 0) {
            val level = stackLevels[i]
            val value = level[name]
            if (value != null) {
                // Remove cell.
                level.remove(name)

                // Remove last empty levels.
                if (level == stackLevels.last()) {
                    var lastLevel: MutableMap<String, LexemPrimitive>? = level
                    while (lastLevel != null && lastLevel.isEmpty()) {
                        stackLevels.removeAt(stackLevels.lastIndex)

                        lastLevel = stackLevels.lastOrNull()
                    }
                }

                return
            }
        }

        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement,
                "Not found element called '$name' in the stack.") {}
    }

    /**
     * Replace the specified stack cell by another primitive.
     */
    fun replaceStackCell(name: String, newValue: LexemPrimitive) {
        cloneStack()

        // Get the stack level.
        for (i in actualStackLevelSize - 1 downTo 0) {
            val level = stackLevels[i]
            val value = level[name]
            if (value != null) {
                // Replace cell.
                level[name] = newValue

                return
            }
        }

        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement,
                "Not found element called '$name' in the stack.") {}
    }

    /**
     * Gets a cell from the heap.
     */
    fun getCell(memory: LexemMemory, position: Long, toWrite: Boolean = false): LexemReferenced {
        if (position >= nextFreeCell) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                    "The analyzer is trying to access a forbidden memory position") {}
        }

        if (toWrite) {
            cloneHeap()
        }

        var res = heap[position] ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FreedMemoryAccess,
                "Cannot access to a freed memory cell") {}

        // Clone the value if it belongs to another bigNode.
        if (res.bigNode != this) {
            res = res.memoryShift(memory)
            heap[position] = res
        }

        return res
    }

    /**
     * Adds a new value to the heap.
     */
    fun alloc(value: LexemReferenced): Long {
        // Prevent errors regarding the BigNode link.
        if (value.bigNode != this) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapBigNodeLinkFault,
                    "The analyzer is trying to save a value in a different bigNode") {}
        }

        val index = nextFreeCell
        heap[index] = value
        nextFreeCell += 1

        return index
    }

    /**
     * Frees a memory cell to reuse it in the future.
     * Only called by the Garbage Collector.
     */
    fun free(position: Long) {
        cloneHeap()

        heap.remove(position)
    }

    /**
     * Called whenever the [BigNode] is recovered.
     */
    fun onRecover() {
        isCleaned.set(false)
    }

    /**
     * Clone the stack.
     */
    private fun cloneStack() {
        if (!isStackCloned) {
            stackLevels = stackLevels.map { it.toMutableMap() }.toMutableList()

            isStackCloned = true
        }
    }

    /**
     * Clone the heap.
     */
    private fun cloneHeap() {
        if (!isHeapCloned) {
            heap = ConcurrentHashMap(heap)

            isHeapCloned = true
        }
    }

    /**
     * Clears the [BigNode] destroying its cells to reuse them.
     */
    fun destroy() {
        // Clears the stack.
        if (isStackCloned) {
            stackLevels.clear()
        }

        // Destroys all cells to reuse them.
        if (isHeapCloned) {
            heap.clear()
        }

        isRemoved = true
        previousNode = null
        nextNode = null
    }

    /**
     * Collects all the garbage of the current [BigNode].
     */
    fun spatialGarbageCollect(memory: LexemMemory, forced: Boolean = false) {
        // Avoid to execute the garbage collector when the node is cleaned.
        if (!forced && !isCleaned.get()) {
            return
        }

        // Track from the main context and stack.
        val gcFifo = GarbageCollectorFifo(actualHeapSize)

        // Track the stdlib and hidden contexts.
        LxmReference.StdLibContext.spatialGarbageCollect(memory, gcFifo)
        LxmReference.HiddenContext.spatialGarbageCollect(memory, gcFifo)

        // Track the stack.
        for (i in actualStackLevelSize - 1 downTo 0) {
            val level = stackLevels[i]
            for ((_, value) in level) {
                if (isRemoved) {
                    return
                }

                value.spatialGarbageCollect(memory, gcFifo)
            }
        }

        // Track the heap.
        var position = gcFifo.pop()
        while (position != null) {
            if (isRemoved) {
                return
            }

            getCell(memory, position, false).spatialGarbageCollect(memory, gcFifo)

            position = gcFifo.pop()
        }

        // Clean memory.
        for (i in gcFifo) {
            if (isRemoved) {
                return
            }

            free(i)
        }

        isCleaned.set(true)

        // Clean next nodes.
        var node = nextNode
        while (node != null) {
            if (node.isRemoved) {
                return
            }

            if (node.isHeapCloned) {
                // Clean memory.
                for (i in gcFifo) {
                    free(i)
                }
            }

            node = nextNode
        }
    }
}
