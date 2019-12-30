package org.lexem.angmar.analyzer.memory

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.config.*
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

        Assertions.assertEquals(0, memory.lastNode.actualStackSize, "The stack is not empty")
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

        Assertions.assertEquals(0, memory.lastNode.actualStackSize, "The stack is not empty")
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

        Assertions.assertEquals(0, memory.lastNode.actualStackSize, "The stack is not empty")
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

        Assertions.assertEquals(0, memory.lastNode.actualStackSize, "The stack is not empty")
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

        Assertions.assertEquals(0, memory.lastNode.actualStackSize, "The stack is not empty")
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

        Assertions.assertEquals(0, memory.lastNode.actualStackSize, "The stack is not empty")
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
        memory.freezeCopy()
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

        for (i in 0..0 + size) {
            val obj = LxmObject(memory)
            val res = memory.get(obj.getPrimitive(), toWrite = false)
            Assertions.assertEquals(i, obj.getPrimitive().position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }

        for (i in 0..0 + size) {
            memory.remove(LxmReference(i))
            val res = memory.get(LxmReference(i), toWrite = false)
            Assertions.assertNull(res, "The value of the memory is incorrect")
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

        memory.freezeCopy()

        for (i in size + 1..2 * size) {
            val obj1 = LxmObject(memory)
            val res = memory.get(obj1.getPrimitive(), toWrite = false)
            Assertions.assertEquals(i, obj1.getPrimitive().position, "The position is incorrect")
            Assertions.assertEquals(obj1, res, "The value of the memory is incorrect")
        }

        // Remove lasts in the second
        for (i in size + 1..2 * size) {
            memory.remove(LxmReference(i))
            val res = memory.get(LxmReference(i), toWrite = false)
            Assertions.assertNull(res, "The value of the memory is incorrect")
        }

        // Remove firsts in the second
        for (i in 0..size) {
            memory.remove(LxmReference(i))
            val res = memory.get(LxmReference(i), toWrite = false)
            Assertions.assertNull(res, "The value of the memory must be null")
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

        memory.freezeCopy()

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

        memory.freezeCopy()
        memory.lastNode.isRecoverable = false

        memory.freezeCopy()

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
        val object1 = LxmObject(memory)

        val bigNode1 = memory.freezeCopy()

        val object2 = LxmObject(memory)

        memory.freezeCopy()

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

        memory.freezeCopy()

        // New big node.
        val object2 = LxmObject(memory)

        Assertions.assertEquals(object1.getPrimitive().position, object2.getPrimitive().position,
                "The position property is incorrect")
    }

    @Test
    fun `test collapseTo`() {
        val memory = TestUtils.generateTestMemory()
        val bigNode1 = memory.lastNode

        // Add values to heap.
        val obj0 = LxmObject(memory)
        val obj1 = LxmList(memory)
        val obj2 = LxmObject(memory)

        memory.freezeCopy()
        val bigNode2 = memory.lastNode


        // Add values to heap.
        obj1.getPrimitive().dereference(memory, toWrite = true)
        val obj3 = LxmObject(memory)
        val obj4 = LxmObject(memory)
        val obj5 = LxmObject(memory)

        memory.remove(obj4.getPrimitive())

        memory.freezeCopy()
        val bigNode3 = memory.lastNode


        // Collapse
        memory.collapseTo(bigNode1)
        val bigNode4 = memory.lastNode


        Assertions.assertNotEquals(bigNode4, bigNode3, "bigNode3 and bigNode4 cannot be equals")
        Assertions.assertTrue(bigNode4.isRecoverable, "The bigNode4 isRecoverable property is incorrect")
        Assertions.assertFalse(bigNode3.isRecoverable, "The bigNode3 isRecoverable property is incorrect")
        Assertions.assertFalse(bigNode2.isRecoverable, "The bigNode2 isRecoverable property is incorrect")
        Assertions.assertFalse(bigNode1.isRecoverable, "The bigNode1 isRecoverable property is incorrect")

        val totalCount = bigNode1.heapSize + bigNode2.heapSize + bigNode3.heapSize
        Assertions.assertEquals(totalCount, bigNode4.temporalGarbageCollectorCount,
                "The bigNode4 temporalGarbageCollectorCount property is incorrect")
        Assertions.assertEquals(0, bigNode3.temporalGarbageCollectorCount,
                "The bigNode3 temporalGarbageCollectorCount property is incorrect")
        Assertions.assertEquals(0, bigNode2.temporalGarbageCollectorCount,
                "The bigNode2 temporalGarbageCollectorCount property is incorrect")
        Assertions.assertEquals(0, bigNode1.temporalGarbageCollectorCount,
                "The temporalGarbageCollectorCount isRecoverable property is incorrect")
    }


    @Test
    fun `test collapseTo - nested`() {
        val memory = TestUtils.generateTestMemory()
        val bigNode1 = memory.lastNode

        // Add values to heap.
        val obj0 = LxmObject(memory)
        val obj1 = LxmList(memory)
        val obj2 = LxmObject(memory)

        memory.freezeCopy()
        val bigNode2 = memory.lastNode


        // Add values to heap.
        obj1.getPrimitive().dereference(memory, toWrite = true)
        val obj3 = LxmObject(memory)
        val obj4 = LxmObject(memory)
        val obj5 = LxmObject(memory)

        memory.remove(obj4.getPrimitive())

        memory.freezeCopy()
        val bigNode3 = memory.lastNode


        // Collapse
        memory.collapseTo(bigNode2)
        val bigNode4 = memory.lastNode
        val obj6 = LxmObject(memory)
        val obj7 = LxmObject(memory)


        // Collapse
        memory.collapseTo(bigNode1)
        val bigNode5 = memory.lastNode

        Assertions.assertNotEquals(bigNode5, bigNode4, "bigNode3 and bigNode5 cannot be equals")
        Assertions.assertTrue(bigNode5.isRecoverable, "The bigNode5 isRecoverable property is incorrect")
        Assertions.assertFalse(bigNode4.isRecoverable, "The bigNode4 isRecoverable property is incorrect")
        Assertions.assertFalse(bigNode3.isRecoverable, "The bigNode3 isRecoverable property is incorrect")
        Assertions.assertFalse(bigNode2.isRecoverable, "The bigNode2 isRecoverable property is incorrect")
        Assertions.assertFalse(bigNode1.isRecoverable, "The bigNode1 isRecoverable property is incorrect")

        val totalCountFor4 = bigNode2.heapSize + bigNode3.heapSize
        val totalCountFor5 = bigNode1.heapSize + totalCountFor4 + bigNode4.heapSize
        Assertions.assertEquals(totalCountFor5, bigNode5.temporalGarbageCollectorCount,
                "The bigNode5 temporalGarbageCollectorCount property is incorrect")
        Assertions.assertEquals(totalCountFor4, bigNode4.temporalGarbageCollectorCount,
                "The bigNode4 temporalGarbageCollectorCount property is incorrect")
        Assertions.assertEquals(0, bigNode3.temporalGarbageCollectorCount,
                "The bigNode3 temporalGarbageCollectorCount property is incorrect")
        Assertions.assertEquals(0, bigNode2.temporalGarbageCollectorCount,
                "The bigNode2 temporalGarbageCollectorCount property is incorrect")
        Assertions.assertEquals(0, bigNode1.temporalGarbageCollectorCount,
                "The temporalGarbageCollectorCount isRecoverable property is incorrect")
    }

    @Test
    fun `test spatial garbage collector - forced`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val initialSize = memory.lastNode.actualHeapSize

        val object1 = LxmObject(memory)
        val object2 = LxmObject(memory)
        val object3 = LxmObject(memory)
        val object4 = LxmObject(memory)

        // Previous big node.

        object1.setProperty( "a", object2.getPrimitive())
        object2.setProperty( "a", object1.getPrimitive())
        memory.remove(object3.getPrimitive())
        object4.setProperty( "b", object1.getPrimitive())
        object4.setProperty( "c", object2.getPrimitive())

        memory.freezeCopy()

        // New big node.
        val object5 = LxmObject(memory)
        val object6 = LxmObject(memory)
        object5.setProperty( "d", object5.getPrimitive())

        val object1_2 = object1.getPrimitive().dereferenceAs<LxmObject>(memory, toWrite = true)!!
        object1_2.setProperty( "e", object6.getPrimitive())

        Assertions.assertEquals(5 + initialSize, memory.lastNode.actualUsedCellCount,
                "The actualUsedCellCount property is incorrect")

        memory.spatialGarbageCollect()

        Assertions.assertEquals(initialSize, memory.lastNode.actualUsedCellCount,
                "The actualUsedCellCount property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object1.getPrimitive().position).isFreed,
                "The cell[0] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object2.getPrimitive().position).isFreed,
                "The cell[1] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object3.getPrimitive().position).isFreed,
                "The cell[2] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object4.getPrimitive().position).isFreed,
                "The cell[3] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object5.getPrimitive().position).isFreed,
                "The cell[4] property is incorrect")
    }

    @Test
    fun `test spatial garbage collector - not forced - not calling`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val initialSize = memory.lastNode.actualHeapSize

        val object1 = LxmObject(memory)
        val object2 = LxmObject(memory)
        val object3 = LxmObject(memory)
        val object4 = LxmObject(memory)

        // Previous big node.

        object1.setProperty( "a", object2.getPrimitive())
        object2.setProperty( "a", object1.getPrimitive())
        memory.remove(object3.getPrimitive())
        object4.setProperty( "b", object1.getPrimitive())
        object4.setProperty( "c", object2.getPrimitive())

        memory.freezeCopy()

        // New big node.
        val object5 = LxmObject(memory)
        val object6 = LxmObject(memory)
        object5.setProperty( "d", object5.getPrimitive())

        val object1_2 = object1.getPrimitive().dereferenceAs<LxmObject>(memory, toWrite = true)!!
        object1_2.setProperty( "e", object6.getPrimitive())

        Assertions.assertEquals(5 + initialSize, memory.lastNode.actualUsedCellCount,
                "The actualUsedCellCount property is incorrect")

        memory.spatialGarbageCollect()

        Assertions.assertEquals(5 + initialSize, memory.lastNode.actualUsedCellCount,
                "The actualUsedCellCount property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object1.getPrimitive().position).isFreed,
                "The cell[0] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object2.getPrimitive().position).isFreed,
                "The cell[1] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object3.getPrimitive().position).isFreed,
                "The cell[2] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object4.getPrimitive().position).isFreed,
                "The cell[3] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object5.getPrimitive().position).isFreed,
                "The cell[4] property is incorrect")
    }

    @Test
    fun `test spatial garbage collector - not forced - calling`() {
        val memory = TestUtils.generateTestMemory()
        val initialGarbageThreshold = memory.lastNode.garbageThreshold
        var prev = LxmObject(memory)

        val limit = (memory.lastNode.garbageThreshold * 0.9).toInt()
        for (i in 0 until limit) {
            val obj = LxmObject(memory)
            prev.setProperty( "next", obj)
            prev = obj
        }

        Assertions.assertEquals(limit + 1, memory.lastNode.actualUsedCellCount,
                "The actualUsedCellCount property is incorrect")
        Assertions.assertEquals(initialGarbageThreshold, memory.lastNode.garbageThreshold,
                "The garbageThreshold property is incorrect")

        memory.spatialGarbageCollect()

        Assertions.assertEquals(limit + 1, memory.lastNode.actualUsedCellCount,
                "The actualUsedCellCount property is incorrect")
        Assertions.assertEquals(
                (initialGarbageThreshold * Consts.Memory.spatialGarbageCollectorThresholdIncrement).toInt(),
                memory.lastNode.garbageThreshold, "The garbageThreshold property is incorrect")
    }

    @Test
    fun `test spatial garbage collector - stack`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val initialSize = memory.lastNode.actualHeapSize
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = true)

        val object0 = LxmObject(memory)
        val object1 = LxmObject(memory)
        val object2 = LxmObject(memory)
        var object3 = LxmObject(memory)

        // Previous big node.

        context.setProperty( "ori", object0.getPrimitive())
        object1.setProperty( "a", object2.getPrimitive())

        memory.addToStackAsLast(object1.getPrimitive())
        memory.addToStackAsLast(object3.getPrimitive())


        memory.freezeCopy()


        object3 = object3.getPrimitive().dereferenceAs(memory, toWrite = true)!!

        // New big node.
        val object4 = LxmObject(memory)
        val object5 = LxmObject(memory)
        object3.setProperty( "d", object4.getPrimitive())
        object4.setProperty( "d", object3.getPrimitive())

        memory.addToStackAsLast(object5.getPrimitive())
        memory.removeLastFromStack()
        memory.removeLastFromStack()


        memory.spatialGarbageCollect()


        Assertions.assertEquals(3 + initialSize, memory.lastNode.actualUsedCellCount,
                "The actualUsedCellCount property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object0.getPrimitive().position).isFreed,
                "The cell[0] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object1.getPrimitive().position).isFreed,
                "The cell[1] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getHeapCell(object2.getPrimitive().position).isFreed,
                "The cell[2] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object3.getPrimitive().position).isFreed,
                "The cell[3] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getHeapCell(object4.getPrimitive().position).isFreed,
                "The cell[4] property is incorrect")
    }

    @Test
    fun `test temporalGarbageCollect - two groups with gap`() {
        val memory = TestUtils.generateTestMemory()
        val bigNode1 = memory.lastNode

        // Add values to heap.
        val obj0 = LxmObject(memory)
        val obj1 = LxmList(memory)
        val obj2 = LxmObject(memory)

        memory.freezeCopy()
        val bigNode2 = memory.lastNode


        // Add values to heap.
        obj1.getPrimitive().dereference(memory, toWrite = true)
        val obj3 = LxmObject(memory)
        val obj4 = LxmObject(memory)
        val obj5 = LxmObject(memory)

        memory.remove(obj4.getPrimitive())

        // Collapse
        memory.collapseTo(bigNode1)
        val bigNode3 = memory.lastNode


        // Add values to heap.
        memory.freezeCopy()
        val bigNode4 = memory.lastNode
        val obj6 = LxmObject(memory)
        val obj7 = LxmObject(memory)


        // Add values to heap.
        memory.freezeCopy()
        val bigNode5 = memory.lastNode
        val obj8 = LxmObject(memory)
        val obj9 = LxmObject(memory)

        // Collapse
        memory.collapseTo(bigNode4)
        val bigNode6 = memory.lastNode

        memory.temporalGarbageCollect()

        // Check isRecoverable and count.
        var count = 0
        var node: BigNode? = memory.lastNode
        while (node != null) {
            Assertions.assertTrue(node.isRecoverable, "The node[$count] isRecoverable property is incorrect")
            Assertions.assertEquals(0, node.temporalGarbageCollectorCount,
                    "The node[$count] temporalGarbageCollectorCount property is incorrect")
            count += 1

            when (count) {
                1 -> {
                    Assertions.assertEquals(4, node.heapSize, "The heapSize property is incorrect")
                }
                2 -> {
                    Assertions.assertEquals(6, node.heapSize, "The heapSize property is incorrect")
                }
                else -> {
                    Assertions.assertEquals(0, node.heapSize, "The heapSize property is incorrect")
                }
            }

            node = node.previousNode
        }
        Assertions.assertEquals(3, count, "The number of bigNodes is incorrect")

    }
}
