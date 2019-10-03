package org.lexem.angmar.analyzer.data.referenced

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*

internal class LxmListTest {
    @Test
    fun `test constructors`() {
        val old = LxmList()

        Assertions.assertNull(old.oldList, "The oldList property is incorrect")

        val new = LxmList(old)

        Assertions.assertEquals(old, new.oldList, "The oldList property is incorrect")
    }

    @Test
    fun `test add and get cell`() {
        val memory = LexemMemory()
        val old = LxmList()
        old.addCell(memory, LxmInteger.Num0)

        val new = LxmList(old)
        new.addCell(memory, LxmLogic.True)

        Assertions.assertEquals(1, old.listSize, "The listSize is incorrect")
        Assertions.assertEquals(2, new.listSize, "The listSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertNull(old.getCell(memory, 1), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, new.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmLogic.True, new.getCell(memory, 1), "The cell[1] is incorrect")
    }

    @Test
    fun `test set cell`() {
        val memory = LexemMemory()
        val old = LxmList()
        old.addCell(memory, LxmInteger.Num0)

        val new = LxmList(old)
        new.addCell(memory, LxmLogic.True)

        new.setCell(memory, 0, LxmInteger.Num10)
        new.setCell(memory, 1, LxmInteger.Num_1)

        Assertions.assertEquals(1, old.listSize, "The listSize is incorrect")
        Assertions.assertEquals(2, new.listSize, "The listSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertNull(old.getCell(memory, 1), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num_1, new.getCell(memory, 1), "The cell[0] is incorrect")

        Assertions.assertEquals(old, new.oldList, "The oldList property is incorrect")
    }

    @Test
    fun `test remove cell`() {
        val memory = LexemMemory()
        val old = LxmList()
        old.addCell(memory, LxmInteger.Num0)

        val new = LxmList(old)
        new.addCell(memory, LxmLogic.True)

        new.removeCell(memory, 1)
        new.removeCell(memory, 0)

        Assertions.assertEquals(1, old.listSize, "The listSize is incorrect")
        Assertions.assertEquals(0, new.listSize, "The listSize is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, old.getCell(memory, 0), "The cell[0] is incorrect")
        Assertions.assertNull(old.getCell(memory, 1), "The cell[0] is incorrect")

        Assertions.assertEquals(old, new.oldList, "The oldList property is incorrect")
    }

    @Test
    fun `test get all cells`() {
        val memory = LexemMemory()
        val old = LxmList()
        old.addCell(memory, LxmInteger.Num0)

        val old2 = LxmList(old)
        old2.addCell(memory, LxmInteger.Num10)

        val new = LxmList(old2)
        new.addCell(memory, LxmLogic.True)

        val cells = new.getAllCells()

        Assertions.assertEquals(3, cells.size, "The number of cells is incorrect")
        Assertions.assertEquals(LxmInteger.Num0, cells[0], "The cell[0] is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, cells[1], "The cell[1] is incorrect")
        Assertions.assertEquals(LxmLogic.True, cells[2], "The cell[2] is incorrect")
    }

    @Test
    fun `test make constant`() {
        val memory = LexemMemory()
        val list = LxmList()

        Assertions.assertFalse(list.isImmutable, "The isImmutable property is incorrect")

        list.makeConstant(memory)

        Assertions.assertTrue(list.isImmutable, "The isImmutable property is incorrect")
    }

    @Test
    fun `test clone`() {
        val memory = LexemMemory()

        val old = LxmList()
        val cloned = old.clone()

        Assertions.assertEquals(old, cloned.oldList, "The oldObject is incorrect")

        val oldConst = LxmList()
        oldConst.makeConstant(memory)
        val clonedConst = oldConst.clone()

        Assertions.assertTrue(clonedConst.isImmutable, "The isImmutable is incorrect")
        Assertions.assertEquals(oldConst, clonedConst, "The clonedConst is incorrect")
    }

    @Test
    fun `test memory dealloc`() {
        val memory = LexemMemory()

        val list = LxmList()
        val listRef = memory.add(list)
        listRef.increaseReferenceCount(memory)
        val listCell = memory.lastNode.getCell(listRef.position)

        val old = LxmList()
        old.addCell(memory, LxmLogic.True)
        old.addCell(memory, listRef)

        val new = LxmList(old)
        new.addCell(memory, LxmLogic.True)
        new.addCell(memory, listRef)

        Assertions.assertEquals(4, new.listSize, "The listSize property is incorrect")
        Assertions.assertEquals(3, listCell.referenceCount, "The referenceCount property is incorrect")

        new.memoryDealloc(memory)

        Assertions.assertEquals(0, new.listSize, "The listSize property is incorrect")
        Assertions.assertEquals(1, listCell.referenceCount, "The referenceCount property is incorrect")
    }

    @Test
    fun `test get type`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory

        val list = LxmList()
        val type = list.getType(memory)
        val context = AnalyzerCommons.getCurrentContext(memory)
        val listType = context.getDereferencedProperty<LxmObject>(memory, ListType.TypeName)!!
        Assertions.assertEquals(listType, type, "The type is incorrect")
    }

    @Test
    fun `test get prototype`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory

        val list = LxmList()
        val result = list.getPrototype(memory)
        val context = AnalyzerCommons.getCurrentContext(memory)
        val listType = context.getDereferencedProperty<LxmObject>(memory, ListType.TypeName)!!
        val prototype = listType.getDereferencedProperty<LxmObject>(memory, AnalyzerCommons.Identifiers.Prototype)!!
        Assertions.assertEquals(prototype, result, "The result is incorrect")
    }

    @Test
    @Incorrect
    fun `add a cell of a constant object`() {
        TestUtils.assertAnalyzerException {
            val memory = LexemMemory()
            val list = LxmList()
            list.makeConstant(memory)
            list.addCell(memory, LxmLogic.True)
        }
    }

    @Test
    @Incorrect
    fun `set a cell of a constant object`() {
        TestUtils.assertAnalyzerException {
            val memory = LexemMemory()
            val list = LxmList()
            list.addCell(memory, LxmLogic.True)
            list.makeConstant(memory)
            list.setCell(memory, 0, LxmLogic.False)
        }
    }

    @Test
    @Incorrect
    fun `set an out-of-bounds cell`() {
        TestUtils.assertAnalyzerException {
            val memory = LexemMemory()
            val list = LxmList()
            list.setCell(memory, 0, LxmLogic.False)
        }
    }

    @Test
    @Incorrect
    fun `remove a cell of a constant object`() {
        TestUtils.assertAnalyzerException {
            val memory = LexemMemory()
            val list = LxmList()
            list.addCell(memory, LxmLogic.True)
            list.makeConstant(memory)
            list.removeCell(memory, 0)
        }
    }

    @Test
    @Incorrect
    fun `remove an out-of-bounds cell`() {
        TestUtils.assertAnalyzerException {
            val memory = LexemMemory()
            val list = LxmList()
            list.removeCell(memory, 0)
        }
    }

    @Test
    fun `check the empty list is constant`() {
        Assertions.assertTrue(LxmList.Empty.isImmutable, "The LxmList.Empty must be constant")
    }
}
