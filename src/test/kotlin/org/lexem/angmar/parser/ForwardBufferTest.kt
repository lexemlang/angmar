package org.lexem.angmar.parser

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*

internal class ForwardBufferTest {

    private val parser = LexemParser(CustomStringReader.from(""))

    @Test
    fun `add and find entries test`(): ForwardBuffer {
        val buffer = ForwardBuffer()

        for (i in 0..200) {
            val node = EmptyNodeForTests(i, parser)
            buffer.add(node)

            Assertions.assertEquals(node, buffer.find(i, EmptyNodeForTests::class.java),
                    "The Whitespace node is not properly added in the buffer")
        }

        // Collisions
        for (i in 0..200) {
            val node = EmptyNodeForTests(i, parser)
            buffer.add(node)

            Assertions.assertEquals(node, buffer.find(i, EmptyNodeForTests::class.java),
                    "The CommentSingleLine node is not properly added in the buffer")
        }

        return buffer
    }

    @Test
    fun `remove one by one`() {
        val buffer = `add and find entries test`()

        for (i in 0..200) {
            buffer.remove(i)

            Assertions.assertEquals(null, buffer.find(i, EmptyNodeForTests::class.java))
        }
    }

    @Test
    fun `remove all with position range`() {
        val buffer = `add and find entries test`()

        buffer.remove(0, 200)

        for (i in 0..200) {
            Assertions.assertEquals(null, buffer.find(i, EmptyNodeForTests::class.java))
        }
    }

    @Test
    fun `remove some positions with position range`() {
        val buffer = ForwardBuffer()
        val parser = listOf(EmptyNodeForTests(0, parser), EmptyNodeForTests(2, parser), EmptyNodeForTests(10, parser),
                EmptyNodeForTests(20, parser), EmptyNodeForTests(30, parser))

        parser.forEach {
            buffer.add(it)
        }

        buffer.remove(1, 15)

        Assertions.assertEquals(parser[0], buffer.find(parser[0].from.position(), EmptyNodeForTests::class.java))
        Assertions.assertEquals(null, buffer.find(parser[1].from.position(), EmptyNodeForTests::class.java))
        Assertions.assertEquals(null, buffer.find(parser[2].from.position(), EmptyNodeForTests::class.java))
        Assertions.assertEquals(parser[3], buffer.find(parser[3].from.position(), EmptyNodeForTests::class.java))
        Assertions.assertEquals(parser[4], buffer.find(parser[4].from.position(), EmptyNodeForTests::class.java))
    }

    @Test
    fun clear() {
        val buffer = `add and find entries test`()

        buffer.clear()

        for (i in 0..200) {
            Assertions.assertEquals(null, buffer.find(i, EmptyNodeForTests::class.java))
        }
    }
}
