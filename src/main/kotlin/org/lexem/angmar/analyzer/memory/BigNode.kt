package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.bignode.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import java.util.concurrent.atomic.*

/**
 * A big node that represents an differential view of the memory.
 */
internal class BigNode constructor(val id: Int, var previousNode: BigNode? = null) : IMemory {
    var nextNode: BigNode? = null
    private val stack: BigNodeStack = previousNode?.stack?.clone(this) ?: BigNodeStack(this)
    private val heap: BigNodeHeap = previousNode?.heap ?: BigNodeHeap()

    /**
     * The number of elements in the current [BigNode]'s stack.
     */
    val stackSize get() = stack.size

    /**
     * The number of cells in the current [BigNode]'s heap.
     */
    var heapSize: Int = previousNode?.heapSize ?: 0
        private set

    /**
     * The number of freed cells in the current [BigNode]'s heap.
     */
    var heapFreedCells: Int = previousNode?.heapFreedCells ?: 0
        private set

    /**
     * The position of the last empty cell that can be used to hold new information.
     * Used to avoid fragmentation.
     */
    var lastFreePosition: AtomicInteger = AtomicInteger(previousNode?.lastFreePosition?.get() ?: heapSize)

    /**
     * The minimum value of the heap to call the garbage collector in synchronous mode.
     */
    var garbageCollectorThreshold: Int =
            previousNode?.garbageCollectorThreshold ?: Consts.Memory.garbageCollectorInitialThreshold
        private set

    /**
     * Whether this [BigNode] should start the garbage collector process synchronously.
     */
    val startGarbageCollectorSync get() = heapSize >= garbageCollectorThreshold

    /**
     * Whether this [BigNode] is in garbage collection mode or not.
     */
    var inGarbageCollectionMode = false

    /**
     * The rollback code point where to continue the analysis when this [BigNode] is recovered.
     */
    var rollbackCodePoint: LxmRollbackCodePoint? = null

    // METHODS ----------------------------------------------------------------

    /**
     * Adds a new value into the stack by a name.
     */
    fun addToStack(name: String, value: LexemPrimitive) = stack.addCell(name, value)

    /**
     * Replace the specified stack cell by another primitive.
     */
    fun replaceStackCell(name: String, newValue: LexemPrimitive) = stack.replaceCell(name, newValue)

    /**
     * Gets a cell in the heap.
     */
    fun getHeapCell(position: Int, toWrite: Boolean) = heap.getCell(this, position, toWrite)

    /**
     * Gets a cell value in the heap.
     */
    fun getHeapValue(position: Int, toWrite: Boolean) = heap.getCell(this, position, toWrite).getValue(this, toWrite)

