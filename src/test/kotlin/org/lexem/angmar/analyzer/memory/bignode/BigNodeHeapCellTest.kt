package org.lexem.angmar.analyzer.memory.bignode

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

internal class BigNodeHeapCellTest {
    companion object {

        // AUX METHODS --------------------------------------------------------

        // Checks the status of a Cell.
        fun checkCell(bigNode: BigNode, cell: BigNodeHeapCell, value: LexemReferenced?, nextRemoveCell: Int = -1,
                isFreed: Boolean = false) {
            Assertions.assertEquals(value, cell.getValue(bigNode, toWrite = false), "The value property is incorrect")
            Assertions.assertEquals(nextRemoveCell, cell.nextRemovedCell, "The nextRemoveCell property is incorrect")
            Assertions.assertEquals(isFreed, cell.isFreed, "The isFreed property is incorrect")
        }
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `test get`() {
        val memory = LexemMemory()
        val emptyList = LxmList(memory)
        val cell = BigNodeHeapCell(memory.lastNode.id, emptyList)

        checkCell(memory.lastNode, cell, emptyList)
    }

    @Test
    fun `test clone`() {
        val memory = LexemMemory()

        val cell0Value = LxmList(memory)
        val cell0 = cell0Value.getPrimitive().getCell(memory, toWrite = true)

        checkCell(memory.lastNode, cell0, cell0Value)

        TestUtils.freezeCopy(memory)

        val clonedCell = cell0.clone(memory.lastNode.id)

        checkCell(memory.lastNode, cell0, cell0Value)
        checkCell(memory.lastNode, clonedCell, clonedCell.getValue(memory.lastNode, toWrite = false))

        Assertions.assertEquals(cell0Value, clonedCell.getValue(memory.lastNode, toWrite = false),
                "The object has been incorrectly cloned")
    }

    @Test
    fun `test realloc`() {
        val memory = LexemMemory()
        val cell0Value = LxmObject(memory)

        val cell0 = cell0Value.getPrimitive().getCell(memory, toWrite = true)

        checkCell(memory.lastNode, cell0, cell0Value)

        // Free cell.
        cell0.freeCell(0, memory.lastNode)

        // Realloc
        cell0.reallocCell(cell0Value)

        checkCell(memory.lastNode, cell0, cell0Value)
    }

    @Test
    fun `test free with value`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val emptyObject = LxmObject(memory)
        val cell0 = emptyObject.getPrimitive().getCell(memory, toWrite = true)

        checkCell(memory.lastNode, cell0, emptyObject)

        // Free
        cell0.freeCell(0, bigNode)

        checkCell(memory.lastNode, cell0, null, nextRemoveCell = bigNode.heapSize, isFreed = true)
    }

    @Test
    @Incorrect
    fun `test realloc a used cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val cell0Value = LxmObject(memory)
            val cell0 = cell0Value.getPrimitive().getCell(bigNode, toWrite = false)

            cell0.reallocCell(cell0Value)
        }
    }
}
