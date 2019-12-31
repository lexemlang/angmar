package org.lexem.angmar.analyzer.data.referenced

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

internal class LxmListTest {
    @Test
    fun `test constructors`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)

        TestUtils.freezeCopy(memory)

        val new = old.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!

        Assertions.assertNotEquals(old, new, "The list is the same")
    }

    @Test
    fun `test add and get cell`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.addCell(LxmInteger.Num0)

        TestUtils.freezeCopy(memory)

        val new = old.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!
        new.addCell(LxmLogic.True, LxmFloat.Num0)

        Assertions.assertEquals(1, old.size, "The size is incorrect")
        Assertions.assertEquals(3, new.size, "The size is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(0), "The cell[0] is incorrect")
        Assertions.assertNull(old.getCell(1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, new.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmLogic.True, new.getCell(1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmFloat.Num0, new.getCell(2), "The cell[2] is incorrect")
    }

    @Test
    fun `test add cell ignoring constant`() {
        val memory = TestUtils.generateTestMemory()
        val list = LxmList(memory)
        list.makeConstant()

        list.addCell(LxmInteger.Num10, ignoreConstant = true)

        Assertions.assertEquals(1, list.size, "The size is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, list.getCell(0), "The cell[0] is incorrect")
    }

    @Test
    fun `test insert cell`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.insertCell(0, LxmInteger.Num0, LxmInteger.Num10)

        TestUtils.freezeCopy(memory)

        val new = old.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!
        new.insertCell(1, LxmInteger.Num1, LxmInteger.Num2)
        new.getAllCells()

        Assertions.assertEquals(2, old.size, "The size is incorrect")
        Assertions.assertEquals(4, new.size, "The size is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, old.getCell(1), "The cell[1] is incorrect")
        Assertions.assertNull(old.getCell(2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, new.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, new.getCell(1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmInteger.Num2, new.getCell(2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getCell(3), "The cell[3] is incorrect")
    }

    @Test
    fun `test insert cell ignoring constant`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.insertCell(0, LxmInteger.Num0, LxmInteger.Num10)

        TestUtils.freezeCopy(memory)

        val new = old.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!
        new.makeConstant()
        new.insertCell(1, LxmInteger.Num1, LxmInteger.Num2, ignoreConstant = true)
        new.getAllCells()

        Assertions.assertEquals(2, old.size, "The size is incorrect")
        Assertions.assertEquals(4, new.size, "The size is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, old.getCell(1), "The cell[1] is incorrect")
        Assertions.assertNull(old.getCell(2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, new.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, new.getCell(1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmInteger.Num2, new.getCell(2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getCell(3), "The cell[3] is incorrect")
    }

    @Test
    fun `test replace cells - removing less`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.replaceCell(0, 0, LxmInteger.Num0, LxmInteger.Num1, LxmInteger.Num2, LxmInteger.Num10)

        TestUtils.freezeCopy(memory)

        val new = old.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!
        new.replaceCell(1, 1, LxmLogic.True, LxmLogic.False)
        new.getAllCells()

        Assertions.assertEquals(4, old.size, "The size is incorrect")
        Assertions.assertEquals(5, new.size, "The size is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, old.getCell(1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmInteger.Num2, old.getCell(2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, old.getCell(3), "The cell[3] is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, new.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmLogic.True, new.getCell(1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmLogic.False, new.getCell(2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num2, new.getCell(3), "The cell[3] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getCell(4), "The cell[4] is incorrect")
    }

    @Test
    fun `test replace cells - removing more`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.replaceCell(0, 0, LxmInteger.Num0, LxmInteger.Num1, LxmInteger.Num2, LxmInteger.Num10)

        TestUtils.freezeCopy(memory)

        val new = old.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!
        new.replaceCell(1, 2, LxmLogic.True)
        new.getAllCells()

        Assertions.assertEquals(4, old.size, "The size is incorrect")
        Assertions.assertEquals(3, new.size, "The size is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, old.getCell(1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmInteger.Num2, old.getCell(2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, old.getCell(3), "The cell[3] is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, new.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmLogic.True, new.getCell(1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getCell(2), "The cell[2] is incorrect")
    }

    @Test
    fun `test set cell`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.addCell(LxmInteger.Num0)

        TestUtils.freezeCopy(memory)

        val new = old.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!
        new.addCell(LxmLogic.True)

        new.setCell(0, LxmInteger.Num10)
        new.setCell(1, LxmInteger.Num_1)

        Assertions.assertEquals(1, old.size, "The size is incorrect")
        Assertions.assertEquals(2, new.size, "The size is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(0), "The cell[0] is incorrect")
        Assertions.assertNull(old.getCell(1), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num_1, new.getCell(1), "The cell[0] is incorrect")
    }

    @Test
    fun `test remove cell`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.addCell(LxmInteger.Num0)

        TestUtils.freezeCopy(memory)

        val new = old.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!
        new.addCell(LxmLogic.True)

        new.removeCell(1)
        new.removeCell(0)

        Assertions.assertEquals(1, old.size, "The size is incorrect")
        Assertions.assertEquals(1, old.size, "The currentListSize is incorrect")
        Assertions.assertEquals(0, new.size, "The size is incorrect")
        Assertions.assertEquals(0, new.size, "The currentListSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(0), "The cell[0] is incorrect")
        Assertions.assertNull(old.getCell(1), "The cell[0] is incorrect")
    }

    @Test
    fun `test remove cell checking references`() {
        val memory = TestUtils.generateTestMemory()
        val list = LxmList(memory)
        val obj = LxmObject(memory)
        list.addCell(obj)

        list.removeCell(0)

        Assertions.assertEquals(0, list.size, "The size is incorrect")
        Assertions.assertEquals(0, list.size, "The currentListSize is incorrect")
        Assertions.assertTrue(obj.getPrimitive().getCell(memory, toWrite = false).isFreed,
                "The object cell has not been freed")
    }

    @Test
    fun `test remove many cells`() {
        val memory = TestUtils.generateTestMemory()
        val list = LxmList(memory)
        val obj = LxmObject(memory)
        list.addCell(LxmInteger.Num0)
        list.addCell(LxmLogic.True)
        list.addCell(obj)
        list.addCell(LxmInteger.Num2)

        list.removeCell(1, 2)

        Assertions.assertEquals(2, list.size, "The size is incorrect")
        Assertions.assertEquals(2, list.size, "The currentListSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, list.getCell(0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num2, list.getCell(1), "The cell[1] is incorrect")
        Assertions.assertTrue(obj.getPrimitive().getCell(memory, toWrite = false).isFreed,
                "The object cell has not been freed")
    }

    @Test
    fun `test remove cell ignoring constant`() {
        val memory = TestUtils.generateTestMemory()
        val list = LxmList(memory)
        list.addCell(LxmInteger.Num0)
        list.makeConstant()

        list.removeCell(0, ignoreConstant = true)

        Assertions.assertEquals(0, list.size, "The size is incorrect")
    }

    @Test
    fun `test get all cells`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.addCell(LxmInteger.Num0)

        TestUtils.freezeCopy(memory)

        val old2 = old.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!
        old2.addCell(LxmInteger.Num10)

        TestUtils.freezeCopy(memory)

        val new = old2.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!
        new.addCell(LxmLogic.True)

        val cells = new.getAllCells().toList()

        Assertions.assertEquals(3, cells.size, "The number of cells is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, cells[0], "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, cells[1], "The cell[1] is incorrect")
        Assertions.assertEquals(LxmLogic.True, cells[2], "The cell[2] is incorrect")
    }

    @Test
    fun `test make constant`() {
        val memory = TestUtils.generateTestMemory()
        val list = LxmList(memory)

        Assertions.assertFalse(list.isConstant, "The isConstant property is incorrect")

        list.makeConstant()

        Assertions.assertTrue(list.isConstant, "The isConstant property is incorrect")
    }

    @Test
    fun `test memoryShift`() {
        val memory = TestUtils.generateTestMemory()

        val old = LxmList(memory)
        val oldConst = LxmList(memory)
        oldConst.makeConstant()

        TestUtils.freezeCopy(memory)

        val cloned = old.memoryClone(memory.lastNode)
        val clonedConst = oldConst.memoryClone(memory.lastNode)

        Assertions.assertFalse(cloned.isConstant, "The isConstant is incorrect")
        Assertions.assertTrue(clonedConst.isConstant, "The isConstant is incorrect")
        Assertions.assertNotEquals(oldConst, clonedConst, "The clonedConst is incorrect")
    }

    @Test
    fun `test memoryShift - non-writable`() {
        val memory = TestUtils.generateTestMemory()

        val old = LxmList(memory)
        old.makeConstantAndNotWritable()

        TestUtils.freezeCopy(memory)

        val cloned = old.memoryClone(memory.lastNode)

        Assertions.assertEquals(old, cloned, "The cloned is incorrect")
    }

    @Test
    @Incorrect
    fun `test memoryShift in the same bigNode`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.ValueShiftOverSameBigNode) {
            val memory = TestUtils.generateTestMemory()
            val old = LxmList(memory)
            old.memoryClone(memory.lastNode)
        }
    }

    @Test
    fun `test memory dealloc`() {
        val memory = TestUtils.generateTestMemory()

        val list = LxmList(memory)
        list.getPrimitive().increaseReferences(memory.lastNode)

        val old = LxmList(memory)
        old.addCell(LxmLogic.True)
        old.addCell(list)

        TestUtils.freezeCopy(memory)

        val new = old.getPrimitive().dereferenceAs<LxmList>(memory, toWrite = true)!!
        new.addCell(LxmLogic.True)
        new.addCell(list)
        val newListCell = memory.lastNode.getHeapCell(list.getPrimitive().position, toWrite = false)

        Assertions.assertEquals(4, new.size, "The size property is incorrect")
        Assertions.assertEquals(3, newListCell.referenceCount, "The referenceCount property is incorrect")

        new.memoryDealloc()

        Assertions.assertEquals(0, new.size, "The size property is incorrect")
        Assertions.assertEquals(1, newListCell.referenceCount, "The referenceCount property is incorrect")
    }

    @Test
    fun `test memory dealloc - non-writable`() {
        val memory = TestUtils.generateTestMemory()

        val list1 = LxmList(memory)
        val list2 = LxmList(memory)

        list1.addCell(list2)
        list1.makeConstantAndNotWritable()
        list1.getPrimitive().increaseReferences(memory.lastNode)

        list1.memoryDealloc()

        Assertions.assertTrue(list2.getPrimitive().getCell(memory, toWrite = false).isFreed,
                "The object has not been dealloc")
        Assertions.assertEquals(list2.getPrimitive(), list1.getCell(0), "The reference property is incorrect")
    }

    @Test
    fun `test get type`() {
        val analyzer = LexemAnalyzer(CompiledNode.Companion.EmptyCompiledNode)
        val memory = analyzer.memory

        val list = LxmList(memory)
        val type = list.getType(memory)
        val context = AnalyzerCommons.getCurrentContext(memory, toWrite = false)
        val listType = context.getPropertyValue(ListType.TypeName)!!
        Assertions.assertTrue(RelationalFunctions.identityEquals(type, listType), "The type is incorrect")
    }

    @Test
    fun `test get prototype`() {
        val analyzer = LexemAnalyzer(CompiledNode.Companion.EmptyCompiledNode)
        val memory = analyzer.memory

        val list = LxmList(memory)
        val result = list.getPrototype()
        val context = AnalyzerCommons.getCurrentContext(memory, toWrite = false)
        val listType = context.getDereferencedProperty<LxmObject>(ListType.TypeName, toWrite = false)!!
        val prototype = listType.getPropertyValue(AnalyzerCommons.Identifiers.Prototype)!!
        Assertions.assertTrue(RelationalFunctions.identityEquals(prototype, result), "The result is incorrect")
    }

    @Test
    @Incorrect
    fun `add a cell of a constant object`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.makeConstant()
            list.addCell(LxmLogic.True)
        }
    }

    @Test
    @Incorrect
    fun `set a cell of a constant object`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.addCell(LxmLogic.True)
            list.makeConstant()
            list.setCell(0, LxmLogic.False)
        }
    }

    @Test
    @Incorrect
    fun `set an out-of-bounds cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.setCell(0, LxmLogic.False)
        }
    }

    @Test
    @Incorrect
    fun `remove a cell of a constant object`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.addCell(LxmLogic.True)
            list.makeConstant()
            list.removeCell(0)
        }
    }

    @Test
    @Incorrect
    fun `remove an out-of-bounds cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.removeCell(0)
        }
    }

    @Test
    @Incorrect
    fun `add a cell in a non-writable list`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyANonWritableList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)

            list.makeConstantAndNotWritable()
            list.addCell(LxmNil, ignoreConstant = true)
        }
    }

    @Test
    @Incorrect
    fun `remove a cell in a non-writable list`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyANonWritableList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.addCell(LxmLogic.True)

            list.makeConstantAndNotWritable()
            list.removeCell(0, ignoreConstant = true)
        }
    }

    @Test
    @Incorrect
    fun `insert a cell in a non-writable list`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyANonWritableList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.addCell(LxmLogic.True)

            list.makeConstantAndNotWritable()
            list.insertCell(0, LxmNil, ignoreConstant = true)
        }
    }
}
