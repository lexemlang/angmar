package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.bignode.*

/**
 * Interface for [LexemMemory] and [BigNode].
 */
internal interface IMemory {
    /**
     * Gets the id associated with the current [BigNode].
     */
    fun getBigNodeId(): Int

    /**
     * Adds a new primitive into the stack.
     */
    fun addToStack(name: String, primitive: LexemMemoryValue)

    /**
     * Adds a new primitive into the stack with '[AnalyzerCommons.Identifiers.Last]' as name.
     */
    fun addToStackAsLast(primitive: LexemMemoryValue)

    /**
     * Gets the specified primitive from the stack.
     */
    fun getFromStack(name: String): LexemPrimitive

    /**
     * Gets the '[AnalyzerCommons.Identifiers.Last]' primitive from the stack.
     */
    fun getLastFromStack(): LexemPrimitive

    /**
     * Removes the specified primitive from the stack.
     */
    fun removeFromStack(name: String)

    /**
     * Removes the '[AnalyzerCommons.Identifiers.Last]' primitive from the stack.
     */
    fun removeLastFromStack()

    /**
     * Renames the specified stack cell by another name.
     */
    fun renameStackCell(oldName: String, newName: String)

    /**
     * Renames the '[AnalyzerCommons.Identifiers.Last]' stack cell by another name.
     */
    fun renameLastStackCell(newName: String)

    /**
     * Renames the specified cell to '[AnalyzerCommons.Identifiers.Last]'.
     */
    fun renameStackCellToLast(oldName: String)

    /**
     * Replace the specified stack cell by another primitive.
     */
    fun replaceStackCell(name: String, newValue: LexemMemoryValue)

    /**
     * Replace the '[AnalyzerCommons.Identifiers.Last]' stack cell by another primitive.
     */
    fun replaceLastStackCell(newValue: LexemMemoryValue)

    /**
     * Gets a value from the memory.
     */
    fun get(reference: LxmReference, toWrite: Boolean): LexemReferenced

    /**
     * Gets a cell from the memory.
     */
    fun getCell(reference: LxmReference, toWrite: Boolean): BigNodeHeapCell

    /**
     * Adds a value in the memory returning the position in which it has been added.
     */
    fun add(value: LexemReferenced): LxmReference

    /**
     * Removes a value in the memory.
     */
    fun remove(reference: LxmReference)
}
