package org.lexem.angmar.io.readers

import org.junit.jupiter.api.*
import org.lexem.angmar.errors.AngmarIOException
import java.io.*

internal class CustomStringReaderTest {
    @Test
    fun getSource() {
        var iter = CustomStringReader.from("xx")
        Assertions.assertEquals("<internal>", iter.getSource())

        val url = Thread.currentThread().contextClassLoader.getResource("./stringIteratorText.txt")
        val file = File(url.path)
        iter = CustomStringReader.from(file)
        Assertions.assertEquals(file.path, iter.getSource())
    }

    @Test
    fun currentPosition() {
        val text = "This is a long test".repeat(10)
        val iter = CustomStringReader.from(text)

        var index = 0
        for (char in text) {
            Assertions.assertEquals(index, iter.currentPosition())
            iter.advance()
            index += 1
        }
        Assertions.assertEquals(index, iter.currentPosition())
    }

    @Test
    fun nextCharAt() {
        val text = "abc"
        val iter = CustomStringReader.from(text)

        Assertions.assertThrows(AngmarIOException::class.java) {
            iter.nextCharAt(-1)
        }
        Assertions.assertEquals(text[0], iter.nextCharAt(0))
        Assertions.assertEquals(text[1], iter.nextCharAt())
        Assertions.assertEquals(text[1], iter.nextCharAt(1))
        Assertions.assertEquals(text[2], iter.nextCharAt(2))
        Assertions.assertNull(iter.nextCharAt(3))
    }

    @Test
    fun prevCharAt() {
        val text = "abc"
        val iter = CustomStringReader.from(text)
        iter.advance(2)

        Assertions.assertThrows(AngmarIOException::class.java) {
            iter.prevCharAt(-1)
        }
        Assertions.assertEquals(text[2], iter.prevCharAt(0))
        Assertions.assertEquals(text[1], iter.prevCharAt())
        Assertions.assertEquals(text[1], iter.prevCharAt(1))
        Assertions.assertEquals(text[0], iter.prevCharAt(2))
        Assertions.assertNull(iter.prevCharAt(3))
    }

    @Test
    fun charAt() {
        val text = "abc"
        val iter = CustomStringReader.from(text)

        Assertions.assertThrows(AngmarIOException::class.java) {
            iter.charAt(-1)
        }

        Assertions.assertEquals(text[0], iter.charAt(0))
        Assertions.assertEquals(text[1], iter.charAt(1))
        Assertions.assertEquals(text[2], iter.charAt(2))
        Assertions.assertNull(iter.charAt(3))

        iter.advance(3)

        Assertions.assertThrows(AngmarIOException::class.java) {
            iter.charAt(-1)
        }

        Assertions.assertEquals(text[0], iter.charAt(0))
        Assertions.assertEquals(text[1], iter.charAt(1))
        Assertions.assertEquals(text[2], iter.charAt(2))
        Assertions.assertNull(iter.charAt(3))
    }

    @Test
    fun isEnd() {
        var text = ""
        var iter = CustomStringReader.from(text)
        Assertions.assertTrue(iter.isEnd())

        text = "a"
        iter = CustomStringReader.from(text)
        Assertions.assertFalse(iter.isEnd())
        iter.advance()
        Assertions.assertTrue(iter.isEnd())

        text = "ab"
        iter = CustomStringReader.from(text)
        Assertions.assertFalse(iter.isEnd())
        iter.advance()
        Assertions.assertFalse(iter.isEnd())
        iter.advance()
        Assertions.assertTrue(iter.isEnd())
    }

    @Test
    fun advance() {
        val text = "abc"
        val iter = CustomStringReader.from(text)

        Assertions.assertEquals(0, iter.currentPosition())
        Assertions.assertTrue(iter.advance())
        Assertions.assertEquals(1, iter.currentPosition())
        Assertions.assertTrue(iter.advance())
        Assertions.assertEquals(2, iter.currentPosition())
        Assertions.assertTrue(iter.advance())
        Assertions.assertEquals(3, iter.currentPosition())
        Assertions.assertFalse(iter.advance())

        iter.restart()
        Assertions.assertEquals(0, iter.currentPosition())
        Assertions.assertTrue(iter.advance(2))
        Assertions.assertEquals(2, iter.currentPosition())
    }

