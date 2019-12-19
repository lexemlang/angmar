package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*

/**
 * The Lexem value of the List type.
 */
internal class LxmList : LexemReferenced {
    var isConstant = false
        private set
    var isWritable = true
        private set
    var actualListSize = 0
        private set

    private var cellList: MutableMap<Int, LexemPrimitive>

    val listSize get() = cellList.size

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory) {
        cellList = mutableMapOf()
    }

    private constructor(memory: LexemMemory, oldVersion: LxmList, toClone: Boolean) : super(memory, oldVersion,
            toClone) {
        isConstant = oldVersion.isConstant
        isWritable = oldVersion.isWritable
        actualListSize = oldVersion.actualListSize

        cellList = if (toClone) {
            oldVersion.getAllCellsAsMap()
        } else {
            mutableMapOf()
        }
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of a cell.
     */
    fun getCell(memory: LexemMemory, index: Int): LexemPrimitive? = getCellRecursively(memory, index)

    /**
     * Gets the final value of a cell.
     */
    inline fun <reified T : LexemMemoryValue> getDereferencedCell(memory: LexemMemory, index: Int,
            toWrite: Boolean): T? = getCell(memory, index)?.dereference(memory, toWrite) as? T

    /**
     * Sets a new value to a cell.
     */
    fun setCell(memory: LexemMemory, index: Int, value: LexemMemoryValue) {
        // Prevent modifications if the list is constant.
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The list is immutable therefore cannot be modified") {}
        }

        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList,
                    "The list is constant therefore cannot be modified") {}
        }

        if (index >= actualListSize) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cellList.size} but the position '$index' was required") {}
        }

        val valuePrimitive = value.getPrimitive()
        val currentCell = cellList[index]
        val lastCell = (oldVersion as? LxmList)?.getCellRecursively(memory, index)

        when {
            // Current cell
            currentCell != null -> {
                replaceCell(memory, index, valuePrimitive)
            }

            // Cell in past version of the list
            lastCell != null -> {
                cellList[index] = lastCell
                replaceCell(memory, index, valuePrimitive)
            }
        }
    }

    /**
     * Adds a new cell to the list.
     */
    fun addCell(memory: LexemMemory, vararg values: LexemMemoryValue, ignoreConstant: Boolean = false) {
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

        for (value in values) {
            val valuePrimitive = value.getPrimitive()
            replaceCell(memory, actualListSize, valuePrimitive)
            actualListSize += 1
        }
    }

    /**
     * Removes a cell.
     */
    fun removeCell(memory: LexemMemory, index: Int, count: Int = 1, ignoreConstant: Boolean = false) {
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

        if (index >= actualListSize) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cellList.size} but the position '$index' was required") {}
        }

        val realCount = if (index + count > actualListSize) {
            actualListSize - index
        } else {
            count
        }

        // Move to the start.
        for (i in index + realCount until actualListSize) {
            val oldIndex = i - count
            val oldValue = getCellRecursively(memory, oldIndex)!!
            val newValue = getCellRecursively(memory, i)!!

            // Remove the cell and its value.
            cellList[oldIndex] = newValue
            memory.replacePrimitives(oldValue, newValue)
        }

        // Remove last count values.
        for (i in actualListSize - realCount until actualListSize) {
            val value = getCellRecursively(memory, i)!!
            memory.replacePrimitives(value, LxmNil)

            cellList.remove(i)
        }

        actualListSize -= realCount
    }

    /**
     * Inserts a set of cells at the specified position.
     */
    fun insertCell(memory: LexemMemory, index: Int, vararg values: LexemMemoryValue, ignoreConstant: Boolean = false) {
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

        if (index > actualListSize) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cellList.size} but the position '$index' was required") {}
        }

        if (index == actualListSize) {
            addCell(memory, *values, ignoreConstant = ignoreConstant)
            return
        }

        // Move the cells to create space.
        val count = values.size
        for (i in actualListSize - 1 downTo index) {
            val value = getCellRecursively(memory, i)!!
            cellList[i + count] = value
        }

        // Add the new cells.
        for ((i, value) in values.withIndex()) {
            val valuePrimitive = value.getPrimitive()
            memory.replacePrimitives(LxmNil, valuePrimitive)
            cellList[index + i] = valuePrimitive
        }

        actualListSize += values.size
    }

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
     * Gets cell recursively.
     */
    private fun getCellRecursively(memory: LexemMemory, index: Int): LexemPrimitive? {
        var list: LxmList? = this
        while (list != null) {
            val value = list.cellList[index]
            if (value != null) {
                return value
            }

            list = list.oldVersion as? LxmList
        }

        return null
    }


    /**
     * Gets all cells of the list in order.
     */
    fun getAllCellsAsMap(): MutableMap<Int, LexemPrimitive> {
        val versions = getListOfVersions<LxmList>()

        // Iterate to get a list of versions.
        val result = HashMap<Int, LexemPrimitive>(actualListSize)

        while (versions.isNotEmpty()) {
            val element = versions.removeLast()
            result.putAll(element.cellList)
        }

        return result
    }

    /**
     * Gets all cells of the list in order.
     */
    fun getAllCells(): List<LexemPrimitive> {
        val result = getAllCellsAsMap()
        return List(actualListSize) { result[it]!! }
    }

    /**
     * Replaces the value of a cell.
     */
    private fun replaceCell(memory: LexemMemory, index: Int, newValue: LexemPrimitive) {
        // Keep this to replace the elements before possibly remove the references.
        val oldValue = cellList[index] ?: LxmNil
        cellList[index] = newValue
        memory.replacePrimitives(oldValue, newValue)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryShift(memory: LexemMemory) = if (!isWritable) {
        this
    } else {
        LxmList(memory, this, toClone = countOldVersions() >= Consts.Memory.maxVersionCountToFullyCopyAValue)
    }

    override fun memoryDealloc(memory: LexemMemory) {
        for (i in getAllCells()) {
            if (i is LxmReference) {
                i.decreaseReferences(memory)
            }
        }

        if (isWritable) {
            cellList.clear()
            actualListSize = 0
        }
    }

    override fun spatialGarbageCollect(memory: LexemMemory, gcFifo: GarbageCollectorFifo) {
        for (cell in getAllCells()) {
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

        val list = getAllCells()
        val text = list.asSequence().take(4).joinToString(", ")

        append(text)
        append(ListNode.endToken)

        if (list.size > 4) {
            append(" and ${list.size - 4} more")
        }
    }.toString()
}
