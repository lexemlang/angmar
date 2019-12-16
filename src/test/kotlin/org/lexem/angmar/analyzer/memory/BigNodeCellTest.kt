package org.lexem.angmar.analyzer.memory

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

internal class BigNodeCellTest {
    companion object {

        // AUX METHODS --------------------------------------------------------

        // Checks the status of a Cell.
        fun checkCell(cell: BigNodeCell, position: Int, value: LexemReferenced?, referenceCount: Int = 0,
                isFreed: Boolean = false) {
            Assertions.assertEquals(position, cell.position, "The position property is incorrect")
            Assertions.assertEquals(value, cell.value, "The value property is incorrect")
            Assertions.assertEquals(referenceCount, cell.referenceCount, "The referenceCount property is incorrect")
            Assertions.assertEquals(isFreed, cell.isFreed, "The isFreed property is incorrect")
            Assertions.assertFalse(cell.isNotGarbage, "The isNotGarbage property is incorrect")
        }
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `test new`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val emptyObject = LxmObject(memory)
        val emptyList = LxmList(memory)
        val cell = BigNodeCell.new(0, emptyObject)

        checkCell(cell, 0, emptyObject)

        // Destroys the cell
        cell.destroy()
        checkCell(cell, -1, null, isFreed = true)

        // Check whether the new cell is the same as before to check the reuse of them.
        val newCell = BigNodeCell.new(56, emptyList)

        Assertions.assertEquals(cell, newCell, "The cells are not equals")
        checkCell(newCell, 56, emptyList)
        checkCell(cell, 56, emptyList)
    }

    @Test
    fun `test get`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val emptyList = LxmList(memory)
        val cell = BigNodeCell.new(0, emptyList)

        checkCell(cell, 0, emptyList)
    }

    @Test
    fun `test set simple`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val emptyObject = LxmObject(memory)
        val emptyList = LxmList(memory)
        val cell0 = bigNode.alloc(memory, emptyObject)

        checkCell(cell0, 0, emptyObject)

        cell0.setValue(emptyList)

        checkCell(cell0, 0, emptyList)
    }

    @Test
    fun `test set and free by reference count`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val emptyList = LxmList(memory)
        val cell0 = bigNode.alloc(memory, emptyList)

        checkCell(cell0, 0, emptyList)

        // Reduce the reference count.
        cell0.increaseReferences()
        cell0.decreaseReferences(memory)
        checkCell(cell0, 0, null, referenceCount = 1, isFreed = true) // 1 = lastFreeCell
    }

    @Test
    fun `test shift`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        val cell0Value = LxmList(memory)
        val cell0 = bigNode.alloc(memory, cell0Value)
        cell0.increaseReferences()

        checkCell(cell0, 0, cell0Value, referenceCount = 1)

        val shiftedCell = cell0.shiftCell(memory)

        checkCell(cell0, 0, cell0Value, referenceCount = 1)
        checkCell(shiftedCell, 0, shiftedCell.value, referenceCount = 1)

        Assertions.assertEquals(cell0Value, (shiftedCell.value as LxmList).oldVersion,
                "The object has been incorrectly cloned")
    }

    @Test
    fun `test realloc`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val emptyObject = LxmObject(memory)
        val emptyList = LxmList(memory)

        val cell0 = bigNode.alloc(memory, emptyObject)

        val obj = LxmObject(memory)
        obj.setProperty(memory, "test", LxmReference(cell0.position))

        val cell1 = bigNode.alloc(memory, obj)
        cell1.increaseReferences()

        checkCell(cell0, 0, emptyObject, referenceCount = 1)
        checkCell(cell1, 1, obj, referenceCount = 1)

        // Realloc
        cell1.reallocCell(memory, emptyList)

        checkCell(cell0, 0, null, referenceCount = 2, isFreed = true) // 2 = lastFreeCell
        checkCell(cell1, 1, emptyList)
    }

    @Test
    fun `test free with value`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val emptyObject = LxmObject(memory)
        val emptyList = LxmList(memory)
        bigNode.alloc(memory, emptyObject)
        val cell1 = bigNode.alloc(memory, emptyList)

        checkCell(cell1, 1, emptyList)

        // Free
        cell1.freeCell(memory)

        checkCell(cell1, 1, null, referenceCount = bigNode.actualHeapSize, isFreed = true)
    }

    @Test
    fun `test destroy`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val empty = LxmObject(memory)
        val cell = bigNode.alloc(memory, empty)

        // Add reference to increase the count.
        cell.increaseReferences()

        checkCell(cell, 0, empty, referenceCount = 1)

        // Destroys the cell
        cell.destroy()
        checkCell(cell, -1, null, isFreed = true)
    }

    @Test
    @Incorrect
    fun `test set a freed cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            val cell0 = bigNode.alloc(memory, empty)
            cell0.increaseReferences()

            // Free
            cell0.decreaseReferences(memory)

            cell0.setValue(empty)
        }
    }

    @Test
    @Incorrect
    fun `test free a freed cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            val cell0 = bigNode.alloc(memory, empty)
            cell0.increaseReferences()

            // Free
            cell0.decreaseReferences(memory)

            cell0.freeCell(memory)
        }
    }

    @Test
    @Incorrect
    fun `test increase references of a freed cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            val cell0 = bigNode.alloc(memory, empty)
            cell0.increaseReferences()

            // Free
            cell0.decreaseReferences(memory)

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
            val cell0 = bigNode.alloc(memory, empty)
            cell0.increaseReferences()

            // Free
            cell0.decreaseReferences(memory)

            cell0.decreaseReferences(memory)
        }
    }

    @Test
    @Incorrect
    fun `test free a referenced cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.ReferencedHeapCellFreed) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            val cell0 = bigNode.alloc(memory, empty)
            cell0.increaseReferences()

            // Free
            cell0.freeCell(memory)
        }
    }

    @Test
    @Incorrect
    fun `test decrease a zero-reference cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.ReferenceCountUnderflow) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            val cell0 = bigNode.alloc(memory, empty)
            cell0.decreaseReferences(memory)
        }
    }
}
