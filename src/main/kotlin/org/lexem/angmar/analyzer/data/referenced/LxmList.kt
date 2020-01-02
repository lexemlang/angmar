package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*

/**
 * The Lexem value of the List type.
 */
internal class LxmList : LexemReferenced {
    private var cells = mutableListOf<LexemPrimitive>()
    private var isCellsCloned = true

    var isConstant = false
        private set
    var isWritable = true
        private set
    val size get() = cells.size

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: IMemory) : super(memory)

    private constructor(memory: IMemory, oldVersion: LxmList) : super(memory, oldVersion) {
        isConstant = oldVersion.isConstant
        isWritable = oldVersion.isWritable
        cells = oldVersion.cells
        isCellsCloned = false
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of a cell.
     */
    fun getCell(index: Int) = cells.getOrNull(index)

    /**
     * Gets the final value of a cell.
     */
    inline fun <reified T : LexemMemoryValue> getDereferencedCell(memory: IMemory, index: Int, toWrite: Boolean): T? =
            getCell(index)?.dereference(memory, toWrite) as? T

    /**
     * Sets a new value to a cell.
     */
    fun setCell(memory: IMemory, index: Int, value: LexemMemoryValue, ignoreConstant: Boolean = false) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The list is immutable therefore cannot be modified") {}
        }

        if (index >= size) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cells.size} but the position '$index' was required") {}
        }

        replaceCell(memory, index, 1, value, ignoreConstant = ignoreConstant)
    }

    /**
     * Adds a new cell to the list.
     */
    fun addCell(memory: IMemory, vararg values: LexemMemoryValue, ignoreConstant: Boolean = false) =
            replaceCell(memory, size, 0, *values, ignoreConstant = ignoreConstant)

    /**
     * Replaces a set of cells removing a group and inserting another one.
     */
    fun replaceCell(memory: IMemory, index: Int, removeCount: Int = 0, vararg values2Add: LexemMemoryValue,
            ignoreConstant: Boolean = false) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The list is immutable therefore cannot be modified") {}
        }

        if (!ignoreConstant && isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList,
                    "The list is constant therefore cannot be modified") {}
        }

        if (index > size) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cells.size} but the position '$index' was required") {}
        }

        if (values2Add.isEmpty() && removeCount == 0) {
            return
        }

        cloneCells()

        val countToReplace = minOf(removeCount, values2Add.size, size - index)

        // Replace cells.
        for (i in 0 until countToReplace) {
            replaceCellValue(memory, index + i, values2Add[i].getPrimitive())
        }

        val removeCount = removeCount - countToReplace
        val index = index + countToReplace
        val values2AddSize = values2Add.size - countToReplace

        // At the end.
        if (index == size) {
            // Add rest.
            cells.addAll(values2Add.asSequence().drop(countToReplace).map {
                val res = it.getPrimitive()
                res.increaseReferences(memory)
                res
            })

            return
        }

        // Remove rest.
        if (values2AddSize == 0) {
            // Remove rest.
            if (removeCount != 0) {
                val realCount = minOf(removeCount, size - index)
                val subList = cells.subList(index, index + realCount)

                subList.forEach {
                    it.decreaseReferences(memory)
                }

                subList.clear()
            }
        }
        // Add rest values.
        else if (removeCount == 0) {
            cells.addAll(index, values2Add.asSequence().drop(countToReplace).map {
                val res = it.getPrimitive()
                res.increaseReferences(memory)
                res
            }.toList())
        } else {
            throw AngmarUnreachableException()
        }
    }

    /**
     * Removes a cell.
     */
    fun removeCell(memory: IMemory, index: Int, count: Int = 1, ignoreConstant: Boolean = false) {
        if (index >= size) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cells.size} but the position '$index' was required") {}
        }

        replaceCell(memory, index, count, ignoreConstant = ignoreConstant)
    }

    /**
     * Inserts a set of cells at the specified position.
     */
    fun insertCell(memory: IMemory, index: Int, vararg values: LexemMemoryValue, ignoreConstant: Boolean = false) =
            replaceCell(memory, index, 0, *values, ignoreConstant = ignoreConstant)

    /**
     * Makes the list constant.
     */
    fun makeConstant(memory: IMemory) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The list is immutable therefore cannot be modified") {}
        }

        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList,
                    "The list is constant therefore cannot be modified") {}
        }

        isConstant = true
    }

    /**
     * Makes the list constant and not writable.
     */
    fun makeConstantAndNotWritable(memory: IMemory) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The list is immutable therefore cannot be modified") {}
        }

        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList,
                    "The list is constant therefore cannot be modified") {}
        }

        isConstant = true
        isWritable = false
    }

    /**
     * Gets all cells of the list in order.
     */
    fun getAllCells() = cells.asSequence()

    /**
     * Replaces the value of a cell.
     */
    private fun replaceCellValue(memory: IMemory, index: Int, newValue: LexemPrimitive) {
        // Keep this to replace the elements before possibly remove the references.
        val oldValue = cells.getOrNull(index) ?: LxmNil
        cells[index] = newValue
        MemoryUtils.replacePrimitives(memory, oldValue, newValue)
    }

    /**
     * Clones the cell list.
     */
    private fun cloneCells() {
        if (!isCellsCloned) {
            cells = cells.toMutableList()
            isCellsCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(memory: IMemory) = LxmList(memory, this)

    override fun memoryDealloc(memory: IMemory) {
        getAllCells().forEach { it.decreaseReferences(memory) }
    }

    override fun spatialGarbageCollect(gcFifo: GarbageCollectorFifo) {
        getAllCells().forEach { it.spatialGarbageCollect(gcFifo) }
    }

    override fun getType(memory: IMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, ListType.TypeName) as LxmReference
    }

    override fun toLexemString(memory: IMemory) = LxmString.ListToString

    override fun toString() = StringBuilder().apply {
        if (isConstant) {
            append(ListNode.constantToken)
        }

        append(ListNode.startToken)

        val text = getAllCells().take(4).joinToString(", ")

        append(text)
        append(ListNode.endToken)

        if (size > 4) {
            append(" and ${size - 4} more")
        }
    }.toString()
}
