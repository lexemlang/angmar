package org.lexem.angmar.analyzer.memory

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

internal class LexemMemoryTest {
    private val size = 4

    @Test
    fun `test stack add, get and remove`() {
        val memory = TestUtils.generateTestMemory()
        val objects = List(size) { LxmString.from("obj$it") }
        val stackName = "last"

        // Add some
        for (obj in objects) {
            memory.addToStack(stackName, obj)
        }

        // Remove some
        for (obj in objects.reversed()) {
            val stackObj = memory.getFromStack(stackName)
            Assertions.assertEquals(obj, stackObj, "The value of the stack does not match")

            memory.removeFromStack(stackName)
        }

        Assertions.assertEquals(0, memory.lastNode.stackSize, "The stack is not empty")
    }

    @Test
    fun `test stack rename`() {
        val memory = TestUtils.generateTestMemory()
        val obj1 = LxmObject(memory)
        val oldName = "last"
        val newName = "test"

        memory.addToStack(oldName, obj1)
        memory.renameStackCell(oldName, newName)

        val stackObj = memory.getFromStack(newName)
        Assertions.assertEquals(obj1.getPrimitive(), stackObj, "The value of the stack does not match")

        try {
            memory.getFromStack(oldName)
            throw Error("The $oldName value cannot exist.")
        } catch (e: AngmarAnalyzerException) {
        }

        // Check the object is ok.
        val finalObj = stackObj.dereference(memory, toWrite = false)
        Assertions.assertEquals(obj1, finalObj, "The object is incorrect")

        memory.removeFromStack(newName)

        Assertions.assertEquals(0, memory.lastNode.stackSize, "The stack is not empty")
    }

    @Test
    fun `test stack replace`() {
        val memory = TestUtils.generateTestMemory()
        val obj1 = LxmInteger.Num1
        val obj2 = LxmInteger.Num2
        val stackName = "last"

        memory.addToStack(stackName, obj1)
        memory.replaceStackCell(stackName, obj2)

        val stackObj = memory.getFromStack(stackName)
        Assertions.assertEquals(obj2, stackObj, "The value of the stack does not match")

        memory.removeFromStack(stackName)

        Assertions.assertEquals(0, memory.lastNode.stackSize, "The stack is not empty")
    }

    @Test
    fun `test stack add, get and remove using last`() {
        val memory = TestUtils.generateTestMemory()
        val objects = List(size) { LxmString.from("obj$it") }

        // Add some
        for (obj in objects) {
            memory.addToStackAsLast(obj)
        }

        // Remove some
        for (obj in objects.reversed()) {
            val stackObj = memory.getLastFromStack()
            Assertions.assertEquals(obj, stackObj, "The value of the stack does not match")

            memory.removeLastFromStack()
        }

        Assertions.assertEquals(0, memory.lastNode.stackSize, "The stack is not empty")
    }

    @Test
    fun `test stack rename using last`() {
        val memory = TestUtils.generateTestMemory()
        val obj = LxmInteger.Num0
        val newName = "test"

        memory.addToStackAsLast(obj)
        memory.renameLastStackCell(newName)

        var stackObj = memory.getFromStack(newName)
        Assertions.assertEquals(obj, stackObj, "The value of the stack does not match")

        try {
            memory.getLastFromStack()
            throw Error("The ${AnalyzerCommons.Identifiers.Last} value cannot exist.")
        } catch (e: AngmarAnalyzerException) {
        }

        memory.renameStackCellToLast(newName)

        stackObj = memory.getLastFromStack()
        Assertions.assertEquals(obj, stackObj, "The value of the stack does not match")

        try {
            memory.getFromStack(newName)
            throw Error("The ${AnalyzerCommons.Identifiers.Last} value cannot exist.")
        } catch (e: AngmarAnalyzerException) {
        }

        memory.removeLastFromStack()

        Assertions.assertEquals(0, memory.lastNode.stackSize, "The stack is not empty")
    }

