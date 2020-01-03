package org.lexem.angmar.analyzer.data.referenced

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.utils.*

internal class LxmNodeTest {
    @Test
    fun `test addToParent without parent`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val parent = LxmNode(memory, "parent", reader.saveCursor())
        val child0 = LxmNode(memory, "child1", reader.saveCursor())
        val child1 = LxmNode(memory, "child2", reader.saveCursor())
        val child2 = LxmNode(memory, "child3", reader.saveCursor())

        parent.addChild(memory, child0)
        parent.addChild(memory, child1)
        parent.addChild(memory, child2)

        Assertions.assertEquals(child0, parent.getFirstChild(memory, toWrite = false),
                "The firstChild property is incorrect")
        Assertions.assertEquals(child2, parent.getLastChild(memory, toWrite = false),
                "The lastChild property is incorrect")
        Assertions.assertEquals(3, parent.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertEquals(parent, child0.getParent(memory, toWrite = false), "The parent property is incorrect")
        Assertions.assertNull(child0.getLeftSibling(memory, toWrite = false), "The leftSibling property is incorrect")
        Assertions.assertEquals(child1, child0.getRightSibling(memory, toWrite = false),
                "The rightSibling property is incorrect")

        Assertions.assertEquals(parent, child1.getParent(memory, toWrite = false), "The parent property is incorrect")
        Assertions.assertEquals(child0, child1.getLeftSibling(memory, toWrite = false),
                "The leftSibling property is incorrect")
        Assertions.assertEquals(child2, child1.getRightSibling(memory, toWrite = false),
                "The rightSibling property is incorrect")

        Assertions.assertEquals(parent, child2.getParent(memory, toWrite = false), "The parent property is incorrect")
        Assertions.assertEquals(child1, child2.getLeftSibling(memory, toWrite = false),
                "The leftSibling property is incorrect")
        Assertions.assertNull(child2.getRightSibling(memory, toWrite = false), "The rightSibling property is incorrect")
    }

    @Test
    fun `test addToParent with parent`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val parent1 = LxmNode(memory, "parent1", reader.saveCursor())
        val parent2 = LxmNode(memory, "parent2", reader.saveCursor())
        val child = LxmNode(memory, "child", reader.saveCursor())

        parent1.addChild(memory, child)

