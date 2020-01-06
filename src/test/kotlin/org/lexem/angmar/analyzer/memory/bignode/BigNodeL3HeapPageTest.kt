package org.lexem.angmar.analyzer.memory.bignode

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

internal class BigNodeL3HeapPageTest {
    @Test
    fun `test set, get and clone`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val page = BigNodeL3HeapPage(0)

        // Set
        val listOfObject = List(5) { LxmObject(memory) }
        for ((index, obj) in listOfObject.withIndex()) {
            Assertions.assertTrue(page.setCell(index, BigNodeHeapCell(bigNode.id, obj)),
                    "The page has not added the object properly")
        }

        Assertions.assertEquals(1, page.size, "The size property is incorrect")

        // Get
        for ((index, obj) in listOfObject.withIndex()) {
            val pageObj = page.getCell(bigNode, index, toWrite = false)

            Assertions.assertEquals(obj, pageObj.getValue(bigNode, toWrite = false), "The objects are different")
        }
    }

    @Test
    fun `test set reusing`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val page = BigNodeL3HeapPage(0)

        // Set
        val obj = LxmObject(memory)
        Assertions.assertTrue(page.setCell(0, BigNodeHeapCell(bigNode.id, obj)),
                "The page has not added the object properly")
        Assertions.assertEquals(1, page.size, "The size property is incorrect")

        // Reuse
        Assertions.assertFalse(page.setCell(0, BigNodeHeapCell(bigNode.id, obj)),
                "The page has not added the object properly")
        Assertions.assertEquals(1, page.size, "The size property is incorrect")
    }

    @Test
    fun `test different pages`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val page = BigNodeL3HeapPage(0)

        // Set
        val obj = LxmObject(memory)
        Assertions.assertTrue(page.setCell(0, BigNodeHeapCell(bigNode.id, obj)),
                "The page has not added the object properly")
        Assertions.assertEquals(1, page.size, "The size property is incorrect")

        // Set in same page
        Assertions.assertTrue(page.setCell(5, BigNodeHeapCell(bigNode.id, obj)),
                "The page has not added the object properly")
        Assertions.assertEquals(1, page.size, "The size property is incorrect")

        // Set in other page
        Assertions.assertTrue(page.setCell(page.lastIndex, BigNodeHeapCell(bigNode.id, obj)),
                "The page has not added the object properly")
        Assertions.assertEquals(2, page.size, "The size property is incorrect")
    }

    @Test
    @Incorrect
    fun `test get an undefined cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val page = BigNodeL3HeapPage(0)

            page.getCell(bigNode, 0, toWrite = false)
        }
    }
}
