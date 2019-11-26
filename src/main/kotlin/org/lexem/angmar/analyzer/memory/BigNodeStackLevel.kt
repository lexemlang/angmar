package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.config.*
import java.util.*

/**
 * The representation of a stack level.
 */
internal class BigNodeStackLevel private constructor(var position: Int) {
    private val cells = mutableMapOf<String, LexemPrimitive>()

    /**
     * The number of cells in the level.
     */
    val cellCount get() = cells.size

    /**
     * A view of the level cells.
     */
    val cellValues
        get() = cells.toMap()

    // METHODS ----------------------------------------------------------------

    /**
     * Sets a value to a cell.
     */
    fun setCellValue(name: String, value: LexemPrimitive) {
        cells[name] = value
    }

    /**
     * Gets a cell value.
     */
    fun getCellValue(name: String) = cells[name]

    /**
     * Whether a cell exists in the current level.
     */
    fun hasCell(name: String) = name in cells

    /**
     * Removes a cell.
     */
    fun removeCell(name: String) {
        cells.remove(name)
    }

    /**
     * Destroys the cell to be reused.
     * MUST ONLY BE CALLED WHENEVER THE CELL IS REMOVED FROM MEMORY.
     */
    fun destroy() {
        cells.clear()

        if (instances.size < Consts.Memory.maxPoolSize) {
            instances.add(this)
        }
    }

    /**
     * Shifts the stack level to a forward [BigNode].
     */
    fun shiftLevel(): BigNodeStackLevel {
        val newLevel = new(position)

        for ((name, cell) in cells) {
            newLevel.cells[name] = cell
        }

        return newLevel
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "$position -> $cells"

    companion object {
        private val instances = Stack<BigNodeStackLevel>()

        /**
         * Creates a new [BigNodeStackLevel] for a specified position.
         */
        fun new(position: Int): BigNodeStackLevel {
            val instance = if (instances.size > 0) {
                instances.pop()!!
            } else {
                BigNodeStackLevel(position)
            }

            instance.position = position

            return instance
        }
    }
}