    @Test
    fun `test stack replace using last`() {
        val memory = TestUtils.generateTestMemory()
        val obj1 = LxmInteger.Num1
        val obj2 = LxmInteger.Num2

        memory.addToStackAsLast(obj1)
        memory.replaceLastStackCell(obj2)

        val stackObj = memory.getLastFromStack()
        Assertions.assertEquals(obj2, stackObj, "The value of the stack does not match")

        memory.removeLastFromStack()

        Assertions.assertEquals(0, memory.lastNode.stackSize, "The stack is not empty")
    }

    @Test
    fun `test stack recursively`() {
        val memory = TestUtils.generateTestMemory()
        val objects1 = List(size) { LxmString.from("objA$it") }
        val objects2 = List(size) { LxmString.from("objB$it") }

        // Add some in the first copy
        for (obj in objects1) {
            memory.addToStackAsLast(obj)
        }

        // Add some in the second copy
        TestUtils.freezeCopy(memory)
        for (obj in objects2) {
            memory.addToStackAsLast(obj)
        }

        // Remove all out of the second copy
        for (obj in (objects1 + objects2).reversed()) {
            val stackObj = memory.getLastFromStack()
            Assertions.assertEquals(obj, stackObj, "The value of the stack does not match")

            memory.removeLastFromStack()
        }

        // Remove all out of the first
        memory.rollbackCopy()
        for (obj in objects1.reversed()) {
            val stackObj = memory.getLastFromStack()
            Assertions.assertEquals(obj, stackObj, "The value of the stack does not match")

            memory.removeLastFromStack()
        }
    }

    @Test
    fun `test get`() {
        val memory = TestUtils.generateTestMemory()
        val emptyObject = LxmObject(memory)
        val emptyList = LxmList(memory)

        Assertions.assertEquals(emptyObject, memory.get(emptyObject.getPrimitive(), toWrite = false),
                "The value of the memory is incorrect")
        Assertions.assertEquals(emptyList, memory.get(emptyList.getPrimitive(), toWrite = false),
                "The value of the memory is incorrect")
    }

