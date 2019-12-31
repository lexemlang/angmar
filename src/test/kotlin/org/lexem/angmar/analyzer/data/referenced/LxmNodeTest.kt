package org.lexem.angmar.analyzer.data.referenced

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
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
        val parent = LxmNode(memory, "parent", readerChild.saveCursor())
        readerChild.setPosition(parentRightBound)
        parent.setTo(readerChild.saveCursor())

        val parentChild1LeftBound = 1
        val parentChild1RightBound = 2
        readerChild.setPosition(parentChild1LeftBound)
        val parentChild1 = LxmNode(memory, "parentChild1", readerChild.saveCursor())
        parentChild1.addToParent(parent)
        readerChild.setPosition(parentChild1RightBound)
        parentChild1.setTo(readerChild.saveCursor())

        val parentChild2LeftBound = 4
        val parentChild2RightBound = 6
        readerChild.setPosition(parentChild2LeftBound)
        val parentChild2 = LxmNode(memory, "parentChild2", readerChild.saveCursor())
        parentChild2.addToParent(parent)
        readerChild.setPosition(parentChild2RightBound)
        parentChild2.setTo(readerChild.saveCursor())

        // Apply the offset.
        readerParent.advance(text1Pre.length)
        parent.applyOffset(readerParent.saveCursor())

        // Checks the nodes.
        checkNode(parent, text1Pre.length + parentLeftBound, text1Pre.length + parentRightBound, readerParent,
                text2.substring(parentLeftBound, parentRightBound))
        checkNode(parentChild1, text1Pre.length + parentChild1LeftBound, text1Pre.length + parentChild1RightBound,
                readerParent, text2.substring(parentChild1LeftBound, parentChild1RightBound))
        checkNode(parentChild2, text1Pre.length + parentChild2LeftBound, text1Pre.length + parentChild2RightBound,
                readerParent, text2.substring(parentChild2LeftBound, parentChild2RightBound))

        Assertions.assertEquals(text1Pre.length, readerParent.currentPosition(), "The reader has been modified")
    }

    // AUXILIARY METHODS ------------------------------------------------------

    /**
     * Checks the correctness of a node.
     */
    private fun checkNode(node: LxmNode, from: Int, to: Int, reader: IOStringReader, content: String) {
        Assertions.assertEquals(from, node.getFrom().primitive.position(), "The from property is incorrect")
        Assertions.assertEquals(reader, node.getFrom().primitive.getReader(), "The from reader is incorrect")
        Assertions.assertEquals(to, node.getTo()?.primitive?.position(), "The to property is incorrect")
        Assertions.assertEquals(reader, node.getTo()?.primitive?.getReader(), "The to reader is incorrect")
        Assertions.assertEquals(content, (node.getContent() as LxmString).primitive,
                "The content of the node is incorrect")
    }
}
