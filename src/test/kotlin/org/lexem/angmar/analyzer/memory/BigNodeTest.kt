package org.lexem.angmar.analyzer.memory

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.utils.*

internal class BigNodeTest {
    companion object {
        private const val size = 4

        // AUX METHODS --------------------------------------------------------

        // Checks the status of a BigNode.
        private fun checkBigNode(bigNode: BigNode, prevNode: BigNode? = null, stackSize: Int = 0,
                actualStackSize: Int = 0, heapSize: Int = 0, actualHeapSize: Int = 0, actualUsedCellCount: Int? = null,
                lastFreePosition: Int? = null) {
            Assertions.assertEquals(stackSize, bigNode.stackSize, "The stackSize property is incorrect")
            Assertions.assertEquals(actualStackSize, bigNode.actualStackSize,
                    "The actualStackSize property is incorrect")

            Assertions.assertEquals(heapSize, bigNode.heapSize, "The heapSize property is incorrect")
            Assertions.assertEquals(actualHeapSize, bigNode.actualHeapSize, "The actualHeapSize property is incorrect")
            Assertions.assertEquals(actualUsedCellCount ?: actualHeapSize, bigNode.actualUsedCellCount,
                    "The lastFreePosition property is incorrect")
            Assertions.assertEquals(lastFreePosition ?: actualHeapSize, bigNode.lastFreePosition,
                    "The lastFreePosition property is incorrect")

            Assertions.assertEquals(prevNode, bigNode.previousNode, "The previousNode is incorrect")
        }
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `test constructors`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        checkBigNode(bigNode)

        // Modify the first BigNode
        val lastFreePosition = let {
            bigNode.alloc(memory, LxmObject.Empty)

            val cell = bigNode.alloc(memory, LxmObject.Empty)
            bigNode.alloc(memory, LxmObject.Empty)
            bigNode.free(memory, cell.position)

            bigNode.pushStack(LxmNil)

            cell.position
        }

        val bigNode2 = BigNode(bigNode)
        bigNode2.pushStack(LxmNil)

        checkBigNode(bigNode2, prevNode = bigNode, actualStackSize = 2, stackSize = 1,
                lastFreePosition = lastFreePosition, actualHeapSize = 3, heapSize = 0, actualUsedCellCount = 2)
    }

