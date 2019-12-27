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
    private var isCellsCloned = false

    var isConstant = false
        private set
    var isWritable = true
        private set
    val size get() = cells.size

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory)

    private constructor(memory: LexemMemory, oldVersion: LxmList) : super(memory, oldVersion) {
        isConstant = oldVersion.isConstant
        isWritable = oldVersion.isWritable
        cells = oldVersion.cells
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of a cell.
     */
    fun getCell(memory: LexemMemory, index: Int) = cells.getOrNull(index)

    /**
     * Gets the final value of a cell.
     */
    inline fun <reified T : LexemMemoryValue> getDereferencedCell(memory: LexemMemory, index: Int,
            toWrite: Boolean): T? = getCell(memory, index)?.dereference(memory, toWrite) as? T

    /**
     * Sets a new value to a cell.
     */
    fun setCell(memory: LexemMemory, index: Int, value: LexemMemoryValue, ignoreConstant: Boolean = false) {
        if (index >= size) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cells.size} but the position '$index' was required") {}
        }

        replaceCell(memory, index, 1, value, ignoreConstant = ignoreConstant)
    }

    /**
     * Adds a new cell to the list.
     */
    fun addCell(memory: LexemMemory, vararg values: LexemMemoryValue, ignoreConstant: Boolean = false) =
            replaceCell(memory, size, 0, *values, ignoreConstant = ignoreConstant)

    /**
     * Replaces a set of cells removing a group and inserting another one.
     */
    fun replaceCell(memory: LexemMemory, index: Int, removeCount: Int = 0, vararg values2Add: LexemMemoryValue,
            ignoreConstant: Boolean = false) {
        // Prevent modifications if the list is constant.
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The list is immutable therefore cannot be modified") {}
        }

        if (!ignoreConstant && isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList,
                    "The list is constant therefore cannot be modified") {}
        }

        if (!isWritable) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyANonWritableList,
                    "The list is non writable therefore cannot be modified") {}
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
            cells[index + i] = values2Add[i].getPrimitive()
        }

        val removeCount = removeCount - countToReplace
        val index = index + countToReplace
        val values2AddSize = values2Add.size - countToReplace

        // At the end.
        if (index == size) {
            // Add rest.
            cells.addAll(values2Add.asSequence().drop(countToReplace).map { it.getPrimitive() })

            return
        }

        // Remove rest.
        if (values2AddSize == 0) {
            // Remove rest.
            if (removeCount != 0) {
                val realCount = minOf(removeCount, size - index)

                repeat(realCount) {
                    cells.removeAt(index)
                }
            }
        }
        // Add rest values.
        else if (removeCount == 0) {
            cells.addAll(index, values2Add.asSequence().drop(countToReplace).map { it.getPrimitive() }.toList())
        } else {
            throw AngmarUnreachableException()
        }
    }

    /**
     * Removes a cell.
     */
    fun removeCell(memory: LexemMemory, index: Int, count: Int = 1, ignoreConstant: Boolean = false) {
        if (index >= size) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cells.size} but the position '$index' was required") {}
        }

        replaceCell(memory, index, count, ignoreConstant = ignoreConstant)
    }

    /**
     * Inserts a set of cells at the specified position.
     */
    fun insertCell(memory: LexemMemory, index: Int, vararg values: LexemMemoryValue, ignoreConstant: Boolean = false) =
            replaceCell(memory, index, 0, *values, ignoreConstant = ignoreConstant)

    /**
     * Makes the list constant.
     */
    fun makeConstant(memory: LexemMemory) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The list is immutable therefore cannot be modified") {}
        }

        isConstant = true
    }

    /**
     * Makes the list constant and not writable.
     */
    fun makeConstantAndNotWritable(memory: LexemMemory) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The list is immutable therefore cannot be modified") {}
        }

        isConstant = true
        isWritable = false
    }

    /**
     * Gets all cells of the list in order.
     */
    fun getAllCells() = cells.toMutableList()

    /**
     * Clones the cell list.
     */
    private fun cloneCells() {
        if (!isCellsCloned) {
            cells = ArrayList(cells)
            isCellsCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryShift(memory: LexemMemory) = if (!isWritable) {
        this
    } else {
        LxmList(memory, oldVersion = this)
    }

    override fun memoryDealloc(memory: LexemMemory) {
    }

    override fun spatialGarbageCollect(memory: LexemMemory, gcFifo: GarbageCollectorFifo) {
        for (cell in cells) {
            cell.spatialGarbageCollect(memory, gcFifo)
        }
    }

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, ListType.TypeName) as LxmReference
    }

    override fun toLexemString(memory: LexemMemory) = LxmString.ListToString

    override fun toString() = StringBuilder().apply {
        if (isConstant) {
            append(ListNode.constantToken)
        }

        append(ListNode.startToken)

        val text = cells.asSequence().take(4).joinToString(", ")

        append(text)
        append(ListNode.endToken)

        if (size > 4) {
            append(" and ${size - 4} more")
        }
    }.toString()
}
