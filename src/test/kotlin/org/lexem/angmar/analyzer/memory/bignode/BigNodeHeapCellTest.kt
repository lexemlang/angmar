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
        fun checkCell(cell: BigNodeHeapCell, position: Int, value: LexemReferenced?, referenceCount: Int = 0,
                isFreed: Boolean = false) {
            Assertions.assertEquals(position, cell.position, "The position property is incorrect")
            Assertions.assertEquals(value, cell.getValue(toWrite = false), "The value property is incorrect")
            Assertions.assertEquals(referenceCount, cell.referenceCount.get(),
                    "The referenceCount property is incorrect")
            Assertions.assertEquals(isFreed, cell.isFreed, "The isFreed property is incorrect")
        }
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `test get`() {
        val memory = LexemMemory()
        val emptyList = LxmList(memory)
        val cell = BigNodeHeapCell(memory.lastNode, 0, emptyList)

        checkCell(cell, 0, emptyList)
    }

    @Test
    fun `test set and free by reference count`() {
        val memory = LexemMemory()
        val emptyList = LxmList(memory)
        val cell0 = emptyList.getPrimitive().getCell(memory, toWrite = true)

        checkCell(cell0, 0, emptyList)

        // Reduce the reference count.
        cell0.increaseReferences()
        cell0.decreaseReferences()
        checkCell(cell0, 0, null, referenceCount = 1, isFreed = true) // 1 = lastFreeCell
    }

    @Test
    fun `test clone`() {
        val memory = LexemMemory()

        val cell0Value = LxmList(memory)
        val cell0 = cell0Value.getPrimitive().getCell(memory, toWrite = true)
        cell0.increaseReferences()

        checkCell(cell0, 0, cell0Value, referenceCount = 1)

        TestUtils.freezeCopy(memory)

        val clonedCell = cell0.clone(memory.lastNode)

        checkCell(cell0, 0, cell0Value, referenceCount = 1)
        checkCell(clonedCell, 0, clonedCell.getValue(toWrite = false), referenceCount = 1)

        Assertions.assertEquals(cell0Value, clonedCell.getValue(toWrite = false),
                "The object has been incorrectly cloned")
    }

    @Test
    fun `test realloc`() {
        val memory = LexemMemory()
        val cell0Value = LxmObject(memory)

        val cell0 = cell0Value.getPrimitive().getCell(memory, toWrite = true)

        checkCell(cell0, 0, cell0Value)

        // Free cell.
        cell0.freeCell()

        // Realloc
        cell0.reallocCell(cell0Value)

        checkCell(cell0, 0, cell0Value)
    }

    @Test
    fun `test free with value`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val emptyObject = LxmObject(memory)
        val cell0 = emptyObject.getPrimitive().getCell(memory, toWrite = true)

        checkCell(cell0, 0, emptyObject)

        // Free
        cell0.freeCell()

        checkCell(cell0, 0, null, referenceCount = bigNode.heapSize, isFreed = true)
    }

    @Test
    @Incorrect
    fun `test free a freed cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            val cell0 = bigNode.allocAndGetHeapCell(empty)
            cell0.increaseReferences()

            // Free
            cell0.decreaseReferences()

            cell0.freeCell()
        }
    }

    @Test
    @Incorrect
    fun `test increase references of a freed cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            val cell0 = bigNode.allocAndGetHeapCell(empty)
            cell0.increaseReferences()

            // Free
            cell0.decreaseReferences()

            cell0.increaseReferences()
        }
    }

    @Test
    @Incorrect
    fun `test decrease references of a freed cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            val cell0 = bigNode.allocAndGetHeapCell(empty)
            cell0.increaseReferences()

            // Free
            cell0.decreaseReferences()

            cell0.decreaseReferences()
        }
    }

    @Test
    @Incorrect
    fun `test free a referenced cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotFreeAReferencedHeapCell) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            val cell0 = bigNode.allocAndGetHeapCell(empty)
            cell0.increaseReferences()

            // Free
            cell0.freeCell()
        }
    }

    @Test
    @Incorrect
    fun `test decrease a zero-reference cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.ReferenceCountUnderflow) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            val cell0 = bigNode.allocAndGetHeapCell(empty)
            cell0.decreaseReferences()
        }
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