    /**
     * Adds a new cell (or reuses a free one) to hold the specified value
     * returning the cell itself.
     */
    fun allocAndGetHeapCell(value: LexemReferenced): Pair<Int, BigNodeHeapCell> {
        // Prevent errors regarding the BigNode link.
        if (value.bigNodeId != id) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapBigNodeLinkFault,
                    "The analyzer is trying to save a value in a different bigNode") {}
        }

        var realloc = false
        val cellPosition = lastFreePosition.getAndUpdate { oldLastFreePosition ->
            if (oldLastFreePosition == heapSize) {
                // Add new cell.
                realloc = false

                heapSize + 1
            } else {
                // Reuse a free cell.
                realloc = true

                val cell = getHeapCell(oldLastFreePosition, toWrite = true)
                cell.nextRemovedCell
            }
        }

        val cell = if (realloc) {
            // Reuse a free cell.
            val cell = getHeapCell(cellPosition, toWrite = true)
            cell.reallocCell(value)

            heapFreedCells -= 1
            cell
        } else {
            // Add new cell.
            val cell = BigNodeHeapCell(id, value)
            heap.setCell(cellPosition, cell)

            heapSize += 1
            cell
        }

        return Pair(cellPosition, cell)
    }

    /**
     * Frees a memory cell to reuse it in the future.
     */
    fun freeHeapCell(position: Int) {
        var cell = getHeapCell(position, toWrite = false)
        if (!cell.isFreed) {
            cell = getHeapCell(position, toWrite = true)
            cell.freeCell(position, this)
            heapFreedCells += 1
        }
    }

    /**
     * Clears this and next [BigNode]s.
     */
    fun destroy() {
        previousNode = null
        nextNode?.destroy()
        nextNode = null
    }

    /**
     * Collects all the garbage of the current big node.
     */
    fun garbageCollect() {
        inGarbageCollectionMode = true

        val inSyncMode = startGarbageCollectorSync

        // Track from the main context and stack.
        val gcFifo = GarbageCollectorFifo(heapSize)

        // Track the stdlib and hidden contexts.
        LxmReference.StdLibContext.spatialGarbageCollect(this, gcFifo)
        LxmReference.HiddenContext.spatialGarbageCollect(this, gcFifo)

        // Track the stack.
        for (primitive in stack.gcIterator()) {
            primitive.spatialGarbageCollect(this, gcFifo)
        }

        // Track the heap.
        var position = gcFifo.pop()
        while (position != null) {
            val cell = getHeapCell(position, toWrite = false).getValue(this, toWrite = false)
                    ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault,
                            "Cannot access to the cell [$position] during garbage collection") {}
            cell.spatialGarbageCollect(this, gcFifo)

            position = gcFifo.pop()
        }

        // Clean memory.
        if (inSyncMode) {
            // Remove exceed cells only if it is executed in sync mode.
            for (i in heapSize until heap.cellCount) {
                heap.removeCell(i)
            }
        }

        for (i in gcFifo) {
            freeHeapCell(i)
        }

        // Recalculate if in sync mode.
        if (inSyncMode) {
            val freeRatio = heapFreedCells / heapSize.toDouble()
            if (freeRatio < Consts.Memory.garbageCollectorThresholdFreeRatioToIncrease) {
                garbageCollectorThreshold *= Consts.Memory.garbageCollectorThresholdIncreaseFactor
            }
        }

        inGarbageCollectionMode = false
    }

    // TODO remove
    fun bigNodeSequence(): Sequence<BigNode> {
        val bn = this
        return sequence {
            var node: BigNode? = bn
            while (node != null) {
                yield(node!!)
                node = node.previousNode
            }
        }
    }

    // TODO remove
    fun bigNodeList() = bigNodeSequence().toList()


    // OVERRIDDEN METHODS ------------------------------------------------------

    override fun getBigNodeId() = id

    override fun addToStack(name: String, primitive: LexemMemoryValue) = addToStack(name, primitive.getPrimitive())

    override fun addToStackAsLast(primitive: LexemMemoryValue) = addToStack(AnalyzerCommons.Identifiers.Last, primitive)

    override fun getFromStack(name: String) = stack.getCell(name)

    override fun getLastFromStack() = getFromStack(AnalyzerCommons.Identifiers.Last)

    override fun removeFromStack(name: String) = stack.removeCell(name)

    override fun removeLastFromStack() = removeFromStack(AnalyzerCommons.Identifiers.Last)

    override fun renameStackCell(oldName: String, newName: String) {
        if (oldName == newName) {
            return
        }

        val currentCell = getFromStack(oldName)
        addToStack(newName, currentCell)
        removeFromStack(oldName)
    }

    override fun renameLastStackCell(newName: String) = renameStackCell(AnalyzerCommons.Identifiers.Last, newName)

    override fun renameStackCellToLast(oldName: String) = renameStackCell(oldName, AnalyzerCommons.Identifiers.Last)

    override fun replaceStackCell(name: String, newValue: LexemMemoryValue) =
            replaceStackCell(name, newValue.getPrimitive())

    override fun replaceLastStackCell(newValue: LexemMemoryValue) =
            replaceStackCell(AnalyzerCommons.Identifiers.Last, newValue)

    override fun get(reference: LxmReference, toWrite: Boolean) = getHeapValue(reference.position, toWrite)!!

    override fun getCell(reference: LxmReference, toWrite: Boolean) = getHeapCell(reference.position, toWrite)

    override fun add(value: LexemReferenced) = LxmReference(allocAndGetHeapCell(value).first)

    override fun remove(reference: LxmReference) = freeHeapCell(reference.position)

    override fun toString() = "[$id]"
}
