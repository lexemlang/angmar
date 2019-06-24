package org.lexem.angmar.nodes

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.config.AngmarConfig
import org.lexem.angmar.io.readers.CustomStringReader

internal class ForwardBufferTest {

    private val defaultConfig = AngmarConfig()
    private val parser =
        LexemParser(CustomStringReader.from(""), defaultConfig)

    @Test
    fun `add and find entries test`() {
        `add and find entries`()
    }

    @Test
    fun `remove one by one`() {
        val buffer = `add and find entries`()

        for (i in 0..200) {
            val type1 = NodeType.CommentSingleLine
            val type2 = NodeType.Whitespace
            buffer.remove(i)

            Assertions.assertEquals(null, buffer.find(i, type1))
            Assertions.assertEquals(null, buffer.find(i, type2))
        }
    }

    @Test
    fun `remove all with position range`() {
        val buffer = `add and find entries`()

        buffer.remove(0, 200)

        for (i in 0..200) {
            val type1 = NodeType.CommentSingleLine
            val type2 = NodeType.Whitespace

            Assertions.assertEquals(null, buffer.find(i, type1))
            Assertions.assertEquals(null, buffer.find(i, type2))
        }
    }

    @Test
    fun `remove some positions with position range`() {
        val buffer = ForwardBuffer()
        val nodes = listOf(EmptyNodeForTests(0, NodeType.CommentSingleLine, parser),
                EmptyNodeForTests(2, NodeType.CommentSingleLine, parser),
                EmptyNodeForTests(10, NodeType.CommentSingleLine, parser),
                EmptyNodeForTests(20, NodeType.CommentSingleLine, parser),
                EmptyNodeForTests(30, NodeType.CommentSingleLine, parser))

        nodes.forEach {
            buffer.add(it)
        }

        buffer.remove(1, 15)

        Assertions.assertEquals(nodes[0], buffer.find(nodes[0].from.position(), nodes[0].type))
        Assertions.assertEquals(null, buffer.find(nodes[1].from.position(), nodes[1].type))
        Assertions.assertEquals(null, buffer.find(nodes[2].from.position(), nodes[2].type))
        Assertions.assertEquals(nodes[3], buffer.find(nodes[3].from.position(), nodes[3].type))
        Assertions.assertEquals(nodes[4], buffer.find(nodes[4].from.position(), nodes[4].type))
    }

    @Test
    fun clear() {
        val buffer = `add and find entries`()

        buffer.clear()

        for (i in 0..200) {
            val type1 = NodeType.CommentSingleLine
            val type2 = NodeType.Whitespace

            Assertions.assertEquals(null, buffer.find(i, type1))
            Assertions.assertEquals(null, buffer.find(i, type2))
        }
    }

    // AUXILIAR ---------------------------------------------------------------

    private fun `add and find entries`(): ForwardBuffer {
        val buffer = ForwardBuffer()

        for (i in 0..200) {
            val node = EmptyNodeForTests(i, NodeType.Whitespace, parser)
            buffer.add(node)

            Assertions.assertEquals(node, buffer.find(i, NodeType.Whitespace))
        }

        // Collisions
        for (i in 0..200) {
            val node = EmptyNodeForTests(i, NodeType.CommentSingleLine, parser)
            buffer.add(node)

            Assertions.assertEquals(node, buffer.find(i, NodeType.CommentSingleLine))
        }

        return buffer
    }
}