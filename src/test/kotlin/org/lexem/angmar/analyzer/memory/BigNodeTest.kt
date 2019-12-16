package org.lexem.angmar.analyzer.memory

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

internal class BigNodeTest {
    companion object {
        private const val size = 4

        // AUX METHODS --------------------------------------------------------

        // Checks the status of a BigNode.
        private fun checkBigNode(bigNode: BigNode, prevNode: BigNode? = null, nextNode: BigNode? = null,
                stackLevelSize: Int = 0, actualStackLevelSize: Int = 0, stackSize: Int = 0, actualStackSize: Int = 0,
                heapSize: Int = 0, actualHeapSize: Int = 0, actualUsedCellCount: Int? = null,
                lastFreePosition: Int? = null) {
            Assertions.assertEquals(stackSize, bigNode.stackSize, "The stackSize property is incorrect")
            Assertions.assertEquals(actualStackSize, bigNode.actualStackSize,
                    "The actualStackSize property is incorrect")
            Assertions.assertEquals(stackLevelSize, bigNode.stackLevelSize, "The stackLevelSize property is incorrect")
            Assertions.assertEquals(actualStackLevelSize, bigNode.actualStackLevelSize,
                    "The actualStackLevelSize property is incorrect")

            Assertions.assertEquals(heapSize, bigNode.heapSize, "The heapSize property is incorrect")
            Assertions.assertEquals(actualHeapSize, bigNode.actualHeapSize, "The actualHeapSize property is incorrect")
            Assertions.assertEquals(actualUsedCellCount ?: actualHeapSize, bigNode.actualUsedCellCount,
                    "The actualUsedCellCount property is incorrect")
            Assertions.assertEquals(lastFreePosition ?: actualHeapSize, bigNode.lastFreePosition,
                    "The lastFreePosition property is incorrect")

            Assertions.assertEquals(prevNode, bigNode.previousNode, "The previousNode is incorrect")
            Assertions.assertEquals(nextNode, bigNode.nextNode, "The nextNode is incorrect")
        }
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `test constructors`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val empty = LxmObject(memory)
        checkBigNode(bigNode)

        // Modify the first BigNode.
        val lastFreePosition = let {
            bigNode.alloc(memory, empty)

            val cell = bigNode.alloc(memory, empty)
            bigNode.alloc(memory, empty)
            bigNode.free(memory, cell.position)

            bigNode.addToStack("last", LxmNil, memory)

            cell.position
        }

        val bigNode2 = BigNode(bigNode, null)
        bigNode.nextNode = bigNode2
        bigNode2.addToStack("last2", LxmNil, memory)

        checkBigNode(bigNode2, prevNode = bigNode, stackLevelSize = 1, actualStackLevelSize = 1, stackSize = 2,
                actualStackSize = 2, lastFreePosition = lastFreePosition, actualHeapSize = 3, heapSize = 0,
                actualUsedCellCount = 2)
        checkBigNode(bigNode, nextNode = bigNode2, stackLevelSize = 1, actualStackLevelSize = 1, stackSize = 1,
                actualStackSize = 1, lastFreePosition = lastFreePosition, actualHeapSize = 3, heapSize = 3,
                actualUsedCellCount = 2)
    }

