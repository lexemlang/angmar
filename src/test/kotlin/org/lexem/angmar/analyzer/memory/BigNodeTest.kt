package org.lexem.angmar.analyzer.memory

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

internal class BigNodeTest {
    companion object {
        private const val size = 4L

        // AUX METHODS --------------------------------------------------------

        // Checks the status of a BigNode.
        private fun checkBigNode(bigNode: BigNode, prevNode: BigNode? = null, nextNode: BigNode? = null,
                actualStackLevelSize: Long = 0, actualStackSize: Long = 0, actualHeapSize: Long = 0,
                nextFreeCell: Long? = null) {
            Assertions.assertEquals(actualStackSize, bigNode.actualStackSize,
                    "The actualStackSize property is incorrect")
            Assertions.assertEquals(actualStackLevelSize, bigNode.actualStackLevelSize,
                    "The actualStackLevelSize property is incorrect")

            Assertions.assertEquals(actualHeapSize, bigNode.actualHeapSize, "The actualHeapSize property is incorrect")
            Assertions.assertEquals(nextFreeCell ?: actualHeapSize, bigNode.nextFreeCell,
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

            bigNode.free(obj3.getPrimitive().position)

            bigNode.addToStack("last", LxmNil)

            obj3.getPrimitive().position
        }

        val bigNode2 = BigNode(bigNode, null)
        bigNode.nextNode = bigNode2
        bigNode2.addToStack("last2", LxmNil)

        checkBigNode(bigNode2, prevNode = bigNode, actualStackLevelSize = 1, actualStackSize = 2, actualHeapSize = 3)
        checkBigNode(bigNode, nextNode = bigNode2, actualStackLevelSize = 1, actualStackSize = 1, actualHeapSize = 3)
    }

    @Test
    fun `test stack with same names`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size.toInt()) { LxmString.from(it.toString()) }
        val stackName = "last"

        // Add some.
        for ((i, obj) in objects.withIndex()) {
            bigNode.addToStack(stackName, obj)
            checkBigNode(bigNode, actualStackLevelSize = i.toLong() + 1, actualStackSize = i.toLong() + 1)
        }

