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

        Assertions.assertEquals(0, memory.lastNode.actualStackSize, "The stack is not empty")
    }

    @Test
    fun `test stack rename`() {
        val memory = TestUtils.generateTestMemory()
        val obj1 = LxmObject()
        val obj1Ref = memory.add(obj1)
        val oldName = "last"
        val newName = "test"

        memory.addToStack(oldName, obj1Ref)
        memory.renameStackCell(oldName, newName)

        val stackObj = memory.getFromStack(newName)
        Assertions.assertEquals(obj1Ref, stackObj, "The value of the stack does not match")

        try {
            memory.getFromStack(oldName)
            throw Error("The $oldName value cannot exist.")
        } catch (e: AngmarAnalyzerException) {
        }

        // Check the object is ok.
        val finalObj = stackObj.dereference(memory)
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
    fun `test get and set`() {
        val memory = TestUtils.generateTestMemory()

        // Add a value to use the 0 index.
        memory.add(LxmObject.Empty)

        // Get empty
        Assertions.assertEquals(LxmObject.Empty, memory.get(LxmReference(0)), "The value of the memory is incorrect")

        // Set - get
        memory.set(LxmReference(0), LxmList.Empty)
        Assertions.assertEquals(LxmList.Empty, memory.get(LxmReference(0)), "The value of the memory is incorrect")
    }

    @Test
    fun `test add and remove`() {
        val memory = TestUtils.generateTestMemory()

        for (i in 0..0 + size) {
            val obj = LxmObject()
            val reference = memory.add(obj)
            val res = memory.get(reference)
            Assertions.assertEquals(i, reference.position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }

        for (i in 0..0 + size) {
            memory.remove(LxmReference(i))
            val res = memory.get(LxmReference(i))
            Assertions.assertEquals(BigNodeCell.EmptyCell, res, "The value of the memory is incorrect")
        }
    }

    @Test
    fun `test add and remove recursively`() {
        val memory = TestUtils.generateTestMemory()
        val obj = LxmObject()

        for (i in 0..size) {
            val reference = memory.add(obj)
            val res = memory.get(reference)
            Assertions.assertEquals(i, reference.position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }

        memory.freezeCopy()

        for (i in size + 1..2 * size) {
            val reference = memory.add(obj)
            val res = memory.get(reference)
            Assertions.assertEquals(i, reference.position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }

        // Remove lasts in the second
        for (i in size + 1..2 * size) {
            memory.remove(LxmReference(i))
            val res = memory.get(LxmReference(i))
            Assertions.assertEquals(BigNodeCell.EmptyCell, res, "The value of the memory is incorrect")
        }

        // Remove firsts in the second
        for (i in 0..size) {
            memory.remove(LxmReference(i))
            val res = memory.get(LxmReference(i))
            Assertions.assertEquals(BigNodeCell.EmptyCell, res, "The value of the memory must be null")
        }

        // Check the old BigNode.
        memory.rollbackCopy()
        for (i in 0..size) {
            val res = memory.get(LxmReference(i))
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }
    }

    @Test
    fun `test clear`() {
        val memory = TestUtils.generateTestMemory()
        val obj = LxmObject()

        for (i in 0..size) {
            val reference = memory.add(obj)
            val res = memory.get(reference)
            Assertions.assertEquals(i, reference.position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }

        memory.clear()

        val reference = memory.add(LxmList.Empty)
        Assertions.assertEquals(0, reference.position, "The memory is not empty")
    }

    @Test
    fun `test clear recursively`() {
        val memory = TestUtils.generateTestMemory()
        val obj = LxmObject()

        for (i in 0..size) {
            val reference = memory.add(obj)
            val res = memory.get(reference)
            Assertions.assertEquals(i, reference.position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }

        memory.freezeCopy()

        for (i in size + 1..2 * size) {
            val reference = memory.add(obj)
            val res = memory.get(reference)
            Assertions.assertEquals(i, reference.position, "The position is incorrect")
            Assertions.assertEquals(obj, res, "The value of the memory is incorrect")
        }

        memory.clear()

        val reference = memory.add(LxmList.Empty)
        Assertions.assertEquals(0, reference.position, "The memory is not empty")
    }

    @Test
    fun `test freezeCopy and rollbackCopy`() {
        val memory = TestUtils.generateTestMemory()
        val object1 = LxmObject()
        val object2 = LxmObject()
        val reference1 = memory.add(object1)

        memory.freezeCopy()

        val reference2 = memory.add(object2)

        Assertions.assertNotEquals(LxmNil, reference1, "The value of the memory is incorrect")
        Assertions.assertEquals(object2, memory.get(reference2), "The value of the memory is incorrect")

        memory.rollbackCopy()

        Assertions.assertEquals(object1, memory.get(reference1), "The value of the memory is incorrect")

        val reference = memory.add(LxmList.Empty)
        Assertions.assertEquals(reference2.position, reference.position, "The memory has not rollback correctly")
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
        val object1 = LxmObject()
        val object2 = LxmObject()
        val object3 = LxmObject()
        val reference1 = memory.add(object1)

        val bigNode1 = memory.freezeCopy()

        val reference2 = memory.add(object2)

        memory.freezeCopy()

        val reference3 = memory.add(object3)

        Assertions.assertNotEquals(LxmNil, reference1, "The value of the memory is incorrect")
        Assertions.assertNotEquals(LxmNil, reference2, "The value of the memory is incorrect")
        Assertions.assertEquals(object3, memory.get(reference3), "The value of the memory is incorrect")

        memory.restoreCopy(bigNode1)

        Assertions.assertEquals(object1, memory.get(reference1), "The value of the memory is incorrect")

        val reference = memory.add(LxmList.Empty)
        Assertions.assertEquals(reference2.position, reference.position, "The memory has not rollback correctly")
    }

    @Test
    fun `test shift empty cell`() {
        val memory = TestUtils.generateTestMemory()
        val object1 = LxmObject()
        val object2 = LxmObject()

        // Previous big node.
        val reference1 = memory.add(object1)
        memory.remove(reference1)

        memory.freezeCopy()

        // New big node.
        val reference2 = memory.add(object2)

        Assertions.assertEquals(reference1.position, reference2.position, "The position property is incorrect")
    }

    @Test
    fun `test collapseTo`() {
        val memory = LexemMemory()
        val oldBigNode = memory.lastNode

        // Add values to heap
        var obj0 = LxmObject()
        obj0.setProperty(memory, "x", LxmString.Empty)
        var obj1 = LxmList()
        obj1.addCell(memory, LxmInteger.Num0)
        val obj2 = LxmObject()

        val ref0 = memory.add(obj0)
        val ref1 = memory.add(obj1)
        val ref2 = memory.add(obj2)

        memory.freezeCopy()


        // Add values to heap
        obj1 = ref1.dereferenceAs(memory)!!
        obj1.addCell(memory, LxmInteger.Num10)
        var obj3 = LxmObject()
        val obj4 = LxmObject()
        val obj5 = LxmObject()

        val ref3 = memory.add(obj3)
        val ref4 = memory.add(obj4)
        val ref5 = memory.add(obj5)

        memory.remove(ref4)


        memory.freezeCopy()

        obj0 = ref0.dereferenceAs(memory)!!
        obj0.setProperty(memory, "y", ref3)
        obj3 = ref3.dereferenceAs(memory)!!
        memory.remove(ref2)
        memory.remove(ref5)

        // Collapse
        memory.collapseTo(oldBigNode)

        // The spatial garbage collector removes those elements that are not
        // referenced.
        Assertions.assertEquals(memory.get(ref0), obj0, "The heap[0] is incorrect")
        Assertions.assertEquals(memory.get(ref1), BigNodeCell.EmptyCell, "The heap[1] is incorrect")
        Assertions.assertEquals(memory.get(ref2), BigNodeCell.EmptyCell, "The heap[2] is incorrect")
        Assertions.assertEquals(memory.get(ref3), obj3, "The heap[3] is incorrect")
        Assertions.assertEquals(memory.get(ref4), BigNodeCell.EmptyCell, "The heap[4] is incorrect")
        Assertions.assertEquals(memory.get(ref5), BigNodeCell.EmptyCell, "The heap[5] is incorrect")
    }

    @Test
    fun `test spatial garbage collector`() {
        val memory = TestUtils.generateTestMemory()
        val object1 = LxmObject()
        val object2 = LxmObject()
        val object3 = LxmObject()
        val object4 = LxmObject()
        val object5 = LxmObject()
        val object6 = LxmObject()

        // Previous big node.
        val reference1 = memory.add(object1)
        val reference2 = memory.add(object2)
        val reference3 = memory.add(object3)
        val reference4 = memory.add(object4)

        object1.setProperty(memory, "a", reference2)
        object2.setProperty(memory, "a", reference1)
        memory.remove(reference3)
        object4.setProperty(memory, "b", reference1)
        object4.setProperty(memory, "c", reference2)

        memory.freezeCopy()

        // New big node.
        val reference5 = memory.add(object5)
        val reference6 = memory.add(object6)
        object5.setProperty(memory, "d", reference5)

        val object1_2 = reference1.dereferenceAs<LxmObject>(memory)!!
        object1_2.setProperty(memory, "e", reference6)

        Assertions.assertEquals(5, memory.lastNode.actualUsedCellCount, "The actualUsedCellCount property is incorrect")

        memory.spatialGarbageCollect()

        Assertions.assertEquals(3, memory.lastNode.actualUsedCellCount, "The actualUsedCellCount property is incorrect")
        Assertions.assertFalse(memory.lastNode.getCell(0).isFreed, "The cell[0] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getCell(1).isFreed, "The cell[1] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getCell(2).isFreed, "The cell[2] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getCell(3).isFreed, "The cell[3] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getCell(4).isFreed, "The cell[4] property is incorrect")
    }

    @Test
    fun `test spatial garbage collector - stack`() {
        val memory = TestUtils.generateTestMemory()
        val object0 = LxmObject()
        val object1 = LxmObject()
        val object2 = LxmObject()
        var object3 = LxmObject()
        val object4 = LxmObject()

        // Previous big node.
        val reference0 = memory.add(object0)
        val reference1 = memory.add(object1)
        val reference2 = memory.add(object2)
        val reference3 = memory.add(object3)

        object1.setProperty(memory, "a", reference2)

        memory.addToStackAsLast(reference1)
        memory.addToStackAsLast(reference3)


        memory.freezeCopy()


        object3 = reference3.dereferenceAs(memory)!!

        // New big node.
        val reference4 = memory.add(object4)
        object3.setProperty(memory, "d", reference4)
        object4.setProperty(memory, "d", reference3)

        memory.removeLastFromStack()


        memory.spatialGarbageCollect()


        Assertions.assertEquals(3, memory.lastNode.actualUsedCellCount, "The actualUsedCellCount property is incorrect")
        Assertions.assertFalse(memory.lastNode.getCell(0).isFreed, "The cell[0] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getCell(1).isFreed, "The cell[1] property is incorrect")
        Assertions.assertFalse(memory.lastNode.getCell(2).isFreed, "The cell[2] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getCell(3).isFreed, "The cell[3] property is incorrect")
        Assertions.assertTrue(memory.lastNode.getCell(4).isFreed, "The cell[4] property is incorrect")
    }

    @Test
    @Incorrect
    fun `test restoring a copy that does not belong to the chain`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BigNodeDoesNotBelongToMemoryChain) {
            val memory1 = TestUtils.generateTestMemory()
            val memory2 = TestUtils.generateTestMemory()

            val bigNode = memory1.freezeCopy()

            memory2.restoreCopy(bigNode)
        }
    }
}