    @Test
    fun `test stack with same names`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size) { LxmString.from(it.toString()) }
        val stackName = "last"

        // Add some.
        for ((i, obj) in objects.withIndex()) {
            bigNode.addToStack(stackName, obj, memory)
            checkBigNode(bigNode, actualStackLevelSize = i + 1, stackLevelSize = i + 1, actualStackSize = i + 1,
                    stackSize = i + 1)
        }

        // Get and remove some.
        for ((i, obj) in objects.withIndex().reversed()) {
            val stackObj = bigNode.getFromStack(stackName)

            Assertions.assertEquals(obj, stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNode, actualStackLevelSize = i + 1, stackLevelSize = i + 1, actualStackSize = i + 1,
                    stackSize = i + 1)

            bigNode.removeFromStack(stackName, memory)

            checkBigNode(bigNode, actualStackLevelSize = i, stackLevelSize = i, actualStackSize = i, stackSize = i)
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
            bigNode.addToStack(stackName + i, obj, memory)
            checkBigNode(bigNode, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = i + 1,
                    stackSize = i + 1)
        }

        // Get and remove some.
        for ((i, obj) in objects.withIndex().reversed()) {
            val stackObj = bigNode.getFromStack(stackName + i)

            Assertions.assertEquals(obj, stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNode, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = i + 1,
                    stackSize = i + 1)

            bigNode.removeFromStack(stackName + i, memory)


            if (i == 0) {
                checkBigNode(bigNode, actualStackLevelSize = 0, stackLevelSize = 0, actualStackSize = i, stackSize = i)
            } else {
                checkBigNode(bigNode, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = i, stackSize = i)
            }
        }
    }

    @Test
    fun `test stack with different and equal names`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        bigNode.addToStack("a", LxmNil, memory)
        checkBigNode(bigNode, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = 1, stackSize = 1)

        bigNode.addToStack("b", LxmNil, memory)
        checkBigNode(bigNode, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = 2, stackSize = 2)

        bigNode.addToStack("a", LxmNil, memory)
        checkBigNode(bigNode, actualStackLevelSize = 2, stackLevelSize = 2, actualStackSize = 3, stackSize = 3)

        bigNode.addToStack("b", LxmNil, memory)
        checkBigNode(bigNode, actualStackLevelSize = 2, stackLevelSize = 2, actualStackSize = 4, stackSize = 4)
    }

    @Test
    fun `test stack recursively`() {
        val memory = LexemMemory()
        val bigNodeOld = memory.lastNode

        // Add some in old
        bigNodeOld.addToStack("a", LxmInteger.Num0, memory)
        bigNodeOld.addToStack("b", LxmInteger.Num1, memory)
        bigNodeOld.addToStack("a", LxmInteger.Num2, memory)
        bigNodeOld.addToStack("b", LxmInteger.Num_1, memory)
        checkBigNode(bigNodeOld, actualStackLevelSize = 2, stackLevelSize = 2, actualStackSize = 4, stackSize = 4)

        // Add new bigNode
        val bigNodeNew = BigNode(bigNodeOld, null)
        bigNodeOld.nextNode = bigNodeNew

        // Get one
        var stackObj = bigNodeNew.getFromStack("a")

        Assertions.assertEquals(LxmInteger.Num2, stackObj, "The value of the stack is incorrect")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 2, stackLevelSize = 0,
                actualStackSize = 4, stackSize = 0)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, stackLevelSize = 2,
                actualStackSize = 4, stackSize = 4)

        // Remove one
        bigNodeNew.removeFromStack("a", memory)
        stackObj = bigNodeNew.getFromStack("a")

        Assertions.assertEquals(LxmInteger.Num0, stackObj, "The value of the stack is incorrect")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 2, stackLevelSize = 1,
                actualStackSize = 3, stackSize = 1)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, stackLevelSize = 2,
                actualStackSize = 4, stackSize = 4)

        // Add some in new
        bigNodeNew.addToStack("b", LxmLogic.True, memory)
        bigNodeNew.addToStack("c", LxmLogic.False, memory)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 3, stackLevelSize = 2,
                actualStackSize = 5, stackSize = 3)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, stackLevelSize = 2,
                actualStackSize = 4, stackSize = 4)

        // Remove all out of new
        bigNodeNew.removeFromStack("b", memory)
        bigNodeNew.removeFromStack("b", memory)
        bigNodeNew.removeFromStack("c", memory)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 1, stackLevelSize = 0,
                actualStackSize = 2, stackSize = 0)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, stackLevelSize = 2,
                actualStackSize = 4, stackSize = 4)

        // Remove all out of old
        bigNodeOld.removeFromStack("a", memory)
        bigNodeOld.removeFromStack("a", memory)
        bigNodeOld.removeFromStack("b", memory)
        bigNodeOld.removeFromStack("b", memory)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew)
    }

    @Test
    fun `test stack addition does not shift when adding a level`() {
        val memory = LexemMemory()
        val bigNodeOld = memory.lastNode

        // Add some in old
        bigNodeOld.addToStack("a", LxmInteger.Num0, memory)
        checkBigNode(bigNodeOld, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = 1, stackSize = 1)

        // Add new bigNode
        val bigNodeNew = BigNode(bigNodeOld, null)
        bigNodeOld.nextNode = bigNodeNew

        // Add some in new
        bigNodeNew.addToStack("a", LxmLogic.True, memory)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 2, stackLevelSize = 1,
                actualStackSize = 2, stackSize = 1)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 1, stackLevelSize = 1,
                actualStackSize = 1, stackSize = 1)
    }

    @Test
    fun `test stack replace`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        // Add some
        bigNode.addToStack("a", LxmInteger.Num0, memory)
        bigNode.addToStack("b", LxmInteger.Num1, memory)
        bigNode.addToStack("a", LxmInteger.Num2, memory)
        checkBigNode(bigNode, actualStackLevelSize = 2, stackLevelSize = 2, actualStackSize = 3, stackSize = 3)

        var a = bigNode.getFromStack("a")
        var b = bigNode.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num2, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, b, "The b value is incorrect")

        // Replace a and b
        bigNode.replaceStackCell("a", LxmInteger.Num10, memory)
        bigNode.replaceStackCell("b", LxmInteger.Num_1, memory)
        checkBigNode(bigNode, actualStackLevelSize = 2, stackLevelSize = 2, actualStackSize = 3, stackSize = 3)

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
        bigNodeOld.addToStack("a", LxmInteger.Num0, memory)
        bigNodeOld.addToStack("b", LxmInteger.Num1, memory)
        bigNodeOld.addToStack("a", LxmInteger.Num2, memory)
        checkBigNode(bigNodeOld, actualStackLevelSize = 2, stackLevelSize = 2, actualStackSize = 3, stackSize = 3)

        var a = bigNodeOld.getFromStack("a")
        var b = bigNodeOld.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num2, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, b, "The b value is incorrect")

        // Add new bigNode
        val bigNodeNew = BigNode(bigNodeOld, null)
        bigNodeOld.nextNode = bigNodeNew

        // Replace a and b
        bigNodeNew.replaceStackCell("a", LxmInteger.Num10, memory)
        bigNodeNew.replaceStackCell("b", LxmInteger.Num_1, memory)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 2, stackLevelSize = 2,
                actualStackSize = 3, stackSize = 3)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, stackLevelSize = 2,
                actualStackSize = 3, stackSize = 3)

        a = bigNodeNew.getFromStack("a")
        b = bigNodeNew.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num10, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num_1, b, "The b value is incorrect")
    }

    @Test
    fun `test stack replace with objects`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val obj1 = LxmObject(memory)
        val obj2 = LxmObject(memory)
        val obj1Ref = bigNode.alloc(memory, obj1)
        val obj2Ref = bigNode.alloc(memory, obj2)

        // Add obj1
        bigNode.addToStack("a", LxmReference(obj1Ref.position), memory)
        checkBigNode(bigNode, heapSize = 2, actualHeapSize = 2, actualStackLevelSize = 1, stackLevelSize = 1,
                actualStackSize = 1, stackSize = 1)

        // Replace obj2
        bigNode.replaceStackCell("a", LxmReference(obj2Ref.position), memory)
        checkBigNode(bigNode, heapSize = 2, actualHeapSize = 2, actualStackLevelSize = 1, stackLevelSize = 1,
                actualStackSize = 1, stackSize = 1, actualUsedCellCount = 1, lastFreePosition = 0)
    }

    @Test
    @Incorrect
    fun `test remove an undefined value`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.removeFromStack("test", memory)
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
            bigNode.replaceStackCell("test", LxmNil, memory)
        }
    }

    @Test
    fun `test heap alloc, get and set`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size) { LxmObject(memory) }

        checkBigNode(bigNode)

        // Add cells
        for (i in objects.withIndex()) {
            bigNode.alloc(memory, i.value)

            checkBigNode(bigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        // Get all cells
        for (i in objects.withIndex()) {
            val cell = bigNode.getCell(memory, i.index)

            BigNodeCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(bigNode, heapSize = size, actualHeapSize = size)
        }

        // Set cells
        val newObjects = List(size) { LxmList(memory) }
        for (i in newObjects.withIndex()) {
            bigNode.setCell(memory, i.index, i.value)

            checkBigNode(bigNode, heapSize = size, actualHeapSize = size)

            val cell = bigNode.getCell(memory, i.index)

            BigNodeCellTest.checkCell(cell, i.index, i.value)
        }
    }

    @Test
    fun `test heap alloc, get and set recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode
        val empty = LxmObject(memory)
        val oldObjects = List(size) { empty }

        checkBigNode(oldBigNode)

        // Add cells
        for (i in oldObjects.withIndex()) {
            oldBigNode.alloc(memory, i.value)

            checkBigNode(oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        // Add new bigNode
        val newBigNode = BigNode(oldBigNode, null)
        oldBigNode.nextNode = newBigNode
        val newObjects = List(size) { LxmList(memory) }

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        // Add cells
        for (i in newObjects.withIndex()) {
            newBigNode.alloc(memory, i.value)

            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1 + size)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        // Get cells
        for (i in oldObjects.withIndex()) {
            val cell = newBigNode.getCell(memory, i.index)

            BigNodeCellTest.checkCell(cell, i.index, cell.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        for (i in newObjects.withIndex()) {
            val cell = newBigNode.getCell(memory, i.index + size)

            BigNodeCellTest.checkCell(cell, i.index + size, i.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        // Set cells
        val newObjects2 = List(size) { LxmList(memory) }
        for (i in newObjects2.withIndex()) {
            newBigNode.setCell(memory, i.index + size - 2, i.value)

            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = minOf(size + i.index + 1, 2 * size - 2),
                    actualHeapSize = 2 * size)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

            val cell = newBigNode.getCell(memory, i.index + size - 2)

            BigNodeCellTest.checkCell(cell, i.index + size - 2, i.value)
        }

        // Get all cells from old
        for (i in oldObjects.withIndex()) {
            val cell = oldBigNode.getCell(memory, i.index)

            BigNodeCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }
    }

    @Test
    @Incorrect
    fun `test get forbidden cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.getCell(memory, 55)
        }
    }

    @Test
    @Incorrect
    fun `test set forbidden cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val empty = LxmObject(memory)
            bigNode.setCell(memory, 55, empty)
        }
    }

    @Test
    fun `test heap alloc and free`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size) { LxmObject(memory) }

        checkBigNode(bigNode)

        // Add cells
        for (i in objects.withIndex()) {
            bigNode.alloc(memory, i.value)

            checkBigNode(bigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        // Free all cells
        for (i in objects.withIndex().reversed()) {
            bigNode.free(memory, i.index)
            checkBigNode(bigNode, heapSize = size, actualHeapSize = size, actualUsedCellCount = i.index,
                    lastFreePosition = i.index)
        }

        // Add cells
        for (i in objects.withIndex()) {
            bigNode.alloc(memory, i.value)

            checkBigNode(bigNode, heapSize = size, actualHeapSize = size, actualUsedCellCount = i.index + 1,
                    lastFreePosition = i.index + 1)
        }
    }

    @Test
    fun `test heap alloc and free recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode
        val empty = LxmObject(memory)
        val oldObjects = List(size) { empty }

        checkBigNode(oldBigNode)

        // Add cells
        for (i in oldObjects.withIndex()) {
            oldBigNode.alloc(memory, i.value)

            checkBigNode(oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        // Add new bigNode
        memory.freezeCopy()
        val newBigNode = memory.lastNode
        val newObjects = List(size) { LxmList(memory) }

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        // Free two cells
        newBigNode.free(memory, size - 1)
        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 1, actualHeapSize = size,
                actualUsedCellCount = size - 1, lastFreePosition = size - 1)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        newBigNode.free(memory, size - 2)

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2, actualHeapSize = size,
                actualUsedCellCount = size - 2, lastFreePosition = size - 2)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        // Add cells
        for (i in newObjects.withIndex().take(2)) {
            newBigNode.alloc(memory, i.value)

            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2, actualHeapSize = size,
                    actualUsedCellCount = size - 1 + i.index, lastFreePosition = i.index + 1 + size - 2)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        for (i in newObjects.withIndex().drop(2)) {
            newBigNode.alloc(memory, i.value)

            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index - 1 + size)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        // Get cells
        for (i in oldObjects.withIndex().take(2)) {
            val cell = newBigNode.getCell(memory, i.index)

            BigNodeCellTest.checkCell(cell, i.index, cell.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size - 2)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        for (i in newObjects.withIndex()) {
            val cell = newBigNode.getCell(memory, i.index + size - 2)

            BigNodeCellTest.checkCell(cell, i.index + size - 2, i.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size - 2)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        // Free all cells
        for (i in 0 until 2 * size - 2) {
            newBigNode.free(memory, i)
        }

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size - 2, actualHeapSize = 2 * size - 2,
                actualUsedCellCount = 0, lastFreePosition = 2 * size - 3)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        // Get all cells from old
        for (i in oldObjects.withIndex()) {
            val cell = oldBigNode.getCell(memory, i.index)

            BigNodeCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }
    }

    @Test
    fun `test collapseTo only stack`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode

        checkBigNode(oldBigNode)

        // Add values to stack
        val primitive0 = LxmInteger.Num1
        val primitive1 = LxmNil
        var primitive2 = LxmLogic.False

        oldBigNode.addToStack("a", primitive0, memory)
        oldBigNode.addToStack("b", primitive1, memory)
        oldBigNode.addToStack("a", primitive2, memory)

        checkBigNode(oldBigNode, stackLevelSize = 2, actualStackLevelSize = 2, stackSize = 3, actualStackSize = 3)


        memory.freezeCopy()


        // Add values to stack
        val oldBigNode2 = memory.lastNode

        primitive2 = LxmLogic.True
        val primitive3 = LxmInteger.Num2
        val primitive4 = LxmString.Empty

        oldBigNode2.replaceStackCell("a", primitive2, memory)
        oldBigNode2.addToStack("b", primitive3, memory)
        oldBigNode2.addToStack("c", primitive4, memory)

        checkBigNode(oldBigNode2, prevNode = oldBigNode, stackLevelSize = 1, actualStackLevelSize = 2, stackSize = 3,
                actualStackSize = 5)


        memory.freezeCopy()


        // Add values to stack
        val bigNode = memory.lastNode

        bigNode.removeFromStack("c", memory)

        checkBigNode(bigNode, prevNode = oldBigNode2, stackLevelSize = 1, actualStackLevelSize = 2, stackSize = 2,
                actualStackSize = 4)

        // Collapse
        oldBigNode.nextNode!!.previousNode = null
        bigNode.collapseTo(oldBigNode)

        checkBigNode(bigNode)
        checkBigNode(oldBigNode2)
        checkBigNode(oldBigNode, nextNode = oldBigNode2, stackLevelSize = 2, actualStackLevelSize = 2, stackSize = 4,
                actualStackSize = 4)

        Assertions.assertEquals(oldBigNode.getFromStack("a"), primitive2, "The stack['a'] is incorrect")
        Assertions.assertEquals(oldBigNode.getFromStack("b"), primitive3, "The stack['b'] is incorrect")
        try {
            oldBigNode.getFromStack("c")
            throw Error("The c value cannot exist.")
        } catch (e: AngmarAnalyzerException) {
        }
    }

    @Test
    fun `test collapseTo heap and stack`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode

        checkBigNode(oldBigNode)

        // Add values to heap
        val obj0 = LxmObject(memory)
        obj0.setProperty(memory, "x", LxmString.Empty)
        var obj1 = LxmList(memory)
        obj1.addCell(memory, LxmInteger.Num0)
        val obj2 = LxmObject(memory)

        val cell0 = oldBigNode.alloc(memory, obj0).position
        val cell1 = oldBigNode.alloc(memory, obj1).position
        val cell2 = oldBigNode.alloc(memory, obj2).position

        checkBigNode(oldBigNode, heapSize = 3, actualHeapSize = 3)


        memory.freezeCopy()


        // Add values to heap
        val oldBigNode2 = memory.lastNode

        obj1 = oldBigNode2.getCell(memory, cell1, forceShift = true).value as LxmList
        obj1.addCell(memory, LxmInteger.Num10)
        val obj3 = LxmObject(memory)
        val obj4 = LxmObject(memory)
        val obj5 = LxmObject(memory)

        val cell3 = oldBigNode2.alloc(memory, obj3).position
        val cell4 = oldBigNode2.alloc(memory, obj4).position
        val cell5 = oldBigNode2.alloc(memory, obj5).position

        oldBigNode2.free(memory, cell4)

        checkBigNode(oldBigNode2, prevNode = oldBigNode, heapSize = 4, actualHeapSize = 6, actualUsedCellCount = 5,
                lastFreePosition = cell4)


        memory.freezeCopy()


        // Add values to heap
        val bigNode = memory.lastNode

        bigNode.free(memory, cell2)
        bigNode.free(memory, cell5)

        checkBigNode(bigNode, prevNode = oldBigNode2, heapSize = 2, actualHeapSize = 6, actualUsedCellCount = 3,
                lastFreePosition = cell5)

        // Collapse
        oldBigNode.nextNode!!.previousNode = null
        bigNode.collapseTo(oldBigNode)

        // Check all nodes are correct and destroyed.
        checkBigNode(bigNode)
        checkBigNode(oldBigNode2)
        checkBigNode(oldBigNode, nextNode = oldBigNode2, heapSize = 6, actualHeapSize = 6, actualUsedCellCount = 3,
                lastFreePosition = cell5)

        Assertions.assertEquals(obj0, oldBigNode.getCell(memory, cell0).value, "The heap[0] is incorrect")
        Assertions.assertEquals(obj1, oldBigNode.getCell(memory, cell1).value, "The heap[1] is incorrect")
        Assertions.assertNull(oldBigNode.getCell(memory, cell2).value, "The heap[2] is incorrect")
        Assertions.assertEquals(obj3, oldBigNode.getCell(memory, cell3).value, "The heap[3] is incorrect")
        Assertions.assertNull(oldBigNode.getCell(memory, cell4).value, "The heap[4] is incorrect")
        Assertions.assertNull(oldBigNode.getCell(memory, cell5).value, "The heap[5] is incorrect")
    }

    @Test
    fun `test destroy`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size) { LxmObject(memory) }

        checkBigNode(bigNode)

        // Add cells
        for (i in objects.withIndex()) {
            bigNode.alloc(memory, i.value)

            checkBigNode(bigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        bigNode.addToStack("a", LxmString.Nil, memory)
        bigNode.addToStack("b", LxmString.Nil, memory)
        bigNode.addToStack("a", LxmString.Nil, memory)

        checkBigNode(bigNode, heapSize = size, actualHeapSize = size, stackLevelSize = 2, actualStackLevelSize = 2,
                stackSize = 3, actualStackSize = 3)

        // Remove all elements
        bigNode.destroy()

        checkBigNode(bigNode)
    }

    @Test
    fun `test destroy recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode
        val oldObjects = List(size) { LxmObject(memory) }
        val stackName = "last"

        checkBigNode(oldBigNode)

        // Add cells and stack
        for (i in oldObjects.withIndex()) {
            oldBigNode.alloc(memory, i.value)

            checkBigNode(oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        oldBigNode.addToStack(stackName, LxmString.Nil, memory)

        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size, stackLevelSize = 1, actualStackLevelSize = 1,
                stackSize = 1, actualStackSize = 1)

        // Add new bigNode
        val newBigNode = BigNode(oldBigNode, null)
        oldBigNode.nextNode = newBigNode
        val newObjects = List(size) { LxmList(memory) }

        checkBigNode(newBigNode, prevNode = oldBigNode, stackLevelSize = 0, actualStackLevelSize = 1,
                actualHeapSize = size, actualStackSize = 1)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, stackLevelSize = 1, actualStackLevelSize = 1,
                actualHeapSize = size, stackSize = 1, actualStackSize = 1)

        // Add cells and stack
        for (i in newObjects.withIndex()) {
            newBigNode.alloc(memory, i.value)

            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = i.index + 1, stackLevelSize = 0,
                    actualStackLevelSize = 1, actualHeapSize = i.index + 1 + size, actualStackSize = 1)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, stackLevelSize = 1,
                    actualStackLevelSize = 1, actualHeapSize = size, stackSize = 1, actualStackSize = 1)
        }

        newBigNode.addToStack(stackName, LxmString.Nil, memory)

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size, stackLevelSize = 1,
                actualStackLevelSize = 2, stackSize = 1, actualStackSize = 2)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size, stackLevelSize = 1,
                actualStackLevelSize = 1, stackSize = 1, actualStackSize = 1)

        // Remove all elements
        newBigNode.destroy()
        oldBigNode.nextNode = null

        checkBigNode(newBigNode)
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size, stackLevelSize = 1, actualStackLevelSize = 1,
                stackSize = 1, actualStackSize = 1)
    }
}