    @Test
    fun `test add and remove`() {
        val memory = TestUtils.generateTestMemory()

        for (i in 0..size) {
            val obj = LxmObject(memory)
            val res = memory.get(obj.getPrimitive(), toWrite = false)
            Assertions.assertEquals(i, obj.getPrimitive().position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }

        for (i in 0..size) {
            memory.remove(LxmReference(i))
            val res = memory.getCell(LxmReference(i), toWrite = false)
            Assertions.assertNull(res.getValue(toWrite = false), "The value of the memory is incorrect")
        }
    }

    @Test
    fun `test add and remove recursively`() {
        val memory = TestUtils.generateTestMemory()
        val initialObjects = mutableListOf<LxmObject>()

        for (i in 0..size) {
            val obj = LxmObject(memory)
            val res = memory.get(obj.getPrimitive(), toWrite = false)
            Assertions.assertEquals(i, obj.getPrimitive().position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")

            initialObjects.add(obj)
        }

        TestUtils.freezeCopy(memory)

        for (i in size + 1..2 * size) {
            val obj1 = LxmObject(memory)
            val res = memory.get(obj1.getPrimitive(), toWrite = false)
            Assertions.assertEquals(i, obj1.getPrimitive().position, "The position is incorrect")
            Assertions.assertEquals(obj1, res, "The value of the memory is incorrect")
        }

        // Remove lasts in the second
        for (i in size + 1..2 * size) {
            memory.remove(LxmReference(i))
            val res = memory.getCell(LxmReference(i), toWrite = false)
            Assertions.assertNull(res.getValue(toWrite = false), "The value of the memory is incorrect")
        }

        // Remove firsts in the second
        for (i in 0..size) {
            memory.remove(LxmReference(i))
            val res = memory.getCell(LxmReference(i), toWrite = false)
            Assertions.assertNull(res.getValue(toWrite = false), "The value of the memory must be null")
        }

        // Check the old BigNode.
        memory.rollbackCopy()
        for (i in 0..size) {
            val res = memory.get(LxmReference(i), toWrite = false)
            Assertions.assertEquals(initialObjects[i], res, "The value of the memory is incorrect")
        }
    }

    @Test
    fun `test clear`() {
        val memory = TestUtils.generateTestMemory()

        for (i in 0..size) {
            val obj = LxmObject(memory)
            val res = memory.get(obj.getPrimitive(), toWrite = false)
            Assertions.assertEquals(i, obj.getPrimitive().position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }

        memory.clear()

        val emptyList = LxmList(memory)
        Assertions.assertEquals(0, emptyList.getPrimitive().position, "The memory is not empty")
    }

    @Test
    fun `test clear recursively`() {
        val memory = TestUtils.generateTestMemory()

        for (i in 0..size) {
            val obj = LxmObject(memory)
            val res = memory.get(obj.getPrimitive(), toWrite = false)
            Assertions.assertEquals(i, obj.getPrimitive().position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }

        TestUtils.freezeCopy(memory)

        for (i in size + 1..2 * size) {
            val obj1 = LxmObject(memory)
            val res = memory.get(obj1.getPrimitive(), toWrite = false)
            Assertions.assertEquals(i, obj1.getPrimitive().position, "The position is incorrect")
            Assertions.assertEquals(obj1, res, "The value of the memory is incorrect")
        }

        memory.clear()

        val emptyList = LxmList(memory)
        Assertions.assertEquals(0, emptyList.getPrimitive().position, "The memory is not empty")
    }

    @Test
    fun `test freezeCopy and rollbackCopy`() {
        val memory = TestUtils.generateTestMemory()
        val object1 = LxmObject(memory)

        TestUtils.freezeCopy(memory)

        val object2 = LxmObject(memory)

        Assertions.assertNotEquals(LxmNil, object1.getPrimitive(), "The value of the memory is incorrect")
        Assertions.assertEquals(object2, memory.get(object2.getPrimitive(), toWrite = false),
                "The value of the memory is incorrect")

        memory.rollbackCopy()

        Assertions.assertEquals(object1, memory.get(object1.getPrimitive(), toWrite = false),
                "The value of the memory is incorrect")

        val emptyList = LxmList(memory)
        Assertions.assertEquals(object2.getPrimitive().position, emptyList.getPrimitive().position,
                "The memory has not rollback correctly")
    }

    @Test
    @Incorrect
    fun `test rollback first copy`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.FirstBigNodeRollback) {
            val memory = TestUtils.generateTestMemory()
            memory.rollbackCopy() // First context
            memory.rollbackCopy() // Stdlib
        }
    }

    @Test
    fun `test freezeCopy and restoreCopy`() {
        val memory = TestUtils.generateTestMemory()
        val bigNode1 = memory.lastNode

        val object1 = LxmObject(memory)

        TestUtils.freezeCopy(memory)

        val object2 = LxmObject(memory)

        TestUtils.freezeCopy(memory)

        val object3 = LxmObject(memory)

        Assertions.assertNotEquals(LxmNil, object1.getPrimitive(), "The value of the memory is incorrect")
        Assertions.assertNotEquals(LxmNil, object2.getPrimitive(), "The value of the memory is incorrect")
        Assertions.assertEquals(object3, memory.get(object3.getPrimitive(), toWrite = false),
                "The value of the memory is incorrect")

        memory.restoreCopy(bigNode1)

        Assertions.assertEquals(object1, memory.get(object1.getPrimitive(), toWrite = false),
                "The value of the memory is incorrect")

        val emptyList = LxmList(memory)
        Assertions.assertEquals(object2.getPrimitive().position, emptyList.getPrimitive().position,
                "The memory has not rollback correctly")
    }

    @Test
    fun `test shift empty cell`() {
        val memory = TestUtils.generateTestMemory()
        val object1 = LxmObject(memory)

        // Previous big node.
        memory.remove(object1.getPrimitive())

        TestUtils.freezeCopy(memory)

        // New big node.
        val object2 = LxmObject(memory)

        Assertions.assertEquals(object1.getPrimitive().position, object2.getPrimitive().position,
                "The position property is incorrect")
    }

    @Test
    fun `test collapseTo`() {
        val memory = TestUtils.generateTestMemory()
        val bigNode1 = memory.lastNode
        val bigNode0 = memory.lastNode.previousNode

        // Add values to heap.
        LxmObject(memory)
        val obj1 = LxmList(memory)
        LxmObject(memory)

        TestUtils.freezeCopy(memory)


        // Add values to heap.
        obj1.getPrimitive().dereference(memory, toWrite = true)
        LxmObject(memory)
        val obj4 = LxmObject(memory)
        LxmObject(memory)

        memory.remove(obj4.getPrimitive())

        TestUtils.freezeCopy(memory)
        val bigNode3 = memory.lastNode


        // Collapse
        memory.collapseTo(bigNode1)
        val bigNode4 = memory.lastNode

        Assertions.assertEquals(bigNode4, bigNode3, "bigNode3 and bigNode4 must be equals")
        Assertions.assertEquals(bigNode0, bigNode4.previousNode, "The previousNode property is incorrect")
        Assertions.assertEquals(bigNode4, bigNode0?.nextNode, "The previousNode property is incorrect")
    }

    @Test
    fun `test spatial garbage collector`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val initialSize = memory.lastNode.heapSize

        val object1 = LxmObject(memory)
        val object2 = LxmObject(memory)
        val object3 = LxmObject(memory)
        val object4 = LxmObject(memory)

        // Previous big node.

        object1.setProperty(memory, "a", object2.getPrimitive())
        object2.setProperty(memory, "a", object1.getPrimitive())
        memory.remove(object3.getPrimitive())
        object4.setProperty(memory, "b", object1.getPrimitive())
        object4.setProperty(memory, "c", object2.getPrimitive())

        TestUtils.freezeCopy(memory)

        // New big node.
        val object5 = LxmObject(memory)
        val object6 = LxmObject(memory)
        object5.setProperty(memory, "d", object5.getPrimitive())

        val object1_2 = object1.getPrimitive().dereferenceAs<LxmObject>(memory, toWrite = true)!!
        object1_2.setProperty(memory, "e", object6.getPrimitive())

        Assertions.assertEquals(0, memory.lastNode.heapFreedCells.get(),
                "The actualUsedCellCount property is incorrect")

        memory.spatialGarbageCollect()

        Assertions.assertEquals(5, memory.lastNode.heapFreedCells.get(),
                "The actualUsedCellCount property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object1.getPrimitive().position, toWrite = false).isFreed,
                "The cell[0] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object2.getPrimitive().position, toWrite = false).isFreed,
                "The cell[1] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object3.getPrimitive().position, toWrite = false).isFreed,
                "The cell[2] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object4.getPrimitive().position, toWrite = false).isFreed,
                "The cell[3] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object5.getPrimitive().position, toWrite = false).isFreed,
                "The cell[4] property is incorrect")
    }

    @Test
    fun `test spatial garbage collector - stack`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val initialSize = memory.lastNode.heapSize
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = true)

