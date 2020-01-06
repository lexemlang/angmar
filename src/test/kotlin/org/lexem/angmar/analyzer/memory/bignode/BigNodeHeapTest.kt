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
        val heap = BigNodeHeap()

        // Set
        val listOfObject = List(5) { LxmObject(memory) }
        for ((index, obj) in listOfObject.withIndex()) {
            heap.setCell(index, BigNodeHeapCell(bigNode.id, obj))

            Assertions.assertEquals(index + 1, heap.cellCount, "The cellCount property is incorrect")
        }

        Assertions.assertEquals(1, heap.size, "The size property is incorrect")

        // Get
        for ((index, obj) in listOfObject.withIndex()) {
            val pageObj = heap.getCell(bigNode, index, toWrite = false)

            Assertions.assertEquals(obj, pageObj.getValue(bigNode, toWrite = false), "The objects are different")
        }
    }

    @Test
    fun `test set reusing`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val heap = BigNodeHeap()

        // Set
        val obj = LxmObject(memory)
        heap.setCell(0, BigNodeHeapCell(bigNode.id, obj))
        Assertions.assertEquals(1, heap.size, "The size property is incorrect")
        Assertions.assertEquals(1, heap.cellCount, "The cellCount property is incorrect")

        // Reuse
        heap.setCell(0, BigNodeHeapCell(bigNode.id, obj))
        Assertions.assertEquals(1, heap.size, "The size property is incorrect")
        Assertions.assertEquals(1, heap.cellCount, "The cellCount property is incorrect")
    }

    @Test
    fun `test different pages`() {
        val memory = LexemMemory()
        val bigNode = memory.lastNode
        val heap = BigNodeHeap()

        // Set
        val obj = LxmObject(memory)
        heap.setCell(0, BigNodeHeapCell(bigNode.id, obj))
        Assertions.assertEquals(1, heap.size, "The size property is incorrect")
        Assertions.assertEquals(1, heap.cellCount, "The cellCount property is incorrect")

        // Set in same page
        heap.setCell(5, BigNodeHeapCell(bigNode.id, obj))
        Assertions.assertEquals(1, heap.size, "The size property is incorrect")
        Assertions.assertEquals(2, heap.cellCount, "The cellCount property is incorrect")

        // Set in other page
        heap.setCell(Int.MAX_VALUE, BigNodeHeapCell(bigNode.id, obj))
        Assertions.assertEquals(2, heap.size, "The size property is incorrect")
        Assertions.assertEquals(3, heap.cellCount, "The cellCount property is incorrect")
    }

    @Test
    @Incorrect
    fun `test get an undefined cell`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.HeapSegmentationFault) {
            val memory = LexemMemory()
            val bigNode = memory.lastNode
            val heap = BigNodeHeap()

            heap.getCell(bigNode, 0, toWrite = false)
        }
    }
}
