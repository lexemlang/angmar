package org.lexem.angmar.analyzer.memory

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.bignode.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

internal class BigNodeTest {
    companion object {
        private const val size = 4

        // AUX METHODS --------------------------------------------------------

        // Checks the status of a BigNode.
        private fun checkBigNode(bigNode: BigNode, prevNode: BigNode? = null, nextNode: BigNode? = null,
                stackSize: Int = 0, heapSize: Int = 0, heapFreedCells: Int? = null, lastFreePosition: Int? = null) {
            Assertions.assertEquals(stackSize, bigNode.stackSize, "The stackSize property is incorrect")
            Assertions.assertEquals(heapSize, bigNode.heapSize, "The heapSize property is incorrect")
            Assertions.assertEquals(heapFreedCells ?: 0, bigNode.heapFreedCells,
                    "The heapFreedCells property is incorrect")
            Assertions.assertEquals(lastFreePosition ?: heapSize, bigNode.lastFreePosition.get(),
                    "The lastFreePosition property is incorrect")

            Assertions.assertEquals(prevNode, bigNode.previousNode, "The previousNode is incorrect")
            Assertions.assertEquals(nextNode, bigNode.nextNode, "The nextNode is incorrect")
        }
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `test stack with same names`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size) { LxmString.from(it.toString()) }
        val stackName = "last"

        // Add some.
        for ((i, obj) in objects.withIndex()) {
            bigNode.addToStack(stackName, obj)
            checkBigNode(bigNode, stackSize = i + 1)
        }

