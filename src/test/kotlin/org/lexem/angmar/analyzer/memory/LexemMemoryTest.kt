package org.lexem.angmar.analyzer.memory

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.utils.*

internal class LexemMemoryTest {
    private val size = 4

    @Test
    fun `test stack`() {
        val memory = TestUtils.generateTestMemory()
        val objects = List(size) { LxmString.from("obj$it") }

        // Push some
        for (obj in objects) {
            memory.pushStack(obj)
        }

        // Pop some
        for (obj in objects.reversed()) {
            val stackObj = memory.popStack()
            Assertions.assertEquals(obj, stackObj, "The value of the stack does not match")
        }
    }

    @Test
    fun `test stack recursively`() {
        val memory = TestUtils.generateTestMemory()
        val objects1 = List(size) { LxmString.from("objA$it") }
        val objects2 = List(size) { LxmString.from("objB$it") }

        // Push some in the first copy
        for (obj in objects1) {
            memory.pushStack(obj)
        }

        // Push some in the second copy
        memory.freezeCopy()
        for (obj in objects2) {
            memory.pushStack(obj)
        }

        // Pop all out of the second copy
        for (obj in (objects1 + objects2).reversed()) {
            val stackObj = memory.popStack()
            Assertions.assertEquals(obj, stackObj, "The value of the stack does not match")
        }

        // Pop all out of the first
        memory.rollbackCopy()
        for (obj in objects1.reversed()) {
            val stackObj = memory.popStack()
            Assertions.assertEquals(obj, stackObj, "The value of the stack does not match")
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
        TestUtils.assertAnalyzerException {
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
    @Incorrect
    fun `test restoring a copy that does not belong to the chain`() {
        TestUtils.assertAnalyzerException {
            val memory1 = TestUtils.generateTestMemory()
            val memory2 = TestUtils.generateTestMemory()

            val bigNode = memory1.freezeCopy()

            memory2.restoreCopy(bigNode)
        }
    }
}
