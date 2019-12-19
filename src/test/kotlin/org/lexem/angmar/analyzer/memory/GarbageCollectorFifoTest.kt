package org.lexem.angmar.analyzer.memory

import org.junit.jupiter.api.*

internal class GarbageCollectorFifoTest {
    @Test
    fun `test constructor`() {
        val size = 20
        val gcFifo = GarbageCollectorFifo(size)
        Assertions.assertEquals("Dead[0, ${size - 1}] ToProcess[]", gcFifo.toString(), "The fifo is incorrect")
    }

    @Test
    fun `test constructor - empty`() {
        val size = 0
        val gcFifo = GarbageCollectorFifo(size)
        Assertions.assertEquals("Dead[] ToProcess[]", gcFifo.toString(), "The fifo is incorrect")
    }

    @Test
    fun `test push - pop 1`() {
        val size = 20
        val gcFifo = GarbageCollectorFifo(size)
        Assertions.assertEquals("Dead[0, ${size - 1}] ToProcess[]", gcFifo.toString(), "The fifo is incorrect")

        // Push
        gcFifo.push(0)
        Assertions.assertEquals("Dead[1, ${size - 1}] ToProcess[0]", gcFifo.toString(), "The fifo is incorrect")

        gcFifo.push(size - 1)
        Assertions.assertEquals("Dead[1, ${size - 2}] ToProcess[0] U [${size - 1}]", gcFifo.toString(),
                "The fifo is incorrect")

        gcFifo.push(2)
        Assertions.assertEquals("Dead[1] U [3, ${size - 2}] ToProcess[0] U [2] U [${size - 1}]", gcFifo.toString(),
                "The fifo is incorrect")

        gcFifo.push(1)
        Assertions.assertEquals("Dead[3, ${size - 2}] ToProcess[0, 2] U [${size - 1}]", gcFifo.toString(),
                "The fifo is incorrect")

        gcFifo.push(size - 2)
        Assertions.assertEquals("Dead[3, ${size - 3}] ToProcess[0, 2] U [${size - 2}, ${size - 1}]", gcFifo.toString(),
                "The fifo is incorrect")

        // Pop
        Assertions.assertEquals(0, gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[3, ${size - 3}] ToProcess[1, 2] U [${size - 2}, ${size - 1}]", gcFifo.toString(),
                "The fifo is incorrect")

        Assertions.assertEquals(1, gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[3, ${size - 3}] ToProcess[2] U [${size - 2}, ${size - 1}]", gcFifo.toString(),
                "The fifo is incorrect")

        Assertions.assertEquals(2, gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[3, ${size - 3}] ToProcess[${size - 2}, ${size - 1}]", gcFifo.toString(),
                "The fifo is incorrect")

        Assertions.assertEquals(size - 2, gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[3, ${size - 3}] ToProcess[${size - 1}]", gcFifo.toString(),
                "The fifo is incorrect")

        Assertions.assertEquals(size - 1, gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[3, ${size - 3}] ToProcess[]", gcFifo.toString(), "The fifo is incorrect")

        Assertions.assertNull(gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[3, ${size - 3}] ToProcess[]", gcFifo.toString(), "The fifo is incorrect")
    }

    @Test
    fun `test push - pop 2`() {
        val size = 20
        val gcFifo = GarbageCollectorFifo(size)
        Assertions.assertEquals("Dead[0, ${size - 1}] ToProcess[]", gcFifo.toString(), "The fifo is incorrect")

        // Push
        gcFifo.push(5)
        Assertions.assertEquals("Dead[0, 4] U [6, ${size - 1}] ToProcess[5]", gcFifo.toString(),
                "The fifo is incorrect")

        gcFifo.push(2)
        Assertions.assertEquals("Dead[0, 1] U [3, 4] U [6, ${size - 1}] ToProcess[2] U [5]", gcFifo.toString(),
                "The fifo is incorrect")

        gcFifo.push(1)
        Assertions.assertEquals("Dead[0] U [3, 4] U [6, ${size - 1}] ToProcess[1, 2] U [5]", gcFifo.toString(),
                "The fifo is incorrect")

        gcFifo.push(6)
        Assertions.assertEquals("Dead[0] U [3, 4] U [7, ${size - 1}] ToProcess[1, 2] U [5, 6]", gcFifo.toString(),
                "The fifo is incorrect")

        gcFifo.push(3)
        Assertions.assertEquals("Dead[0] U [4] U [7, ${size - 1}] ToProcess[1, 3] U [5, 6]", gcFifo.toString(),
                "The fifo is incorrect")

        // Pop
        Assertions.assertEquals(1, gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[0] U [4] U [7, ${size - 1}] ToProcess[2, 3] U [5, 6]", gcFifo.toString(),
                "The fifo is incorrect")

        Assertions.assertEquals(2, gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[0] U [4] U [7, ${size - 1}] ToProcess[3] U [5, 6]", gcFifo.toString(),
                "The fifo is incorrect")

        Assertions.assertEquals(3, gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[0] U [4] U [7, ${size - 1}] ToProcess[5, 6]", gcFifo.toString(),
                "The fifo is incorrect")

        Assertions.assertEquals(5, gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[0] U [4] U [7, ${size - 1}] ToProcess[6]", gcFifo.toString(),
                "The fifo is incorrect")

        Assertions.assertEquals(6, gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[0] U [4] U [7, ${size - 1}] ToProcess[]", gcFifo.toString(),
                "The fifo is incorrect")

        Assertions.assertNull(gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[0] U [4] U [7, ${size - 1}] ToProcess[]", gcFifo.toString(),
                "The fifo is incorrect")
    }

    @Test
    fun `test push - pop - non contained`() {
        val size = 20
        val gcFifo = GarbageCollectorFifo(size)
        Assertions.assertEquals("Dead[0, ${size - 1}] ToProcess[]", gcFifo.toString(), "The fifo is incorrect")

        // Push
        gcFifo.push(size + 5)
        Assertions.assertEquals("Dead[0, ${size - 1}] ToProcess[]", gcFifo.toString(), "The fifo is incorrect")

        // Pop
        Assertions.assertNull(gcFifo.pop(), "The result of the fifo is incorrect")
        Assertions.assertEquals("Dead[0, ${size - 1}] ToProcess[]", gcFifo.toString(), "The fifo is incorrect")
    }
}
