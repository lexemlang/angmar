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
internal class LxmList(val oldList: LxmList? = null) : LexemReferenced {
    private var isConstant = false
    var listSize: Int = oldList?.listSize ?: 0
        private set
    private var cellList = mutableMapOf<Int, LexemPrimitive>()
    val currentListSize get() = cellList.size

    /**
     * Gets the value of a cell.
     */
    fun getCell(memory: LexemMemory, index: Int): LexemPrimitive? = getCellRecursively(memory, index)

    /**
     * Gets the final value of a cell.
     */
    inline fun <reified T : LexemMemoryValue> getDereferencedCell(memory: LexemMemory, index: Int): T? =
            getCell(memory, index) as? T

    /**
     * Sets a new value to a cell.
     */
    fun setCell(memory: LexemMemory, index: Int, value: LexemPrimitive) {
        // Prevent modifications if the list is constant.
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList,
                    "The list is constant therefore cannot be modified") {}
        }

        if (index >= listSize) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cellList.size} but the position '$index' was required") {}
        }

        val currentCell = cellList[index]
        val lastCell = oldList?.getCellRecursively(memory, index)

        when {
            // Current cell
            currentCell != null -> {
                replaceCell(memory, index, value)
            }

            // Cell in past version of the list
            lastCell != null -> {
                cellList[index] = lastCell
                replaceCell(memory, index, value)
            }
        }
    }

    /**
     * Adds a new cell to the list.
     */
    fun addCell(memory: LexemMemory, vararg values: LexemPrimitive, ignoreConstant: Boolean = false) {
        // Prevent modifications if the list is constant.
        if (!ignoreConstant && isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList,
                    "The list is constant therefore cannot be modified") {}
        }

        for (value in values) {
            replaceCell(memory, listSize, value)
            listSize += 1
        }
    }

    /**
     * Removes a cell.
     */
    fun removeCell(memory: LexemMemory, index: Int, count: Int = 1, ignoreConstant: Boolean = false) {
        // Prevent modifications if the list is constant.
        if (!ignoreConstant && isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList,
                    "The list is constant therefore cannot be modified") {}
        }

        if (index >= listSize) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cellList.size} but the position '$index' was required") {}
        }

        val realCount = if (index + count > listSize) {
            listSize - index
        } else {
            count
        }

        // Move to the start.
        for (i in index + realCount until listSize) {
            val oldIndex = i - count
            val oldValue = getCellRecursively(memory, oldIndex)!!
            val newValue = getCellRecursively(memory, i)!!

            // Remove the cell and its value.
            cellList[oldIndex] = newValue
            memory.replacePrimitives(oldValue, newValue)
        }

        // Remove last count values.
        for (i in listSize - realCount until listSize) {
            val value = getCellRecursively(memory, i)!!
            memory.replacePrimitives(value, LxmNil)

            cellList.remove(i)
        }

        listSize -= count
    }

    /**
     * Inserts a set of cells at the specified position.
     */
    fun insertCell(memory: LexemMemory, index: Int, vararg values: LexemPrimitive, ignoreConstant: Boolean = false) {
        // Prevent modifications if the list is constant.
        if (!ignoreConstant && isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList,
                    "The list is constant therefore cannot be modified") {}
        }

        if (index > listSize) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                    "The list's length is ${cellList.size} but the position '$index' was required") {}
        }

        if (index == listSize) {
            addCell(memory, *values, ignoreConstant = ignoreConstant)
            return
        }

        // Move the cells to create space.
        val count = values.size
        for (i in listSize - 1 downTo index) {
            val value = getCellRecursively(memory, i)!!
            cellList[i + count] = value
        }

        // Add the new cells.
        for ((i, value) in values.withIndex()) {
            memory.replacePrimitives(LxmNil, value)
            cellList[index + i] = value
        }

        listSize += values.size
    }

    /**
     * Makes the object constant.
     */
    fun makeConstant(memory: LexemMemory) {
        isConstant = true
    }

    /**
     * Gets cell recursively.
     */
    private fun getCellRecursively(memory: LexemMemory, index: Int): LexemPrimitive? =
            cellList[index] ?: oldList?.getCellRecursively(memory, index)

    /**
     * Gets all cells of the list in order.
     */
    fun getAllCells(): List<LexemPrimitive> {
        val result = LinkedHashMap<Int, LexemPrimitive>(listSize)

        var currentList: LxmList? = this
        while (currentList != null) {
            currentList.cellList.filter { it.key < listSize }.forEach { (key, value) ->
                if (key !in result) {
                    result[key] = value
                }
            }
            currentList = currentList.oldList
        }

        return List(listSize) { result[it]!! }
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

    override val isImmutable: Boolean
        get() = isConstant

    override fun clone() = LxmList(this)

    override fun memoryDealloc(memory: LexemMemory) {
        for (i in getAllCells()) {
            if (i is LxmReference) {
                i.decreaseReferences(memory)
            }
        }

        cellList.clear()
        listSize = 0
    }

    override fun spatialGarbageCollect(memory: LexemMemory) {
        for (i in getAllCells()) {
            i.spatialGarbageCollect(memory)
        }
    }

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory)
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

    // STATIC -----------------------------------------------------------------

    companion object {
        val Empty = LxmList()

        init {
            Empty.isConstant = true
        }
    }
}