        val object0 = LxmObject(memory)
        val object1 = LxmObject(memory)
        val object2 = LxmObject(memory)
        var object3 = LxmObject(memory)

        // Previous big node.

        context.setProperty(memory, "ori", object0.getPrimitive())
        object1.setProperty(memory, "a", object2.getPrimitive())

        memory.addToStackAsLast(object1.getPrimitive())
        memory.addToStackAsLast(object3.getPrimitive())


        TestUtils.freezeCopy(memory)


        object3 = object3.getPrimitive().dereferenceAs(memory, toWrite = true)!!

        // New big node.
        val object4 = LxmObject(memory)
        val object5 = LxmObject(memory)
        object3.setProperty(memory, "d", object4.getPrimitive())
        object4.setProperty(memory, "d", object3.getPrimitive())

        memory.addToStackAsLast(object5.getPrimitive())
        memory.removeLastFromStack()
        memory.removeLastFromStack()


        memory.spatialGarbageCollect()


        Assertions.assertEquals(3, memory.lastNode.heapFreedCells.get(),
                "The actualUsedCellCount property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object0.getPrimitive().position, toWrite = false).isFreed,
                "The cell[0] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object1.getPrimitive().position, toWrite = false).isFreed,
                "The cell[1] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object2.getPrimitive().position, toWrite = false).isFreed,
                "The cell[2] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object3.getPrimitive().position, toWrite = false).isFreed,
                "The cell[3] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object4.getPrimitive().position, toWrite = false).isFreed,
                "The cell[4] property is incorrect")
    }
}