        // Get and remove some.
        for ((i, obj) in objects.withIndex().reversed()) {
            val stackObj = bigNode.getFromStack(stackName)

            Assertions.assertEquals(obj, stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNode, stackSize = i + 1)

            bigNode.removeFromStack(stackName)

            checkBigNode(bigNode, stackSize = i)
        }
    }

    @Test
    fun `test stack with different names`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size) { LxmString.from(it.toString()) }
        val stackName = "last"

        // Add some.
        for ((i, obj) in objects.withIndex()) {
            bigNode.addToStack(stackName + i, obj)
            checkBigNode(bigNode, stackSize = i + 1)
        }

        // Get and remove some.
        for ((i, obj) in objects.withIndex().reversed()) {
            val stackObj = bigNode.getFromStack(stackName + i)

            Assertions.assertEquals(obj, stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNode, stackSize = i + 1)

            bigNode.removeFromStack(stackName + i)


            if (i == 0) {
                checkBigNode(bigNode, stackSize = i)
            } else {
                checkBigNode(bigNode, stackSize = i)
            }
        }
    }

    @Test
    fun `test stack with different and equal names`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        bigNode.addToStack("a", LxmNil)
        checkBigNode(bigNode, stackSize = 1)

        bigNode.addToStack("b", LxmNil)
        checkBigNode(bigNode, stackSize = 2)

        bigNode.addToStack("a", LxmNil)
        checkBigNode(bigNode, stackSize = 3)

        bigNode.addToStack("b", LxmNil)
        checkBigNode(bigNode, stackSize = 4)
    }

    @Test
    fun `test stack recursively`() {
        val memory = LexemMemory()
        val bigNodeOld = memory.lastNode

        // Add some in old
        bigNodeOld.addToStack("a", LxmInteger.Num0)
        bigNodeOld.addToStack("b", LxmInteger.Num1)
        bigNodeOld.addToStack("a", LxmInteger.Num2)
        bigNodeOld.addToStack("b", LxmInteger.Num_1)
        checkBigNode(bigNodeOld, stackSize = 4)

        // Add new bigNode
        val bigNodeNew = BigNode(0, previousNode = bigNodeOld)
        bigNodeOld.nextNode = bigNodeNew

        // Get one
        var stackObj = bigNodeNew.getFromStack("a")

        Assertions.assertEquals(LxmInteger.Num2, stackObj, "The value of the stack is incorrect")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, stackSize = 4)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, stackSize = 4)

        // Remove one
        bigNodeNew.removeFromStack("a")
        stackObj = bigNodeNew.getFromStack("a")

        Assertions.assertEquals(LxmInteger.Num0, stackObj, "The value of the stack is incorrect")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, stackSize = 3)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, stackSize = 4)

        // Add some in new
        bigNodeNew.addToStack("b", LxmLogic.True)
        bigNodeNew.addToStack("c", LxmLogic.False)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, stackSize = 5)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, stackSize = 4)

        // Remove all out of new
        bigNodeNew.removeFromStack("b")
        bigNodeNew.removeFromStack("b")
        bigNodeNew.removeFromStack("c")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, stackSize = 2)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, stackSize = 4)

        // Remove all out of old
        bigNodeOld.removeFromStack("a")
        bigNodeOld.removeFromStack("a")
        bigNodeOld.removeFromStack("b")
        bigNodeOld.removeFromStack("b")
        checkBigNode(bigNodeOld, nextNode = bigNodeNew)
    }

    @Test
    fun `test stack addition does not shift when adding a level`() {
        val memory = LexemMemory()
        val bigNodeOld = memory.lastNode

        // Add some in old
        bigNodeOld.addToStack("a", LxmInteger.Num0)
        checkBigNode(bigNodeOld, stackSize = 1)

        // Add new bigNode
        val bigNodeNew = BigNode(0, previousNode = bigNodeOld)
        bigNodeOld.nextNode = bigNodeNew

        // Add some in new
        bigNodeNew.addToStack("a", LxmLogic.True)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, stackSize = 2)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, stackSize = 1)
    }

    @Test
    fun `test stack replace`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        // Add some
        bigNode.addToStack("a", LxmInteger.Num0)
        bigNode.addToStack("b", LxmInteger.Num1)
        bigNode.addToStack("a", LxmInteger.Num2)
        checkBigNode(bigNode, stackSize = 3)

        var a = bigNode.getFromStack("a")
        var b = bigNode.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num2, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, b, "The b value is incorrect")

        // Replace a and b
        bigNode.replaceStackCell("a", LxmInteger.Num10)
        bigNode.replaceStackCell("b", LxmInteger.Num_1)
        checkBigNode(bigNode, stackSize = 3)

        a = bigNode.getFromStack("a")
        b = bigNode.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num10, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num_1, b, "The b value is incorrect")
    }

    @Test
    fun `test stack replace recursively`() {
        val memory = LexemMemory()
        val bigNodeOld = memory.lastNode

        // Add some in old
        bigNodeOld.addToStack("a", LxmInteger.Num0)
        bigNodeOld.addToStack("b", LxmInteger.Num1)
        bigNodeOld.addToStack("a", LxmInteger.Num2)
        checkBigNode(bigNodeOld, stackSize = 3)

        var a = bigNodeOld.getFromStack("a")
        var b = bigNodeOld.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num2, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, b, "The b value is incorrect")

        // Add new bigNode
        val bigNodeNew = BigNode(0, previousNode = bigNodeOld)
        bigNodeOld.nextNode = bigNodeNew

        // Replace a and b
        bigNodeNew.replaceStackCell("a", LxmInteger.Num10)
        bigNodeNew.replaceStackCell("b", LxmInteger.Num_1)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, stackSize = 3)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, stackSize = 3)

        a = bigNodeNew.getFromStack("a")
        b = bigNodeNew.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num10, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num_1, b, "The b value is incorrect")
    }

    @Test
    @Incorrect
    fun `test remove an undefined value`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.removeFromStack("test")
        }
    }

    @Test
    @Incorrect
    fun `test get an undefined value`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.getFromStack("test")
        }
    }

    @Test
    @Incorrect
    fun `test replace an undefined value`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.replaceStackCell("test", LxmNil)
        }
    }

    @Test
    fun `test heap alloc and get`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        checkBigNode(bigNode)

        // Add cells
        val objects = List(size) { LxmObject(memory) }
        checkBigNode(bigNode, heapSize = size)

        // Get all cells
        for (i in objects.withIndex()) {
            val cell = bigNode.getHeapCell(i.index, toWrite = false)

            BigNodeHeapCellTest.checkCell(bigNode, cell, i.value)
            checkBigNode(bigNode, heapSize = size)
        }
    }

    @Test
    fun `test heap alloc and get recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode

        checkBigNode(oldBigNode)

        // Add cells
        val oldObjects = List(size) { LxmObject(memory) }
        checkBigNode(oldBigNode, heapSize = size)

        TestUtils.freezeCopy(memory)

        // Add new bigNode.
        val newBigNode = memory.lastNode

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)

        // Add cells
        val newObjects = List(size) { LxmList(memory) }
        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)

        // Get cells
        for (i in oldObjects.withIndex()) {
            val cell = newBigNode.getHeapCell(i.index, toWrite = false)

            BigNodeHeapCellTest.checkCell(memory.lastNode, cell, cell.getValue(memory.lastNode, toWrite = false))
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)
        }

        for (i in newObjects.withIndex()) {
            val cell = newBigNode.getHeapCell(i.index + size, toWrite = false)

            BigNodeHeapCellTest.checkCell(memory.lastNode, cell, i.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)
        }

        // Get all cells from old
        for (i in oldObjects.withIndex()) {
            val cell = oldBigNode.getHeapCell(i.index, toWrite = false)

            BigNodeHeapCellTest.checkCell(memory.lastNode, cell, i.value)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)
        }
    }

    @Test
    @Incorrect
    fun `test get forbidden cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.getHeapCell(55, toWrite = false)
        }
    }

    @Test
    fun `test heap alloc and free`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        checkBigNode(bigNode)

        // Add cells
        val objects = List(size) { LxmObject(memory) }
        checkBigNode(bigNode, heapSize = size)

        // Free all cells
        for ((index, _) in objects.withIndex().reversed()) {
            bigNode.freeHeapCell(index)
            checkBigNode(bigNode, heapSize = size, heapFreedCells = size - index, lastFreePosition = index)
        }

        // Add cells
        List(size) { LxmObject(memory) }
        checkBigNode(bigNode, heapSize = size, lastFreePosition = size)
    }

    @Test
    fun `test heap alloc and free recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode

        checkBigNode(oldBigNode)

        // Add cells
        val oldObjects = List(size) { LxmObject(memory) }
        checkBigNode(oldBigNode, heapSize = size)

        TestUtils.freezeCopy(memory)

        // Add new bigNode
        val newBigNode = memory.lastNode

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)

        // Free two cells
        newBigNode.freeHeapCell(size - 1)
        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, heapFreedCells = 1,
                lastFreePosition = size - 1)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)

        newBigNode.freeHeapCell(size - 2)

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, heapFreedCells = 2,
                lastFreePosition = size - 2)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)

        // Add cells
        val newObjects = List(size) { LxmList(memory) }
        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size - 2)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)

        // Get cells
        for (i in oldObjects.withIndex().take(2)) {
            val cell = newBigNode.getHeapCell(i.index, toWrite = false)

            BigNodeHeapCellTest.checkCell(memory.lastNode, cell, cell.getValue(memory.lastNode, toWrite = false))
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size - 2)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)
        }

        for (i in newObjects.withIndex()) {
            val cell = newBigNode.getHeapCell(i.index + size - 2, toWrite = false)

            BigNodeHeapCellTest.checkCell(memory.lastNode, cell, i.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size - 2)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)
        }

        // Free all cells
        for (i in 0 until 2 * size - 2) {
            newBigNode.freeHeapCell(i)
        }

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size - 2, heapFreedCells = 2 * size - 2,
                lastFreePosition = 2 * size - 3)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size)

        // Remove last bigNode to update the alive index.
        memory.rollbackCopy()

        // Get all cells from old
        for (i in oldObjects.withIndex()) {
            val cell = oldBigNode.getHeapCell(i.index, toWrite = false)

            BigNodeHeapCellTest.checkCell(memory.lastNode, cell, i.value)
            checkBigNode(oldBigNode, heapSize = size)
        }
    }
}