        Assertions.assertEquals(child, parent1.getFirstChild(memory, toWrite = false),
                "The firstChild property is incorrect")
        Assertions.assertEquals(child, parent1.getLastChild(memory, toWrite = false),
                "The lastChild property is incorrect")
        Assertions.assertEquals(1, parent1.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertNull(parent2.getFirstChild(memory, toWrite = false), "The firstChild property is incorrect")
        Assertions.assertNull(parent2.getLastChild(memory, toWrite = false), "The lastChild property is incorrect")
        Assertions.assertEquals(0, parent2.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertEquals(parent1, child.getParent(memory, toWrite = false), "The parent property is incorrect")
        Assertions.assertNull(child.getLeftSibling(memory, toWrite = false), "The leftSibling property is incorrect")
        Assertions.assertNull(child.getRightSibling(memory, toWrite = false), "The rightSibling property is incorrect")

        parent2.addChild(memory, child)

        Assertions.assertEquals(child, parent2.getFirstChild(memory, toWrite = false),
                "The firstChild property is incorrect")
        Assertions.assertEquals(child, parent2.getLastChild(memory, toWrite = false),
                "The lastChild property is incorrect")
        Assertions.assertEquals(1, parent2.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertNull(parent1.getFirstChild(memory, toWrite = false), "The firstChild property is incorrect")
        Assertions.assertNull(parent1.getLastChild(memory, toWrite = false), "The lastChild property is incorrect")
        Assertions.assertEquals(0, parent1.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertEquals(parent2, child.getParent(memory, toWrite = false), "The parent property is incorrect")
        Assertions.assertNull(child.getLeftSibling(memory, toWrite = false), "The leftSibling property is incorrect")
        Assertions.assertNull(child.getRightSibling(memory, toWrite = false), "The rightSibling property is incorrect")
    }

    @Test
    fun `test removeFromParent without parent`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val node = LxmNode(memory, "node", reader.saveCursor())

        node.removeFromParent(memory)

        Assertions.assertNull(node.getParent(memory, toWrite = false), "The parent property is incorrect")
        Assertions.assertNull(node.getLeftSibling(memory, toWrite = false), "The leftSibling property is incorrect")
        Assertions.assertNull(node.getRightSibling(memory, toWrite = false), "The rightSibling property is incorrect")
    }

    @Test
    fun `test removeFromParent with parent`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val parent = LxmNode(memory, "parent", reader.saveCursor())
        val child = LxmNode(memory, "child", reader.saveCursor())

        parent.addChild(memory, child)

        Assertions.assertEquals(child, parent.getFirstChild(memory, toWrite = false),
                "The firstChild property is incorrect")
        Assertions.assertEquals(child, parent.getLastChild(memory, toWrite = false),
                "The lastChild property is incorrect")
        Assertions.assertEquals(1, parent.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertEquals(parent, child.getParent(memory, toWrite = false), "The parent property is incorrect")
        Assertions.assertNull(child.getLeftSibling(memory, toWrite = false), "The leftSibling property is incorrect")
        Assertions.assertNull(child.getRightSibling(memory, toWrite = false), "The rightSibling property is incorrect")

        child.removeFromParent(memory)

        Assertions.assertNull(parent.getFirstChild(memory, toWrite = false), "The firstChild property is incorrect")
        Assertions.assertNull(parent.getLastChild(memory, toWrite = false), "The lastChild property is incorrect")
        Assertions.assertEquals(0, parent.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertNull(child.getParent(memory, toWrite = false), "The parent property is incorrect")
        Assertions.assertNull(child.getLeftSibling(memory, toWrite = false), "The leftSibling property is incorrect")
        Assertions.assertNull(child.getRightSibling(memory, toWrite = false), "The rightSibling property is incorrect")
    }

    @Test
    fun `test insertChild at the beginning`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val parent = LxmNode(memory, "parent", reader.saveCursor())
        val child0 = LxmNode(memory, "child1", reader.saveCursor())
        val child1 = LxmNode(memory, "child2", reader.saveCursor())
        val childToInsert = LxmNode(memory, "childToInsert", reader.saveCursor())

        parent.addChild(memory, child0)
        parent.addChild(memory, child1)

        parent.insertChild(memory, childToInsert, null)

        Assertions.assertEquals(childToInsert, parent.getFirstChild(memory, toWrite = false),
                "The firstChild property is incorrect")
        Assertions.assertEquals(child1, parent.getLastChild(memory, toWrite = false),
                "The lastChild property is incorrect")
        Assertions.assertEquals(3, parent.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertEquals(parent, childToInsert.getParent(memory, toWrite = false),
                "The parent property is incorrect")
        Assertions.assertNull(childToInsert.getLeftSibling(memory, toWrite = false),
                "The leftSibling property is incorrect")
        Assertions.assertEquals(child0, childToInsert.getRightSibling(memory, toWrite = false),
                "The rightSibling property is incorrect")
    }

    @Test
    fun `test insertChild in the middle`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val parent = LxmNode(memory, "parent", reader.saveCursor())
        val child0 = LxmNode(memory, "child1", reader.saveCursor())
        val child1 = LxmNode(memory, "child2", reader.saveCursor())
        val childToInsert = LxmNode(memory, "childToInsert", reader.saveCursor())

        parent.addChild(memory, child0)
        parent.addChild(memory, child1)

        parent.insertChild(memory, childToInsert, child0)

        Assertions.assertEquals(child0, parent.getFirstChild(memory, toWrite = false),
                "The firstChild property is incorrect")
        Assertions.assertEquals(child1, parent.getLastChild(memory, toWrite = false),
                "The lastChild property is incorrect")
        Assertions.assertEquals(3, parent.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertEquals(parent, childToInsert.getParent(memory, toWrite = false),
                "The parent property is incorrect")
        Assertions.assertEquals(child0, childToInsert.getLeftSibling(memory, toWrite = false),
                "The leftSibling property is incorrect")
        Assertions.assertEquals(child1, childToInsert.getRightSibling(memory, toWrite = false),
                "The rightSibling property is incorrect")
    }

    @Test
    fun `test insertChild at the end`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val parent = LxmNode(memory, "parent", reader.saveCursor())
        val child0 = LxmNode(memory, "child1", reader.saveCursor())
        val child1 = LxmNode(memory, "child2", reader.saveCursor())
        val childToInsert = LxmNode(memory, "childToInsert", reader.saveCursor())

        parent.addChild(memory, child0)
        parent.addChild(memory, child1)

        parent.insertChild(memory, childToInsert, child1)

        Assertions.assertEquals(child0, parent.getFirstChild(memory, toWrite = false),
                "The firstChild property is incorrect")
        Assertions.assertEquals(childToInsert, parent.getLastChild(memory, toWrite = false),
                "The lastChild property is incorrect")
        Assertions.assertEquals(3, parent.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertEquals(parent, childToInsert.getParent(memory, toWrite = false),
                "The parent property is incorrect")
        Assertions.assertEquals(child1, childToInsert.getLeftSibling(memory, toWrite = false),
                "The leftSibling property is incorrect")
        Assertions.assertNull(childToInsert.getRightSibling(memory, toWrite = false),
                "The rightSibling property is incorrect")
    }

    @Test
    fun `test replaceNodeByItsChildren without children`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val grandParent = LxmNode(memory, "grandParent", reader.saveCursor())
        val parent = LxmNode(memory, "parent", reader.saveCursor())

        grandParent.addChild(memory, parent)

        Assertions.assertEquals(parent, grandParent.getFirstChild(memory, toWrite = false),
                "The firstChild property is incorrect")
        Assertions.assertEquals(parent, grandParent.getLastChild(memory, toWrite = false),
                "The lastChild property is incorrect")
        Assertions.assertEquals(1, grandParent.getChildCount(memory), "The childCount property is incorrect")

        parent.replaceByItsChildrenInParent(memory)

        Assertions.assertNull(grandParent.getFirstChild(memory, toWrite = false),
                "The firstChild property is incorrect")
        Assertions.assertNull(grandParent.getLastChild(memory, toWrite = false), "The lastChild property is incorrect")
        Assertions.assertEquals(0, parent.getChildCount(memory), "The childCount property is incorrect")
    }

    @Test
    fun `test replaceNodeByItsChildren with children`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val grandParent = LxmNode(memory, "grandParent", reader.saveCursor())
        val parent = LxmNode(memory, "parent", reader.saveCursor())
        val child0 = LxmNode(memory, "child1", reader.saveCursor())
        val child1 = LxmNode(memory, "child2", reader.saveCursor())

        grandParent.addChild(memory, parent)
        parent.addChild(memory, child0)
        parent.addChild(memory, child1)

        parent.replaceByItsChildrenInParent(memory)

        Assertions.assertEquals(child0, grandParent.getFirstChild(memory, toWrite = false),
                "The firstChild property is incorrect")
        Assertions.assertEquals(child1, grandParent.getLastChild(memory, toWrite = false),
                "The lastChild property is incorrect")
        Assertions.assertEquals(2, grandParent.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertEquals(grandParent, child0.getParent(memory, toWrite = false),
                "The parent property is incorrect")
        Assertions.assertNull(child0.getLeftSibling(memory, toWrite = false), "The leftSibling property is incorrect")
        Assertions.assertEquals(child1, child0.getRightSibling(memory, toWrite = false),
                "The rightSibling property is incorrect")

        Assertions.assertEquals(grandParent, child1.getParent(memory, toWrite = false),
                "The parent property is incorrect")
        Assertions.assertEquals(child0, child1.getLeftSibling(memory, toWrite = false),
                "The leftSibling property is incorrect")
        Assertions.assertNull(child1.getRightSibling(memory, toWrite = false), "The rightSibling property is incorrect")
    }

    @Test
    fun `test clear children`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val parent = LxmNode(memory, "parent", reader.saveCursor())
        val child0 = LxmNode(memory, "child1", reader.saveCursor())
        val child1 = LxmNode(memory, "child2", reader.saveCursor())
        val child2 = LxmNode(memory, "child3", reader.saveCursor())

        parent.addChild(memory, child0)
        parent.addChild(memory, child1)
        parent.addChild(memory, child2)

        parent.clearChildren(memory)

        Assertions.assertNull(parent.getFirstChild(memory, toWrite = false), "The firstChild property is incorrect")
        Assertions.assertNull(parent.getLastChild(memory, toWrite = false), "The lastChild property is incorrect")
        Assertions.assertEquals(0, parent.getChildCount(memory), "The childCount property is incorrect")

        Assertions.assertEquals(parent, child0.getParent(memory, toWrite = false), "The parent property is incorrect")
        Assertions.assertEquals(parent, child1.getParent(memory, toWrite = false), "The parent property is incorrect")
        Assertions.assertEquals(parent, child2.getParent(memory, toWrite = false), "The parent property is incorrect")
    }

    @Test
    fun `test getParentIndex`() {
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val reader = IOStringReader.from("")
        val parent = LxmNode(memory, "parent", reader.saveCursor())
        val child0 = LxmNode(memory, "child1", reader.saveCursor())
        val child1 = LxmNode(memory, "child2", reader.saveCursor())
        val child2 = LxmNode(memory, "child3", reader.saveCursor())

        parent.addChild(memory, child0)
        parent.addChild(memory, child1)
        parent.addChild(memory, child2)

        Assertions.assertEquals(-1, parent.getParentIndex(memory), "The parent index is incorrect")
        Assertions.assertEquals(0, child0.getParentIndex(memory), "The parent index is incorrect")
        Assertions.assertEquals(1, child1.getParentIndex(memory), "The parent index is incorrect")
        Assertions.assertEquals(2, child2.getParentIndex(memory), "The parent index is incorrect")
    }

    @Test
    fun `test apply offset`() {
        // Readers.
        val text2 = "a text for"
        val text1Pre = "this is "
        val text1Post = "this is "
        val text1 = "$text1Pre$text2$text1Post"
        val readerParent = IOStringReader.from(text1)
        val readerChild = IOStringReader.from(text2)

        // Prepare the node tree.
        val memory = TestUtils.generateTestMemoryFromAnalyzer()
        val parentLeftBound = 0
        val parentRightBound = text2.length
        readerChild.setPosition(parentLeftBound)
        val parent = LxmNode(memory, "parent", readerChild.saveCursor())
        readerChild.setPosition(parentRightBound)
        parent.setTo(memory, readerChild.saveCursor())

        val parentChild1LeftBound = 1
        val parentChild1RightBound = 2
        readerChild.setPosition(parentChild1LeftBound)
        val parentChild1 = LxmNode(memory, "parentChild1", readerChild.saveCursor())
        parent.addChild(memory, parentChild1)
        readerChild.setPosition(parentChild1RightBound)
        parentChild1.setTo(memory, readerChild.saveCursor())

        val parentChild2LeftBound = 4
        val parentChild2RightBound = 6
        readerChild.setPosition(parentChild2LeftBound)
        val parentChild2 = LxmNode(memory, "parentChild2", readerChild.saveCursor())
        parent.addChild(memory, parentChild2)
        readerChild.setPosition(parentChild2RightBound)
        parentChild2.setTo(memory, readerChild.saveCursor())

        // Apply the offset.
        readerParent.advance(text1Pre.length)
        parent.applyOffset(memory, readerParent.saveCursor())

        // Checks the nodes.
        checkNode(memory, parent, text1Pre.length + parentLeftBound, text1Pre.length + parentRightBound, readerParent,
                text2.substring(parentLeftBound, parentRightBound))
        checkNode(memory, parentChild1, text1Pre.length + parentChild1LeftBound,
                text1Pre.length + parentChild1RightBound, readerParent,
                text2.substring(parentChild1LeftBound, parentChild1RightBound))
        checkNode(memory, parentChild2, text1Pre.length + parentChild2LeftBound,
                text1Pre.length + parentChild2RightBound, readerParent,
                text2.substring(parentChild2LeftBound, parentChild2RightBound))

        Assertions.assertEquals(text1Pre.length, readerParent.currentPosition(), "The reader has been modified")
    }

    // AUXILIARY METHODS ------------------------------------------------------

    /**
     * Checks the correctness of a node.
     */
    private fun checkNode(memory: IMemory, node: LxmNode, from: Int, to: Int, reader: IOStringReader, content: String) {
        Assertions.assertEquals(from, node.getFrom(memory).primitive.position(), "The from property is incorrect")
        Assertions.assertEquals(reader, node.getFrom(memory).primitive.getReader(), "The from reader is incorrect")
        Assertions.assertEquals(to, node.getTo(memory)?.primitive?.position(), "The to property is incorrect")
        Assertions.assertEquals(reader, node.getTo(memory)?.primitive?.getReader(), "The to reader is incorrect")
        Assertions.assertEquals(content, (node.getContent(memory) as LxmString).primitive,
                "The content of the node is incorrect")
    }
}