    @Test
    fun back() {
        val text = "abc"
        val iter = CustomStringReader.from(text)
        iter.advance(3)

        Assertions.assertEquals(3, iter.currentPosition())
        Assertions.assertTrue(iter.back())
        Assertions.assertEquals(2, iter.currentPosition())
        Assertions.assertTrue(iter.back())
        Assertions.assertEquals(1, iter.currentPosition())
        Assertions.assertTrue(iter.back())
        Assertions.assertEquals(0, iter.currentPosition())
        Assertions.assertFalse(iter.back())

        iter.restart()
        iter.advance(3)
        Assertions.assertEquals(3, iter.currentPosition())
        Assertions.assertTrue(iter.back(2))
        Assertions.assertEquals(1, iter.currentPosition())
    }

    @Test
    fun setPosition() {
        val text = "abc"
        val iter = CustomStringReader.from(text)

        Assertions.assertTrue(iter.setPosition(0))
        Assertions.assertEquals(0, iter.currentPosition())
        Assertions.assertTrue(iter.setPosition(1))
        Assertions.assertEquals(1, iter.currentPosition())
        Assertions.assertTrue(iter.setPosition(2))
        Assertions.assertEquals(2, iter.currentPosition())
        Assertions.assertTrue(iter.setPosition(3))
        Assertions.assertEquals(3, iter.currentPosition())
        Assertions.assertFalse(iter.setPosition(4))

        Assertions.assertTrue(iter.setPosition(3))
        Assertions.assertEquals(3, iter.currentPosition())
        Assertions.assertTrue(iter.setPosition(2))
        Assertions.assertEquals(2, iter.currentPosition())
        Assertions.assertTrue(iter.setPosition(1))
        Assertions.assertEquals(1, iter.currentPosition())
        Assertions.assertTrue(iter.setPosition(0))
        Assertions.assertEquals(0, iter.currentPosition())
    }

    @Test
    fun saveCursor() {
        val text = "This is a long test".repeat(10)
        val iter = CustomStringReader.from(text)

        var index = 0
        for (char in text) {
            val cursor = iter.saveCursor()
            Assertions.assertEquals(index, cursor.position())
            Assertions.assertEquals(char, cursor.char())
            Assertions.assertEquals(iter, cursor.getReader())
            iter.advance()
            index += 1
        }

        val cursor = iter.saveCursor()
        Assertions.assertEquals(index, cursor.position())
        Assertions.assertNull(cursor.char())
        Assertions.assertEquals(iter, cursor.getReader())
    }

    @Test
    fun restoreCursor() {
        val text = "This is a long test"
        val iter = CustomStringReader.from(text)

        val cursor = iter.saveCursor()
        repeat(5) {
            iter.advance()
        }

        Assertions.assertEquals(5, iter.currentPosition())
        Assertions.assertEquals(0, cursor.position())
        Assertions.assertEquals(text[0], cursor.char())
        Assertions.assertEquals(iter, cursor.getReader())

        cursor.restore()

        Assertions.assertEquals(0, iter.currentPosition())
    }

    @Test
    fun restart() {
        val text = "abc"
        val iter = CustomStringReader.from(text)

        Assertions.assertEquals(0, iter.currentPosition())
        iter.restart()
        Assertions.assertEquals(0, iter.currentPosition())
        iter.advance()
        Assertions.assertNotEquals(0, iter.currentPosition())
        iter.restart()
        Assertions.assertEquals(0, iter.currentPosition())
    }

    @Test
    fun readAllText() {
        val text = "test"
        var iter = CustomStringReader.from(text)
        Assertions.assertEquals(text, iter.readAllText())

        val url = Thread.currentThread().contextClassLoader.getResource("./stringIteratorText.txt")
        val file = File(url.path)
        val fileContent = file.readText()
        iter = CustomStringReader.from(file)
        Assertions.assertEquals(fileContent, iter.readAllText())
    }
}