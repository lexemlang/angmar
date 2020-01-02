package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.bignode.*
import org.lexem.angmar.errors.*
import java.util.concurrent.atomic.*

/**
 * A big node that represents an differential view of the memory.
 */
internal class BigNode constructor(var previousNode: BigNode?, var nextNode: BigNode?) : IMemory {
    val id = nextId.getAndIncrement()
    private val stack: BigNodeStack = previousNode?.stack?.clone(this) ?: BigNodeStack(this)
    private val heap: BigNodeHeap = previousNode?.heap?.clone(this) ?: BigNodeHeap(this)

    /**
     * The number of elements in the current [BigNode]'s stack.
     */
    val stackSize get() = stack.size

    /**
     * The number of cells in the current [BigNode]'s heap.
     */
    val heapSize get() = heap.cellCount.get()

    /**
     * The number of freed cells in the current [BigNode]'s heap.
     */
    var heapFreedCells: AtomicInteger = AtomicInteger(previousNode?.heapFreedCells?.get() ?: 0)
        private set

    /**
     * The position of the last empty cell that can be used to hold new information.
     * Used to avoid fragmentation.
     */
    var lastFreePosition: AtomicInteger = AtomicInteger(previousNode?.lastFreePosition?.get() ?: heapSize)
        private set

    /**
     * Whether this [BigNode] is in garbage collection mode or not.
     */
    var inGarbageCollectionMode = AtomicBoolean(false)

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
    fun getHeapCell(position: Int, toWrite: Boolean) = heap.getCell(position, toWrite)

    /**
     * Gets a cell value in the heap.
     */
    fun getHeapValue(position: Int, toWrite: Boolean) = heap.getCell(position, toWrite).getValue(toWrite)

    /**
     * Adds a new cell (or reuses a free one) to hold the specified value
     * returning the cell itself.
     */
    fun allocAndGetHeapCell(value: LexemReferenced): BigNodeHeapCell {
        // Prevent errors regarding the BigNode link.
        if (value.bigNodeId != id) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.HeapBigNodeLinkFault,
                    "The analyzer is trying to save a value in a different bigNode") {}
        }

        // No free cell.
        val lastFreePositionValue = lastFreePosition.get()
        if (lastFreePositionValue == heapSize) {
            val cell = BigNodeHeapCell(this, lastFreePositionValue, value)
            heap.setCell(cell)

            lastFreePosition.set(heapSize)
            return cell
        }

        // Reuse a free cell.
        val cell = getHeapCell(lastFreePositionValue, toWrite = true)
        lastFreePosition.set(cell.referenceCount.get())
        cell.reallocCell(value)

        heapFreedCells.decrementAndGet()
        return cell
    }

    /**
     * Frees a memory cell to reuse it in the future.
     */
    fun freeHeapCell(position: Int) {
        var cell = getHeapCell(position, toWrite = false)
        if (!cell.isFreed) {
            cell = getHeapCell(position, toWrite = true)
            cell.freeCell()
            lastFreePosition.set(cell.position)
            heapFreedCells.incrementAndGet()
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
    fun spatialGarbageCollect() {
        // Prevent do it twice.
        if (inGarbageCollectionMode.getAndSet(true)) {
            return
        }

        // Track from the main context and stack.
        val gcFifo = GarbageCollectorFifo(heapSize)

        // Track the stdlib and hidden contexts.
        LxmReference.StdLibContext.spatialGarbageCollect(gcFifo)
        LxmReference.HiddenContext.spatialGarbageCollect(gcFifo)

        // Track the stack.
        for (primitive in stack.gcIterator()) {
            primitive.spatialGarbageCollect(gcFifo)
        }

        // Track the heap.
        var position = gcFifo.pop()
        while (position != null) {
            getHeapCell(position, toWrite = false).getValue(toWrite = false)!!.spatialGarbageCollect(gcFifo)

            position = gcFifo.pop()
        }

        // Clean memory.
        for (i in gcFifo) {
            freeHeapCell(i)
        }

        inGarbageCollectionMode.set(false)
    }

    // OVERRIDDEN METHODS ------------------------------------------------------

    override fun getBigNodeId() = this.id

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

    override fun add(value: LexemReferenced) = LxmReference(allocAndGetHeapCell(value).position)

    override fun remove(reference: LxmReference) = freeHeapCell(reference.position)

    // STATIC -----------------------------------------------------------------

    companion object {
        private val nextId = AtomicInteger(0)
    }
}
