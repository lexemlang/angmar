package org.lexem.angmar.analyzer.memory

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.config.*
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
        checkBigNode(bigNode)

        // Modify the first BigNode.
        val lastFreePosition = let {
            val obj1 = LxmObject(memory)
            val obj2 = LxmObject(memory)
            val obj3 = LxmObject(memory)

            bigNode.freeHeapCell(memory, obj3.getPrimitive().position)

            bigNode.addToStack(memory, "last", LxmNil)

            obj3.getPrimitive().position
        }

        val bigNode2 = BigNode(bigNode, null)
        bigNode.nextNode = bigNode2
        bigNode2.addToStack(memory, "last2", LxmNil)

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
            bigNode.addToStack(memory, stackName, obj)
            checkBigNode(bigNode, actualStackLevelSize = i + 1, stackLevelSize = i + 1, actualStackSize = i + 1,
                    stackSize = i + 1)
        }

        // Get and remove some.
        for ((i, obj) in objects.withIndex().reversed()) {
            val stackObj = bigNode.getFromStack(stackName)

            Assertions.assertEquals(obj, stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNode, actualStackLevelSize = i + 1, stackLevelSize = i + 1, actualStackSize = i + 1,
                    stackSize = i + 1)

            bigNode.removeFromStack(memory, stackName)

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
            bigNode.addToStack(memory, stackName + i, obj)
            checkBigNode(bigNode, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = i + 1,
                    stackSize = i + 1)
        }

        // Get and remove some.
        for ((i, obj) in objects.withIndex().reversed()) {
            val stackObj = bigNode.getFromStack(stackName + i)

            Assertions.assertEquals(obj, stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNode, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = i + 1,
                    stackSize = i + 1)

            bigNode.removeFromStack(memory, stackName + i)


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

        bigNode.addToStack(memory, "a", LxmNil)
        checkBigNode(bigNode, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = 1, stackSize = 1)

        bigNode.addToStack(memory, "b", LxmNil)
        checkBigNode(bigNode, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = 2, stackSize = 2)

        bigNode.addToStack(memory, "a", LxmNil)
        checkBigNode(bigNode, actualStackLevelSize = 2, stackLevelSize = 2, actualStackSize = 3, stackSize = 3)

        bigNode.addToStack(memory, "b", LxmNil)
        checkBigNode(bigNode, actualStackLevelSize = 2, stackLevelSize = 2, actualStackSize = 4, stackSize = 4)
    }

    @Test
    fun `test stack recursively`() {
        val memory = LexemMemory()
        val bigNodeOld = memory.lastNode

        // Add some in old
        bigNodeOld.addToStack(memory, "a", LxmInteger.Num0)
        bigNodeOld.addToStack(memory, "b", LxmInteger.Num1)
        bigNodeOld.addToStack(memory, "a", LxmInteger.Num2)
        bigNodeOld.addToStack(memory, "b", LxmInteger.Num_1)
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
        bigNodeNew.removeFromStack(memory, "a")
        stackObj = bigNodeNew.getFromStack("a")

        Assertions.assertEquals(LxmInteger.Num0, stackObj, "The value of the stack is incorrect")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 2, stackLevelSize = 1,
                actualStackSize = 3, stackSize = 1)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, stackLevelSize = 2,
                actualStackSize = 4, stackSize = 4)

        // Add some in new
        bigNodeNew.addToStack(memory, "b", LxmLogic.True)
        bigNodeNew.addToStack(memory, "c", LxmLogic.False)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 3, stackLevelSize = 2,
                actualStackSize = 5, stackSize = 3)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, stackLevelSize = 2,
                actualStackSize = 4, stackSize = 4)

        // Remove all out of new
        bigNodeNew.removeFromStack(memory, "b")
        bigNodeNew.removeFromStack(memory, "b")
        bigNodeNew.removeFromStack(memory, "c")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 1, stackLevelSize = 0,
                actualStackSize = 2, stackSize = 0)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, stackLevelSize = 2,
                actualStackSize = 4, stackSize = 4)

        // Remove all out of old
        bigNodeOld.removeFromStack(memory, "a")
        bigNodeOld.removeFromStack(memory, "a")
        bigNodeOld.removeFromStack(memory, "b")
        bigNodeOld.removeFromStack(memory, "b")
        checkBigNode(bigNodeOld, nextNode = bigNodeNew)
    }

    @Test
    fun `test not shift cell until reach threshold`() {
        val memory = LexemMemory()
        val bigNodeOld = memory.lastNode

        val obj = LxmObject(memory)

        for (i in 0 until Consts.Memory.maxDistanceToShift - 1) {
            memory.freezeCopy()

            val obj2 = memory.lastNode.getHeapCell(obj.getPrimitive().position, forceShift = false).value
            Assertions.assertEquals(obj, obj2, "The object has been shifted in [$i]")
        }

        memory.freezeCopy()

        val obj2 = memory.lastNode.getHeapCell(obj.getPrimitive().position, forceShift = true).value as LxmObject
        Assertions.assertNotEquals(obj, obj2, "The object has not been shifted")
        Assertions.assertEquals(obj, obj2.oldVersion, "The object has not been shifted")
    }

    @Test
    fun `test stack addition does not shift when adding a level`() {
        val memory = LexemMemory()
        val bigNodeOld = memory.lastNode

        // Add some in old
        bigNodeOld.addToStack(memory, "a", LxmInteger.Num0)
        checkBigNode(bigNodeOld, actualStackLevelSize = 1, stackLevelSize = 1, actualStackSize = 1, stackSize = 1)

        // Add new bigNode
        val bigNodeNew = BigNode(bigNodeOld, null)
        bigNodeOld.nextNode = bigNodeNew

        // Add some in new
        bigNodeNew.addToStack(memory, "a", LxmLogic.True)
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
        bigNode.addToStack(memory, "a", LxmInteger.Num0)
        bigNode.addToStack(memory, "b", LxmInteger.Num1)
        bigNode.addToStack(memory, "a", LxmInteger.Num2)
        checkBigNode(bigNode, actualStackLevelSize = 2, stackLevelSize = 2, actualStackSize = 3, stackSize = 3)

        var a = bigNode.getFromStack("a")
        var b = bigNode.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num2, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, b, "The b value is incorrect")

        // Replace a and b
        bigNode.replaceStackCell(memory, "a", LxmInteger.Num10)
        bigNode.replaceStackCell(memory, "b", LxmInteger.Num_1)
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
        bigNodeOld.addToStack(memory, "a", LxmInteger.Num0)
        bigNodeOld.addToStack(memory, "b", LxmInteger.Num1)
        bigNodeOld.addToStack(memory, "a", LxmInteger.Num2)
        checkBigNode(bigNodeOld, actualStackLevelSize = 2, stackLevelSize = 2, actualStackSize = 3, stackSize = 3)

        var a = bigNodeOld.getFromStack("a")
        var b = bigNodeOld.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num2, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, b, "The b value is incorrect")

        // Add new bigNode
        val bigNodeNew = BigNode(bigNodeOld, null)
        bigNodeOld.nextNode = bigNodeNew

        // Replace a and b
        bigNodeNew.replaceStackCell(memory, "a", LxmInteger.Num10)
        bigNodeNew.replaceStackCell(memory, "b", LxmInteger.Num_1)
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

        // Add obj1
        bigNode.addToStack(memory, "a", obj1.getPrimitive())
        checkBigNode(bigNode, heapSize = 2, actualHeapSize = 2, actualStackLevelSize = 1, stackLevelSize = 1,
                actualStackSize = 1, stackSize = 1)

        // Replace obj2
        bigNode.replaceStackCell(memory, "a", obj2.getPrimitive())
        checkBigNode(bigNode, heapSize = 2, actualHeapSize = 2, actualStackLevelSize = 1, stackLevelSize = 1,
                actualStackSize = 1, stackSize = 1, actualUsedCellCount = 1, lastFreePosition = 0)
    }

    @Test
    @Incorrect
    fun `test remove an undefined value`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.StackNotFoundElement) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.removeFromStack(memory, "test")
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
            bigNode.replaceStackCell(memory, "test", LxmNil)
        }
    }

    @Test
    fun `test heap alloc and get`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        checkBigNode(bigNode)

        // Add cells
        val objects = List(size) { LxmObject(memory) }
        checkBigNode(bigNode, heapSize = size, actualHeapSize = size)

        // Get all cells
        for (i in objects.withIndex()) {
            val cell = bigNode.getHeapCell(i.index)

            BigNodeHeapCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(bigNode, heapSize = size, actualHeapSize = size)
        }
    }

    @Test
    fun `test heap alloc and get recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode

        checkBigNode(oldBigNode)

        // Add cells
        val oldObjects = List(size) { LxmObject(memory) }
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        memory.freezeCopy()

        // Add new bigNode.
        val newBigNode = memory.lastNode

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        // Add cells
        val newObjects = List(size) { LxmList(memory) }
        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        // Get cells
        for (i in oldObjects.withIndex()) {
            val cell = newBigNode.getHeapCell(i.index)

            BigNodeHeapCellTest.checkCell(cell, i.index, cell.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        for (i in newObjects.withIndex()) {
            val cell = newBigNode.getHeapCell(i.index + size)

            BigNodeHeapCellTest.checkCell(cell, i.index + size, i.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        // Get all cells from old
        for (i in oldObjects.withIndex()) {
            val cell = oldBigNode.getHeapCell(i.index)

            BigNodeHeapCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }
    }

    @Test
    @Incorrect
    fun `test get forbidden cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.getHeapCell(55)
        }
    }

    @Test
    fun `test heap alloc and free`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        checkBigNode(bigNode)

        // Add cells
        val objects = List(size) { LxmObject(memory) }
        checkBigNode(bigNode, heapSize = size, actualHeapSize = size)

        // Free all cells
        for (i in objects.withIndex().reversed()) {
            bigNode.freeHeapCell(memory, i.index)
            checkBigNode(bigNode, heapSize = size, actualHeapSize = size, actualUsedCellCount = i.index,
                    lastFreePosition = i.index)
        }

        // Add cells
        val objects2 = List(size) { LxmObject(memory) }
        checkBigNode(bigNode, heapSize = size, actualHeapSize = size, actualUsedCellCount = size,
                lastFreePosition = size)
    }

    @Test
    fun `test heap alloc and free recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode

        checkBigNode(oldBigNode)

        // Add cells
        val oldObjects = List(size) { LxmObject(memory) }
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        memory.freezeCopy()

        // Add new bigNode
        val newBigNode = memory.lastNode

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        // Free two cells
        newBigNode.freeHeapCell(memory, size - 1)
        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 1, actualHeapSize = size,
                actualUsedCellCount = size - 1, lastFreePosition = size - 1)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        newBigNode.freeHeapCell(memory, size - 2)

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2, actualHeapSize = size,
                actualUsedCellCount = size - 2, lastFreePosition = size - 2)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        // Add cells
        val newObjects = List(size) { LxmList(memory) }
        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size - 2)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        // Get cells
        for (i in oldObjects.withIndex().take(2)) {
            val cell = newBigNode.getHeapCell(i.index)

            BigNodeHeapCellTest.checkCell(cell, i.index, cell.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size - 2)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        for (i in newObjects.withIndex()) {
            val cell = newBigNode.getHeapCell(i.index + size - 2)

            BigNodeHeapCellTest.checkCell(cell, i.index + size - 2, i.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size - 2)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }

        // Free all cells
        for (i in 0 until 2 * size - 2) {
            newBigNode.freeHeapCell(memory, i)
        }

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size - 2, actualHeapSize = 2 * size - 2,
                actualUsedCellCount = 0, lastFreePosition = 2 * size - 3)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)

        // Get all cells from old
        for (i in oldObjects.withIndex()) {
            val cell = oldBigNode.getHeapCell(i.index)

            BigNodeHeapCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, actualHeapSize = size)
        }
    }

    @Test
    fun `test destroy`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        checkBigNode(bigNode)

        // Add cells
        val objects = List(size) { LxmObject(memory) }
        checkBigNode(bigNode, heapSize = size, actualHeapSize = size)

        bigNode.addToStack(memory, "a", LxmString.Nil)
        bigNode.addToStack(memory, "b", LxmString.Nil)
        bigNode.addToStack(memory, "a", LxmString.Nil)

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
        val stackName = "last"

        checkBigNode(oldBigNode)

        // Add cells and stack
        val oldObjects = List(size) { LxmObject(memory) }
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        oldBigNode.addToStack(memory, stackName, LxmString.Nil)

        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size, stackLevelSize = 1, actualStackLevelSize = 1,
                stackSize = 1, actualStackSize = 1)

        // Add new bigNode
        memory.freezeCopy()
        val newBigNode = memory.lastNode

        checkBigNode(newBigNode, prevNode = oldBigNode, stackLevelSize = 0, actualStackLevelSize = 1,
                actualHeapSize = size, actualStackSize = 1)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, stackLevelSize = 1, actualStackLevelSize = 1,
                actualHeapSize = size, stackSize = 1, actualStackSize = 1)

        // Add cells and stack
        val newObjects = List(size) { LxmList(memory) }
        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, stackLevelSize = 0, actualStackLevelSize = 1,
                actualHeapSize = 2 * size, actualStackSize = 1)
        checkBigNode(oldBigNode, nextNode = newBigNode, heapSize = size, stackLevelSize = 1, actualStackLevelSize = 1,
                actualHeapSize = size, stackSize = 1, actualStackSize = 1)

        newBigNode.addToStack(memory, stackName, LxmString.Nil)

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