        // Get and remove some.
        for ((i, obj) in objects.withIndex().reversed()) {
            val stackObj = bigNode.getFromStack(stackName)

            Assertions.assertEquals(obj, stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNode, actualStackLevelSize = i.toLong() + 1, actualStackSize = i.toLong() + 1)

            bigNode.removeFromStack(stackName)

            checkBigNode(bigNode, actualStackLevelSize = i.toLong(), actualStackSize = i.toLong())
        }
    }

    @Test
    fun `test stack with different names`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size.toInt()) { LxmString.from(it.toString()) }
        val stackName = "last"

        // Add some.
        for ((i, obj) in objects.withIndex()) {
            bigNode.addToStack(stackName + i, obj)
            checkBigNode(bigNode, actualStackLevelSize = 1, actualStackSize = i.toLong() + 1)
        }

        // Get and remove some.
        for ((i, obj) in objects.withIndex().reversed()) {
            val stackObj = bigNode.getFromStack(stackName + i)

            Assertions.assertEquals(obj, stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNode, actualStackLevelSize = 1, actualStackSize = i.toLong() + 1)

            bigNode.removeFromStack(stackName + i)


            if (i == 0) {
                checkBigNode(bigNode, actualStackLevelSize = 0, actualStackSize = i.toLong())
            } else {
                checkBigNode(bigNode, actualStackLevelSize = 1, actualStackSize = i.toLong())
            }
        }
    }

    @Test
    fun `test stack with different and equal names`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        bigNode.addToStack("a", LxmNil)
        checkBigNode(bigNode, actualStackLevelSize = 1, actualStackSize = 1)

        bigNode.addToStack("b", LxmNil)
        checkBigNode(bigNode, actualStackLevelSize = 1, actualStackSize = 2)

        bigNode.addToStack("a", LxmNil)
        checkBigNode(bigNode, actualStackLevelSize = 2, actualStackSize = 3)

        bigNode.addToStack("b", LxmNil)
        checkBigNode(bigNode, actualStackLevelSize = 2, actualStackSize = 4)
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
        checkBigNode(bigNodeOld, actualStackLevelSize = 2, actualStackSize = 4)

        // Add new bigNode
        val bigNodeNew = BigNode(bigNodeOld, null)
        bigNodeOld.nextNode = bigNodeNew

        // Get one
        var stackObj = bigNodeNew.getFromStack("a")

        Assertions.assertEquals(LxmInteger.Num2, stackObj, "The value of the stack is incorrect")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 2, actualStackSize = 4)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, actualStackSize = 4)

        // Remove one
        bigNodeNew.removeFromStack("a")
        stackObj = bigNodeNew.getFromStack("a")

        Assertions.assertEquals(LxmInteger.Num0, stackObj, "The value of the stack is incorrect")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 2, actualStackSize = 3)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, actualStackSize = 4)

        // Add some in new
        bigNodeNew.addToStack("b", LxmLogic.True)
        bigNodeNew.addToStack("c", LxmLogic.False)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 3, actualStackSize = 5)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, actualStackSize = 4)

        // Remove all out of new
        bigNodeNew.removeFromStack("b")
        bigNodeNew.removeFromStack("b")
        bigNodeNew.removeFromStack("c")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 1, actualStackSize = 2)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, actualStackSize = 4)

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
        checkBigNode(bigNodeOld, actualStackLevelSize = 1, actualStackSize = 1)

        // Add new bigNode
        val bigNodeNew = BigNode(bigNodeOld, null)
        bigNodeOld.nextNode = bigNodeNew

        // Add some in new
        bigNodeNew.addToStack("a", LxmLogic.True)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 2, actualStackSize = 2)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 1, actualStackSize = 1)
    }

    @Test
    fun `test stack replace`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        // Add some
        bigNode.addToStack("a", LxmInteger.Num0)
        bigNode.addToStack("b", LxmInteger.Num1)
        bigNode.addToStack("a", LxmInteger.Num2)
        checkBigNode(bigNode, actualStackLevelSize = 2, actualStackSize = 3)

        var a = bigNode.getFromStack("a")
        var b = bigNode.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num2, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, b, "The b value is incorrect")

        // Replace a and b
        bigNode.replaceStackCell("a", LxmInteger.Num10)
        bigNode.replaceStackCell("b", LxmInteger.Num_1)
        checkBigNode(bigNode, actualStackLevelSize = 2, actualStackSize = 3)

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
        checkBigNode(bigNodeOld, actualStackLevelSize = 2, actualStackSize = 3)

        var a = bigNodeOld.getFromStack("a")
        var b = bigNodeOld.getFromStack("b")

        Assertions.assertEquals(LxmInteger.Num2, a, "The a value is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, b, "The b value is incorrect")

        // Add new bigNode
        val bigNodeNew = BigNode(bigNodeOld, null)
        bigNodeOld.nextNode = bigNodeNew

        // Replace a and b
        bigNodeNew.replaceStackCell("a", LxmInteger.Num10)
        bigNodeNew.replaceStackCell("b", LxmInteger.Num_1)
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackLevelSize = 2, actualStackSize = 3)
        checkBigNode(bigNodeOld, nextNode = bigNodeNew, actualStackLevelSize = 2, actualStackSize = 3)

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
        bigNode.addToStack("a", obj1.getPrimitive())
        checkBigNode(bigNode, actualStackLevelSize = 1, actualStackSize = 1, actualHeapSize = 2)

        // Replace obj2
        bigNode.replaceStackCell("a", obj2.getPrimitive())
        checkBigNode(bigNode, actualHeapSize = 2, actualStackLevelSize = 1, actualStackSize = 1)
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
        val objects = List(size.toInt()) { LxmObject(memory) }
        checkBigNode(bigNode, actualHeapSize = size)

        // Get all cells
        for (i in objects.withIndex()) {
            val cell = bigNode.getCell(memory, i.index.toLong(), toWrite = false)

            Assertions.assertEquals(cell, i.value, "The cell is incorrect")
            checkBigNode(bigNode, actualHeapSize = size)
        }
    }

    @Test
    fun `test heap alloc and get recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode

        checkBigNode(oldBigNode)

        // Add cells
        val oldObjects = List(size.toInt()) { LxmObject(memory) }
        checkBigNode(oldBigNode, actualHeapSize = size)

        memory.freezeCopy()

        // Add new bigNode.
        val newBigNode = memory.lastNode

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)

        // Add cells
        val newObjects = List(size.toInt()) { LxmList(memory) }
        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = 2 * size)
        checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)

        // Get cells
        for (i in oldObjects.withIndex()) {
            val cell = newBigNode.getCell(memory, i.index.toLong())

            Assertions.assertEquals(cell, i.value, "The cell is incorrect")
            checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = 2 * size)
            checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)
        }

        for (i in newObjects.withIndex()) {
            val cell = newBigNode.getCell(memory, i.index.toLong() + size)

            Assertions.assertEquals(cell, i.value, "The cell is incorrect")
            checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = 2 * size)
            checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)
        }

        // Get all cells from old
        for (i in oldObjects.withIndex()) {
            val cell = oldBigNode.getCell(memory, i.index.toLong())

            Assertions.assertEquals(cell, i.value, "The cell is incorrect")
            checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)
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
    fun `test heap alloc and free`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        checkBigNode(bigNode)

        // Add cells
        val objects = List(size.toInt()) { LxmObject(memory) }
        checkBigNode(bigNode, actualHeapSize = size)

        // Free all cells
        for (i in objects.withIndex().reversed()) {
            bigNode.free(i.index.toLong())
            checkBigNode(bigNode, actualHeapSize = size)
        }

        // Add cells
        val objects2 = List(size.toInt()) { LxmObject(memory) }
        checkBigNode(bigNode, actualHeapSize = size)
    }

    @Test
    fun `test heap alloc and free recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode

        checkBigNode(oldBigNode)

        // Add cells
        val oldObjects = List(size.toInt()) { LxmObject(memory) }
        checkBigNode(oldBigNode, actualHeapSize = size)

        memory.freezeCopy()

        // Add new bigNode
        val newBigNode = memory.lastNode

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)

        // Free two cells
        newBigNode.free(size - 1)
        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)

        newBigNode.free(size - 2)

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)

        // Add cells
        val newObjects = List(size.toInt()) { LxmList(memory) }
        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = 2 * size - 2)
        checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)

        // Get cells
        for (i in oldObjects.withIndex().take(2)) {
            val cell = newBigNode.getCell(memory, i.index.toLong(), toWrite = false)

            Assertions.assertEquals(cell, i.value, "The cell is incorrect")
            checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = 2 * size - 2)
            checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)
        }

        for (i in newObjects.withIndex()) {
            val cell = newBigNode.getCell(memory, i.index.toLong() + size - 2, toWrite = false)

            Assertions.assertEquals(cell, i.value, "The cell is incorrect")
            checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = 2 * size - 2)
            checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)
        }

        // Free all cells
        for (i in 0 until 2 * size - 2) {
            newBigNode.free(i)
        }

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = 2 * size - 2)
        checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)

        // Get all cells from old
        for (i in oldObjects.withIndex()) {
            val cell = oldBigNode.getCell(memory, i.index.toLong())

            Assertions.assertEquals(cell, i.value, "The cell is incorrect")
            checkBigNode(oldBigNode, nextNode = newBigNode, actualHeapSize = size)
        }
    }

    @Test
    fun `test destroy`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode

        checkBigNode(bigNode)

        // Add cells
        val objects = List(size.toInt()) { LxmObject(memory) }
        checkBigNode(bigNode, actualHeapSize = size)

        bigNode.addToStack("a", LxmString.Nil)
        bigNode.addToStack("b", LxmString.Nil)
        bigNode.addToStack("a", LxmString.Nil)

        checkBigNode(bigNode, actualStackLevelSize = 2, actualStackSize = 3, actualHeapSize = size)

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
        val oldObjects = List(size.toInt()) { LxmObject(memory) }
        checkBigNode(oldBigNode, actualHeapSize = size)

        oldBigNode.addToStack(stackName, LxmString.Nil)

        checkBigNode(oldBigNode, actualStackLevelSize = 1, actualStackSize = 1, actualHeapSize = size)

        // Add new bigNode
        memory.freezeCopy()
        val newBigNode = memory.lastNode

        checkBigNode(newBigNode, prevNode = oldBigNode, actualStackLevelSize = 1, actualStackSize = 1,
                actualHeapSize = size)
        checkBigNode(oldBigNode, nextNode = newBigNode, actualStackLevelSize = 1, actualStackSize = 1,
                actualHeapSize = size)

        // Add cells and stack
        val newObjects = List(size.toInt()) { LxmList(memory) }
        checkBigNode(newBigNode, prevNode = oldBigNode, actualStackLevelSize = 1, actualStackSize = 1,
                actualHeapSize = 2 * size)
        checkBigNode(oldBigNode, nextNode = newBigNode, actualStackLevelSize = 1, actualStackSize = 1,
                actualHeapSize = size)

        newBigNode.addToStack(stackName, LxmString.Nil)

        checkBigNode(newBigNode, prevNode = oldBigNode, actualStackLevelSize = 2, actualStackSize = 2,
                actualHeapSize = 2 * size)
        checkBigNode(oldBigNode, nextNode = newBigNode, actualStackLevelSize = 1, actualStackSize = 1,
                actualHeapSize = size)

        // Remove all elements
        newBigNode.destroy()
        oldBigNode.nextNode = null

        checkBigNode(newBigNode)
        checkBigNode(oldBigNode, actualStackLevelSize = 1, actualStackSize = 1, actualHeapSize = size)
    }
}
