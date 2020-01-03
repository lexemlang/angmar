package org.lexem.angmar.analyzer.memory.bignode

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.utils.*

internal class BigNodeHeapTest {
    @Test
    fun `test set, get and clone`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val heap = BigNodeHeap(bigNode)

        // Set
        val listOfObject = List(5) { LxmObject(memory) }
        for ((index, obj) in listOfObject.withIndex()) {
            heap.setCell(BigNodeHeapCell(bigNode, index, obj))

            Assertions.assertEquals(index + 1, heap.cellCount.get(), "The cellCount property is incorrect")
        }

        Assertions.assertEquals(1, heap.size, "The size property is incorrect")

        // Get
        for ((index, obj) in listOfObject.withIndex()) {
            val pageObj = heap.getCell(index, toWrite = false)

            Assertions.assertEquals(obj, pageObj.getValue(toWrite = false), "The objects are different")
        }

        // Clone
        val pageNew = heap.clone(BigNode(previousNode = null, nextNode = null))

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
        val heap = BigNodeHeap(bigNode)

        // Set
        val obj = LxmObject(memory)
        heap.setCell(BigNodeHeapCell(bigNode, 0, obj))
        Assertions.assertEquals(1, heap.size, "The size property is incorrect")
        Assertions.assertEquals(1, heap.cellCount.get(), "The cellCount property is incorrect")

        // Reuse
        heap.setCell(BigNodeHeapCell(bigNode, 0, obj))
        Assertions.assertEquals(1, heap.size, "The size property is incorrect")
        Assertions.assertEquals(1, heap.cellCount.get(), "The cellCount property is incorrect")
    }

    @Test
    fun `test different pages`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val heap = BigNodeHeap(bigNode)

        // Set
        val obj = LxmObject(memory)
        heap.setCell(BigNodeHeapCell(bigNode, 0, obj))
        Assertions.assertEquals(1, heap.size, "The size property is incorrect")
        Assertions.assertEquals(1, heap.cellCount.get(), "The cellCount property is incorrect")

        // Set in same page
        heap.setCell(BigNodeHeapCell(bigNode, 5, obj))
        Assertions.assertEquals(1, heap.size, "The size property is incorrect")
        Assertions.assertEquals(2, heap.cellCount.get(), "The cellCount property is incorrect")

        // Set in other page
        heap.setCell(BigNodeHeapCell(bigNode, Int.MAX_VALUE, obj))
        Assertions.assertEquals(2, heap.size, "The size property is incorrect")
        Assertions.assertEquals(3, heap.cellCount.get(), "The cellCount property is incorrect")
    }

    @Test
    @Incorrect
    fun `test get an undefined cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val heap = BigNodeHeap(bigNode)

            heap.getCell(0, toWrite = false)
        }
    }

    @Test
    @Incorrect
    fun `test clone over the same bigNode`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CloneOverTheSameBigNode) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val heap = BigNodeHeap(bigNode)

            heap.clone(bigNode)
        }
    }

    @Test
    @Incorrect
    fun `test set a value over different bigNode`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.DifferentBigNodeLink) {
            val memory = LexemMemory()
            val bigNode = BigNode(previousNode = null, nextNode = null)
            val heap = LxmObject(memory)

            val page = BigNodeHeap(bigNode)
            page.setCell(BigNodeHeapCell(memory.lastNode, 5, heap))
        }
    }
}
