package org.lexem.angmar.analyzer.data.referenced

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.utils.*

internal class LxmNodeTest {
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
        val parent = LxmNode("parent", readerChild.saveCursor(), null, memory)
        readerChild.setPosition(parentRightBound)
        parent.setTo(memory, readerChild.saveCursor())

        val parentChild1LeftBound = 1
        val parentChild1RightBound = 2
        readerChild.setPosition(parentChild1LeftBound)
        val parentChild1 = LxmNode("parentChild1", readerChild.saveCursor(), parent, memory)
        parent.getChildren(memory, toWrite = true).addCell(memory, parentChild1, ignoreConstant = true)
        readerChild.setPosition(parentChild1RightBound)
        parentChild1.setTo(memory, readerChild.saveCursor())

        val parentChild2LeftBound = 4
        val parentChild2RightBound = 6
        readerChild.setPosition(parentChild2LeftBound)
        val parentChild2 = LxmNode("parentChild2", readerChild.saveCursor(), parent, memory)
        parent.getChildren(memory, toWrite = true).addCell(memory, parentChild2, ignoreConstant = true)
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
    private fun checkNode(memory: LexemMemory, node: LxmNode, from: Int, to: Int, reader: IOStringReader,
            content: String) {
        Assertions.assertEquals(from, node.getFrom(memory).primitive.position(), "The from property is incorrect")
        Assertions.assertEquals(reader, node.getFrom(memory).primitive.getReader(), "The from reader is incorrect")
        Assertions.assertEquals(to, node.getTo(memory)?.primitive?.position(), "The to property is incorrect")
        Assertions.assertEquals(reader, node.getTo(memory)?.primitive?.getReader(), "The to reader is incorrect")
        Assertions.assertEquals(content, (node.getContent(memory) as LxmString).primitive,
                "The content of the node is incorrect")
    }
}
