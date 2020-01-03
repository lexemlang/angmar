package org.lexem.angmar.analyzer.data.referenced.iterators

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.utils.*

internal class LxmNodeChildrenIteratorTest {
    @Test
    fun `test node with no children`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val parent = LxmNode(memory, "parent", reader.saveCursor())
        val iterator = LxmNodeChildrenIterator(memory, parent)

        Assertions.assertEquals(0, iterator.intervalSize, "The isEnded property is incorrect")
        Assertions.assertTrue(iterator.isEnded(memory), "The isEnded property is incorrect")
        Assertions.assertNull(iterator.getCurrent(memory), "The current property is incorrect")
        Assertions.assertEquals(0, iterator.getIndex(memory).primitive, "The index property is incorrect")
    }

    @Test
    fun `test node with children`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val parent = LxmNode(memory, "parent", reader.saveCursor())
        val child0 = LxmNode(memory, "child1", reader.saveCursor())
        val child1 = LxmNode(memory, "child2", reader.saveCursor())
        val child2 = LxmNode(memory, "child3", reader.saveCursor())

        parent.addChild(memory, child0)
        parent.addChild(memory, child1)
        parent.addChild(memory, child2)

        val iterator = LxmNodeChildrenIterator(memory, parent)

        Assertions.assertEquals(3, iterator.intervalSize, "The isEnded property is incorrect")
        Assertions.assertFalse(iterator.isEnded(memory), "The isEnded property is incorrect")
        Assertions.assertEquals(child0.getPrimitive(), iterator.getCurrent(memory)!!.second,
                "The current property is incorrect")
        Assertions.assertEquals(0, iterator.getIndex(memory).primitive, "The index property is incorrect")

        iterator.advance(memory)

        Assertions.assertFalse(iterator.isEnded(memory), "The isEnded property is incorrect")
        Assertions.assertEquals(child1.getPrimitive(), iterator.getCurrent(memory)!!.second,
                "The current property is incorrect")
        Assertions.assertEquals(1, iterator.getIndex(memory).primitive, "The index property is incorrect")

        iterator.advance(memory)

        Assertions.assertFalse(iterator.isEnded(memory), "The isEnded property is incorrect")
        Assertions.assertEquals(child2.getPrimitive(), iterator.getCurrent(memory)!!.second,
                "The current property is incorrect")
        Assertions.assertEquals(2, iterator.getIndex(memory).primitive, "The index property is incorrect")

        iterator.advance(memory)

        Assertions.assertTrue(iterator.isEnded(memory), "The isEnded property is incorrect")
        Assertions.assertNull(iterator.getCurrent(memory), "The current property is incorrect")
        Assertions.assertEquals(3, iterator.getIndex(memory).primitive, "The index property is incorrect")
    }
}
