package org.lexem.angmar.analyzer.data.referenced

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*

internal class LxmListTest {
    @Test
    fun `test constructors`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)

        Assertions.assertNull(old.oldVersion, "The oldList property is incorrect")

        val new = LxmList(memory, old)

        Assertions.assertEquals(old, new.oldVersion, "The oldList property is incorrect")
    }

    @Test
    fun `test add and get cell`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.addCell(memory, LxmInteger.Num0)

        val new = LxmList(memory, old)
        new.addCell(memory, LxmLogic.True, LxmFloat.Num0)

        Assertions.assertEquals(1, old.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(3, new.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertNull(old.getCell(memory, 1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, new.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmLogic.True, new.getCell(memory, 1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmFloat.Num0, new.getCell(memory, 2), "The cell[2] is incorrect")
    }

    @Test
    fun `test add cell ignoring constant`() {
        val memory = TestUtils.generateTestMemory()
        val list = LxmList(memory)
        list.makeConstant(memory)

        list.addCell(memory, LxmInteger.Num10, ignoreConstant = true)

        Assertions.assertEquals(1, list.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, list.getCell(memory, 0), "The cell[0] is incorrect")
    }

    @Test
    fun `test insert cell`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.insertCell(memory, 0, LxmInteger.Num0, LxmInteger.Num10)

        val new = LxmList(memory, old)
        new.insertCell(memory, 1, LxmInteger.Num1, LxmInteger.Num2)
        new.getAllCells()

        Assertions.assertEquals(2, old.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(4, new.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, old.getCell(memory, 1), "The cell[1] is incorrect")
        Assertions.assertNull(old.getCell(memory, 2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, new.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, new.getCell(memory, 1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmInteger.Num2, new.getCell(memory, 2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getCell(memory, 3), "The cell[3] is incorrect")
    }

    @Test
    fun `test insert cell ignoring constant`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.insertCell(memory, 0, LxmInteger.Num0, LxmInteger.Num10)

        val new = LxmList(memory, old)
        new.makeConstant(memory)
        new.insertCell(memory, 1, LxmInteger.Num1, LxmInteger.Num2, ignoreConstant = true)
        new.getAllCells()

        Assertions.assertEquals(2, old.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(4, new.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, old.getCell(memory, 1), "The cell[1] is incorrect")
        Assertions.assertNull(old.getCell(memory, 2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, new.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, new.getCell(memory, 1), "The cell[1] is incorrect")
        Assertions.assertEquals(LxmInteger.Num2, new.getCell(memory, 2), "The cell[2] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getCell(memory, 3), "The cell[3] is incorrect")
    }

    @Test
    fun `test set cell`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.addCell(memory, LxmInteger.Num0)

        val new = LxmList(memory, old)
        new.addCell(memory, LxmLogic.True)

        new.setCell(memory, 0, LxmInteger.Num10)
        new.setCell(memory, 1, LxmInteger.Num_1)

        Assertions.assertEquals(1, old.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(2, new.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertNull(old.getCell(memory, 1), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num_1, new.getCell(memory, 1), "The cell[0] is incorrect")

        Assertions.assertEquals(old, new.oldVersion, "The oldList property is incorrect")
    }

    @Test
    fun `test remove cell`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.addCell(memory, LxmInteger.Num0)

        val new = LxmList(memory, old)
        new.addCell(memory, LxmLogic.True)

        new.removeCell(memory, 1)
        new.removeCell(memory, 0)

        Assertions.assertEquals(1, old.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(1, old.listSize, "The currentListSize is incorrect")
        Assertions.assertEquals(0, new.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(0, new.listSize, "The currentListSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertNull(old.getCell(memory, 1), "The cell[0] is incorrect")

        Assertions.assertEquals(old, new.oldVersion, "The oldList property is incorrect")
    }

    @Test
    fun `test remove cell checking references`() {
        val memory = TestUtils.generateTestMemory()
        val list = LxmList(memory)
        val obj = LxmObject(memory)
        val objRef = memory.add(obj)
        list.addCell(memory, objRef)

        list.removeCell(memory, 0)

        Assertions.assertEquals(0, list.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(0, list.listSize, "The currentListSize is incorrect")
        Assertions.assertTrue(objRef.getCell(memory).isFreed, "The object cell has not been freed")
    }

    @Test
    fun `test remove many cells`() {
        val memory = TestUtils.generateTestMemory()
        val list = LxmList(memory)
        val obj = LxmObject(memory)
        val objRef = memory.add(obj)
        list.addCell(memory, LxmInteger.Num0)
        list.addCell(memory, LxmLogic.True)
        list.addCell(memory, objRef)
        list.addCell(memory, LxmInteger.Num2)

        list.removeCell(memory, 1, 2)

        Assertions.assertEquals(2, list.actualListSize, "The listSize is incorrect")
        Assertions.assertEquals(2, list.listSize, "The currentListSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, list.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num2, list.getCell(memory, 1), "The cell[1] is incorrect")
        Assertions.assertTrue(objRef.getCell(memory).isFreed, "The object cell has not been freed")
    }

    @Test
    fun `test remove cell ignoring constant`() {
        val memory = TestUtils.generateTestMemory()
        val list = LxmList(memory)
        list.addCell(memory, LxmInteger.Num0)
        list.makeConstant(memory)

        list.removeCell(memory, 0, ignoreConstant = true)

        Assertions.assertEquals(0, list.actualListSize, "The listSize is incorrect")
    }

    @Test
    fun `test get all cells`() {
        val memory = TestUtils.generateTestMemory()
        val old = LxmList(memory)
        old.addCell(memory, LxmInteger.Num0)

        val old2 = LxmList(memory, old)
        old2.addCell(memory, LxmInteger.Num10)

        val new = LxmList(memory, old2)
        new.addCell(memory, LxmLogic.True)

        val cells = new.getAllCells()

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

        list.makeConstant(memory)

        Assertions.assertTrue(list.isConstant, "The isConstant property is incorrect")
    }

    @Test
    fun `test clone`() {
        val memory = TestUtils.generateTestMemory()

        val old = LxmList(memory)
        val cloned = old.clone(memory)

        Assertions.assertEquals(old, cloned.oldVersion, "The oldObject is incorrect")

        val oldConst = LxmList(memory)
        oldConst.makeConstant(memory)
        val clonedConst = oldConst.clone(memory)

        Assertions.assertTrue(clonedConst.isConstant, "The isConstant is incorrect")
        Assertions.assertNotEquals(oldConst, clonedConst, "The clonedConst is incorrect")
    }

    @Test
    fun `test clone - non-writable`() {
        val memory = TestUtils.generateTestMemory()

        val old = LxmList(memory)
        old.makeConstantAndNotWritable(memory)
        val cloned = old.clone(memory)

        Assertions.assertEquals(old, cloned, "The cloned is incorrect")
    }

    @Test
    fun `test memory dealloc`() {
        val memory = TestUtils.generateTestMemory()

        val list = LxmList(memory)
        val listRef = memory.add(list)
        listRef.increaseReferences(memory)
        val listCell = memory.lastNode.getCell(memory, listRef.position)

        val old = LxmList(memory)
        old.addCell(memory, LxmLogic.True)
        old.addCell(memory, listRef)

        val new = LxmList(memory, old)
        new.addCell(memory, LxmLogic.True)
        new.addCell(memory, listRef)

        Assertions.assertEquals(4, new.actualListSize, "The listSize property is incorrect")
        Assertions.assertEquals(3, listCell.referenceCount, "The referenceCount property is incorrect")

        new.memoryDealloc(memory)

        Assertions.assertEquals(0, new.actualListSize, "The listSize property is incorrect")
        Assertions.assertEquals(1, listCell.referenceCount, "The referenceCount property is incorrect")
    }

    @Test
    fun `test memory dealloc - non-writable`() {
        val memory = TestUtils.generateTestMemory()

        val list1 = LxmList(memory)
        val list2 = LxmList(memory)

        val obj1Ref = memory.add(list1)
        val obj2Ref = memory.add(list2)

        list1.addCell(memory, obj2Ref)
        list1.makeConstantAndNotWritable(memory)
        obj1Ref.increaseReferences(memory)

        list1.memoryDealloc(memory)

        Assertions.assertTrue(obj2Ref.getCell(memory).isFreed, "The object has not been dealloc")
        Assertions.assertEquals(obj2Ref, list1.getCell(memory, 0), "The reference property is incorrect")
    }

    @Test
    fun `test get type`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory

        val list = LxmList(memory)
        val type = list.getType(memory)
        val context = AnalyzerCommons.getCurrentContext(memory, toWrite = false)
        val listType = context.getPropertyValue(memory, ListType.TypeName)!!
        Assertions.assertTrue(RelationalFunctions.identityEquals(type, listType), "The type is incorrect")
    }

    @Test
    fun `test get prototype`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory

        val list = LxmList(memory)
        val result = list.getPrototype(memory)
        val context = AnalyzerCommons.getCurrentContext(memory, toWrite = false)
        val listType = context.getDereferencedProperty<LxmObject>(memory, ListType.TypeName, toWrite = false)!!
        val prototype = listType.getPropertyValue(memory, AnalyzerCommons.Identifiers.Prototype)!!
        Assertions.assertTrue(RelationalFunctions.identityEquals(prototype, result), "The result is incorrect")
    }

    @Test
    @Incorrect
    fun `add a cell of a constant object`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.makeConstant(memory)
            list.addCell(memory, LxmLogic.True)
        }
    }

    @Test
    @Incorrect
    fun `set a cell of a constant object`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.addCell(memory, LxmLogic.True)
            list.makeConstant(memory)
            list.setCell(memory, 0, LxmLogic.False)
        }
    }

    @Test
    @Incorrect
    fun `set an out-of-bounds cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.setCell(memory, 0, LxmLogic.False)
        }
    }

    @Test
    @Incorrect
    fun `remove a cell of a constant object`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.addCell(memory, LxmLogic.True)
            list.makeConstant(memory)
            list.removeCell(memory, 0)
        }
    }

    @Test
    @Incorrect
    fun `remove an out-of-bounds cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.removeCell(memory, 0)
        }
    }

    @Test
    @Incorrect
    fun `add a cell in a non-writable list`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyANonWritableList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)

            list.makeConstantAndNotWritable(memory)
            list.addCell(memory, LxmNil, ignoreConstant = true)
        }
    }

    @Test
    @Incorrect
    fun `remove a cell in a non-writable list`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyANonWritableList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.addCell(memory, LxmLogic.True)

            list.makeConstantAndNotWritable(memory)
            list.removeCell(memory, 0, ignoreConstant = true)
        }
    }

    @Test
    @Incorrect
    fun `insert a cell in a non-writable list`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyANonWritableList) {
            val memory = TestUtils.generateTestMemory()
            val list = LxmList(memory)
            list.addCell(memory, LxmLogic.True)

            list.makeConstantAndNotWritable(memory)
            list.insertCell(memory, 0, LxmNil, ignoreConstant = true)
        }
    }
}