    @Test
    fun `test stack`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size) { LxmString.from(it.toString()) }

        // Push some
        for (obj in objects.withIndex()) {
            bigNode.pushStack(obj.value)
            checkBigNode(bigNode, actualStackSize = obj.index + 1, stackSize = obj.index + 1)
        }

        // Pop some
        for (obj in objects.withIndex().reversed()) {
            val stackObj = bigNode.popStack()

            Assertions.assertEquals(obj.value, stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNode, actualStackSize = obj.index, stackSize = obj.index)
        }
    }

    @Test
    fun `test stack recursively`() {
        val bigNodeOld = BigNode(null)
        val objectsOld = List(size) { LxmString.from("old$it") }

        // Push some in old
        for (obj in objectsOld.withIndex()) {
            bigNodeOld.pushStack(obj.value)
            checkBigNode(bigNodeOld, actualStackSize = obj.index + 1, stackSize = obj.index + 1)
        }

        // Add new bigNode
        val bigNodeNew = BigNode(bigNodeOld)
        val objectsNew = List(size) { LxmString.from("new$it") }

        // Remove one
        var stackObj = bigNodeNew.popStack()

        Assertions.assertEquals(objectsOld.last(), stackObj, "The value of the stack is incorrect")
        checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackSize = size - 1, stackSize = 0)
        checkBigNode(bigNodeOld, actualStackSize = size, stackSize = size)

        // Push some in new
        for (obj in objectsNew.withIndex()) {
            bigNodeNew.pushStack(obj.value)
            checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackSize = obj.index + size,
                    stackSize = obj.index + 1)
            checkBigNode(bigNodeOld, actualStackSize = size, stackSize = size)
        }

        // Pop all out of new
        for (i in size - 1 downTo 0) {
            stackObj = bigNodeNew.popStack()

            Assertions.assertEquals(objectsNew[i], stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackSize = i + size - 1, stackSize = i)
            checkBigNode(bigNodeOld, actualStackSize = size, stackSize = size)
        }

        for (i in size - 2 downTo 0) {
            stackObj = bigNodeNew.popStack()

            Assertions.assertEquals(objectsOld[i], stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNodeNew, prevNode = bigNodeOld, actualStackSize = i, stackSize = 0)
            checkBigNode(bigNodeOld, actualStackSize = size, stackSize = size)
        }

        // Pop all out of old
        for (i in size - 1 downTo 0) {
            stackObj = bigNodeOld.popStack()

            Assertions.assertEquals(objectsOld[i], stackObj, "The value of the stack is incorrect")
            checkBigNode(bigNodeOld, actualStackSize = i, stackSize = i)
        }
    }

    @Test
    @Incorrect
    fun `test pop from empty stack`() {
        TestUtils.assertAnalyzerException {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.popStack()
        }
    }

    @Test
    @Incorrect
    fun `test pop not found element from stack`() {
        TestUtils.assertAnalyzerException {
            val bigNodeOld = BigNode(null)
            bigNodeOld.pushStack(LxmString.from("test"))

            val bigNodeNew = BigNode(bigNodeOld)

            bigNodeOld.popStack()
            bigNodeNew.popStack()
        }
    }

    @Test
    fun `test heap alloc, get and set`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size) { LxmObject() }

        checkBigNode(bigNode)

        // Add cells
        for (i in objects.withIndex()) {
            bigNode.alloc(memory, i.value)

            checkBigNode(bigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        // Get all cells
        for (i in objects.withIndex()) {
            val cell = bigNode.getCell(i.index)

            BigNodeCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(bigNode, heapSize = size, actualHeapSize = size)
        }

        // Set cells
        val newObjects = List(size) { LxmList() }
        for (i in newObjects.withIndex()) {
            bigNode.setCell(i.index, i.value)

            checkBigNode(bigNode, heapSize = size, actualHeapSize = size)

            val cell = bigNode.getCell(i.index)

            BigNodeCellTest.checkCell(cell, i.index, i.value)
        }
    }

    @Test
    fun `test heap alloc, get and set recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode
        val oldObjects = List(size) { LxmObject.Empty }

        checkBigNode(oldBigNode)

        // Add cells
        for (i in oldObjects.withIndex()) {
            oldBigNode.alloc(memory, i.value)

            checkBigNode(oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        // Add new bigNode
        val newBigNode = BigNode(oldBigNode)
        val newObjects = List(size) { LxmList() }

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size)
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        // Add cells
        for (i in newObjects.withIndex()) {
            newBigNode.alloc(memory, i.value)

            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1 + size)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)
        }

        // Get cells
        for (i in oldObjects.withIndex()) {
            val cell = newBigNode.getCell(i.index)

            BigNodeCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size + i.index + 1, actualHeapSize = 2 * size)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)
        }

        for (i in newObjects.withIndex()) {
            val cell = newBigNode.getCell(i.index + size)

            BigNodeCellTest.checkCell(cell, i.index + size, i.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size, actualHeapSize = 2 * size)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)
        }

        // Set cells
        val newObjects2 = List(size) { LxmList() }
        for (i in newObjects2.withIndex()) {
            newBigNode.setCell(i.index + size - 2, i.value)

            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size, actualHeapSize = 2 * size)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

            val cell = newBigNode.getCell(i.index + size - 2)

            BigNodeCellTest.checkCell(cell, i.index + size - 2, i.value)
        }

        // Get all cells from old
        for (i in oldObjects.withIndex()) {
            val cell = oldBigNode.getCell(i.index)

            BigNodeCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)
        }
    }

    @Test
    @Incorrect
    fun `test get forbidden cell`() {
        TestUtils.assertAnalyzerException {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.getCell(55)
        }
    }

    @Test
    @Incorrect
    fun `test set forbidden cell`() {
        TestUtils.assertAnalyzerException {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            bigNode.setCell(55, LxmObject.Empty)
        }
    }

    @Test
    fun `test heap alloc and free`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size) { LxmObject() }

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
        val oldObjects = List(size) { LxmObject.Empty }

        checkBigNode(oldBigNode)

        // Add cells
        for (i in oldObjects.withIndex()) {
            oldBigNode.alloc(memory, i.value)

            checkBigNode(oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        // Add new bigNode
        val newBigNode = BigNode(oldBigNode)
        val newObjects = List(size) { LxmList() }

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size)
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        // Free two cells
        newBigNode.free(memory, size - 1)
        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 1, actualHeapSize = size,
                actualUsedCellCount = size - 1, lastFreePosition = size - 1)
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        newBigNode.free(memory, size - 2)

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2, actualHeapSize = size,
                actualUsedCellCount = size - 2, lastFreePosition = size - 2)
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        // Add cells
        for (i in newObjects.withIndex().take(2)) {
            newBigNode.alloc(memory, i.value)

            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2, actualHeapSize = size,
                    actualUsedCellCount = size - 1 + i.index, lastFreePosition = i.index + 1 + size - 2)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)
        }

        for (i in newObjects.withIndex().drop(2)) {
            newBigNode.alloc(memory, i.value)

            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index - 1 + size)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)
        }

        // Get cells
        for (i in oldObjects.withIndex().take(2)) {
            val cell = newBigNode.getCell(i.index)

            BigNodeCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size + i.index + 1,
                    actualHeapSize = 2 * size - 2)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)
        }

        for (i in newObjects.withIndex()) {
            val cell = newBigNode.getCell(i.index + size - 2)

            BigNodeCellTest.checkCell(cell, i.index + size - 2, i.value)
            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size - 2, actualHeapSize = 2 * size - 2)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)
        }

        // Free all cells
        for (i in 0 until 2 * size - 2) {
            newBigNode.free(memory, i)
        }

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = 2 * size - 2, actualHeapSize = 2 * size - 2,
                actualUsedCellCount = 0, lastFreePosition = 2 * size - 3)
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)

        // Get all cells from old
        for (i in oldObjects.withIndex()) {
            val cell = oldBigNode.getCell(i.index)

            BigNodeCellTest.checkCell(cell, i.index, i.value)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size)
        }
    }

    @Test
    fun `test destroy`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val objects = List(size) { LxmObject() }

        checkBigNode(bigNode)

        // Add cells
        for (i in objects.withIndex()) {
            bigNode.alloc(memory, i.value)

            checkBigNode(bigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        bigNode.pushStack(LxmString.Nil)

        checkBigNode(bigNode, heapSize = size, actualHeapSize = size, stackSize = 1, actualStackSize = 1)

        // Remove all elements
        bigNode.destroy()

        checkBigNode(bigNode)
    }

    @Test
    fun `test destroy recursively`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode
        val oldObjects = List(size) { LxmObject() }

        checkBigNode(oldBigNode)

        // Add cells and stack
        for (i in oldObjects.withIndex()) {
            oldBigNode.alloc(memory, i.value)

            checkBigNode(oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1)
        }

        oldBigNode.pushStack(LxmString.Nil)

        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size, stackSize = 1, actualStackSize = 1)

        // Add new bigNode
        val newBigNode = BigNode(oldBigNode)
        val newObjects = List(size) { LxmList() }

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size, actualStackSize = 1)
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size, stackSize = 1, actualStackSize = 1)

        // Add cells and stack
        for (i in newObjects.withIndex()) {
            newBigNode.alloc(memory, i.value)

            checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = i.index + 1, actualHeapSize = i.index + 1 + size,
                    actualStackSize = 1)
            checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size, stackSize = 1, actualStackSize = 1)
        }

        newBigNode.pushStack(LxmString.Nil)

        checkBigNode(newBigNode, prevNode = oldBigNode, heapSize = size, actualHeapSize = 2 * size, stackSize = 1,
                actualStackSize = 2)
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size, stackSize = 1, actualStackSize = 1)

        // Remove all elements
        newBigNode.destroy()

        checkBigNode(newBigNode, prevNode = oldBigNode, actualHeapSize = size, actualStackSize = 1)
        checkBigNode(oldBigNode, heapSize = size, actualHeapSize = size, stackSize = 1, actualStackSize = 1)
    }
}
