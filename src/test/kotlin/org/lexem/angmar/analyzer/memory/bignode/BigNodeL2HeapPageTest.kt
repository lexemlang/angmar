package org.lexem.angmar.analyzer.memory.bignode

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

internal class BigNodeL2HeapPageTest {
    @Test
    fun `test set, get and clone`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val page = BigNodeL2HeapPage(bigNode, 0)

        // Set
        val listOfObject = List(5) { LxmObject(memory) }
        for ((index, obj) in listOfObject.withIndex()) {
            Assertions.assertTrue(page.setCell(BigNodeHeapCell(bigNode, index, obj)),
                    "The page has not added the object properly")
        }

        Assertions.assertEquals(1, page.size, "The size property is incorrect")

        // Get
        for ((index, obj) in listOfObject.withIndex()) {
            val pageObj = page.getCell(index, toWrite = false)

            Assertions.assertEquals(obj, pageObj.getValue(toWrite = false), "The objects are different")
        }

        // Clone
        val pageNew = page.clone(BigNode(previousNode = null, nextNode = null))

        // Get
        for ((index, obj) in listOfObject.withIndex()) {
            val pageObj = pageNew.getCell(index, toWrite = false)

            Assertions.assertEquals(obj, pageObj.getValue(toWrite = false), "The objects are different")
        }

        // Get writing
        val cell = pageNew.getCell(0, true)

        // Get
        for ((index, obj) in listOfObject.withIndex().drop(1)) {
            val pageObj = pageNew.getCell(index, toWrite = false)

            Assertions.assertEquals(obj, pageObj.getValue(toWrite = false), "The objects are different")
        }
    }

    @Test
    fun `test set reusing`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val page = BigNodeL2HeapPage(bigNode, 0)

        // Set
        val obj = LxmObject(memory)
        Assertions.assertTrue(page.setCell(BigNodeHeapCell(bigNode, 0, obj)),
                "The page has not added the object properly")
        Assertions.assertEquals(1, page.size, "The size property is incorrect")

        // Reuse
        Assertions.assertFalse(page.setCell(BigNodeHeapCell(bigNode, 0, obj)),
                "The page has not added the object properly")
        Assertions.assertEquals(1, page.size, "The size property is incorrect")
    }

    @Test
    fun `test different pages`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val page = BigNodeL2HeapPage(bigNode, 0)

        // Set
        val obj = LxmObject(memory)
        Assertions.assertTrue(page.setCell(BigNodeHeapCell(bigNode, 0, obj)),
                "The page has not added the object properly")
        Assertions.assertEquals(1, page.size, "The size property is incorrect")

        // Set in same page
        Assertions.assertTrue(page.setCell(BigNodeHeapCell(bigNode, 5, obj)),
                "The page has not added the object properly")
        Assertions.assertEquals(1, page.size, "The size property is incorrect")

        // Set in other page
        Assertions.assertTrue(page.setCell(BigNodeHeapCell(bigNode, page.lastIndex, obj)),
                "The page has not added the object properly")
        Assertions.assertEquals(2, page.size, "The size property is incorrect")
    }

    @Test
    @Incorrect
    fun `test get an undefined cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val page = BigNodeL2HeapPage(bigNode, 0)

            page.getCell(0, toWrite = false)
        }
    }

    @Test
    @Incorrect
    fun `test clone over the same bigNode`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CloneOverTheSameBigNode) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val page = BigNodeL2HeapPage(bigNode, 0)

            page.clone(bigNode)
        }
    }

    @Test
    @Incorrect
    fun `test set a value over different bigNode`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.DifferentBigNodeLink) {
            val memory = LexemMemory()
            val bigNode = BigNode(previousNode = null, nextNode = null)
            val obj = LxmObject(memory)

            val page = BigNodeL2HeapPage(bigNode, 0)
            page.setCell(BigNodeHeapCell(memory.lastNode, 5, obj))
        }
    }
}

