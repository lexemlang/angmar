package org.lexem.angmar

import org.junit.jupiter.api.*
import org.lexem.angmar.io.readers.CustomStringReader

internal class LexemParserTest {
    @Test
    fun checkAnyCharText() {
        val text = "abc"
        val list1 = "abc"
        val list2 = "xyz"

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.checkAnyChar(list1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        check = parser.checkAnyChar(list2)

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.checkAnyChar("a")

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun readAnyCharText() {
        val text = "abc"
        val list1 = "abc"
        val list2 = "xyz"

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.readAnyChar(list1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readAnyChar(list2)

        Assertions.assertNull(check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readAnyChar(list1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[1], check)
        Assertions.assertEquals(2, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.readAnyChar("a")

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun checkNegativeAnyCharText() {
        val text = "abc"
        val list1 = "xyz"
        val list2 = "abc"

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.checkNegativeAnyChar(list1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        check = parser.checkNegativeAnyChar(list2)

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.checkNegativeAnyChar("a")

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun readNegativeAnyCharText() {
        val text = "abc"
        val list1 = "xyz"
        val list2 = "abc"

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.readNegativeAnyChar(list1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readNegativeAnyChar(list2)

        Assertions.assertNull(check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readNegativeAnyChar(list1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[1], check)
        Assertions.assertEquals(2, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.readNegativeAnyChar("a")

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun checkAnyCharSetOfChars() {
        val text = "abc"
        val set1 = listOf('a'..'d', 'e'..'g')
        val set2 = listOf('p'..'t', 'x'..'z')

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.checkAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        check = parser.checkAnyChar(set2)

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.checkAnyChar(listOf('a'..'a'))

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun readAnyCharSetOfChars() {
        val text = "abc"
        val set1 = listOf('a'..'d', 'e'..'g')
        val set2 = listOf('p'..'t', 'x'..'z')

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.readAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readAnyChar(set2)

        Assertions.assertNull(check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[1], check)
        Assertions.assertEquals(2, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.readAnyChar(listOf('a'..'a'))

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun checkNegativeAnyCharSetOfChars() {
        val text = "abc"
        val set1 = listOf('p'..'t', 'x'..'z')
        val set2 = listOf('a'..'d', 'e'..'g')

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.checkNegativeAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        check = parser.checkNegativeAnyChar(set2)

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.checkNegativeAnyChar(listOf('a'..'a'))

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun readNegativeAnyCharSetOfChars() {
        val text = "abc"
        val set1 = listOf('p'..'t', 'x'..'z')
        val set2 = listOf('a'..'d', 'e'..'g')

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.readNegativeAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readNegativeAnyChar(set2)

        Assertions.assertNull(check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readNegativeAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[1], check)
        Assertions.assertEquals(2, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.readNegativeAnyChar(listOf('a'..'a'))

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun checkAnyCharListOfCharRanges() {
        val text = "abc"
        val set1 = setOf('a', 'd', 'e')
        val set2 = setOf('x', 'y', 'z')

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.checkAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        check = parser.checkAnyChar(set2)

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.checkAnyChar(setOf('a'))

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun readAnyCharListOfCharRanges() {
        val text = "abc"
        val set1 = setOf('a', 'b', 'c')
        val set2 = setOf('x', 'y', 'z')

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.readAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readAnyChar(set2)

        Assertions.assertNull(check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[1], check)
        Assertions.assertEquals(2, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.readAnyChar(setOf('a'))

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun checkNegativeAnyCharListOfCharRanges() {
        val text = "abc"
        val set1 = setOf('x', 'y', 'z')
        val set2 = setOf('a', 'd', 'e')

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.checkNegativeAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        check = parser.checkNegativeAnyChar(set2)

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.checkNegativeAnyChar(setOf('a'))

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun readNegativeAnyCharListOfCharRanges() {
        val text = "abc"
        val set1 = setOf('x', 'y', 'z')
        val set2 = setOf('a', 'b', 'c')

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.readNegativeAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[0], check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readNegativeAnyChar(set2)

        Assertions.assertNull(check)
        Assertions.assertEquals(1, parser.reader.currentPosition())

        check = parser.readNegativeAnyChar(set1)

        Assertions.assertNotNull(check)
        Assertions.assertEquals(text[1], check)
        Assertions.assertEquals(2, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.readNegativeAnyChar(setOf('a'))

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun checkText() {
        val text = "abc"
        val text1 = "abc"
        val text2 = "cba"

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.checkText(text1)

        Assertions.assertTrue(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        check = parser.checkText(text2)

        Assertions.assertFalse(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.checkText("a")

        Assertions.assertFalse(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun readText() {
        val text = "abcabc"
        val text1 = "abc"
        val text2 = "cba"

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.readText(text1)

        Assertions.assertTrue(check)
        Assertions.assertEquals(text1.length, parser.reader.currentPosition())

        check = parser.readText(text2)

        Assertions.assertFalse(check)
        Assertions.assertEquals(text1.length, parser.reader.currentPosition())

        check = parser.readText(text1)

        Assertions.assertTrue(check)
        Assertions.assertEquals(text1.length * 2, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.readText("a")

        Assertions.assertFalse(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun checkAnyText() {
        val text = "abcabc"
        val okText = "abc"
        val text1 = listOf("x", "yasu", okText, "alfa")
        val text2 = listOf("xxx", "999", "alfa")

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.checkAnyText(text1)

        Assertions.assertEquals(okText, check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        check = parser.checkAnyText(text2)

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.checkAnyText(text1)

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun checkNegativeAnyText() {
        val text = "abcabc"
        val okText = "abc"
        val text1 = listOf("x", "yasu", okText, "alfa")
        val text2 = listOf("xxx", "999", "alfa")

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.checkNegativeAnyText(text1)

        Assertions.assertFalse(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        check = parser.checkNegativeAnyText(text2)

        Assertions.assertTrue(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.checkNegativeAnyText(text1)

        Assertions.assertTrue(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun readAnyText() {
        val text = "abcxxx"
        val okText = "abc"
        val okText2 = "xxx"
        val text1 = listOf(okText2, "999", "alfa")
        val text2 = listOf("x", "yasu", okText, "alfa")

        var parser = LexemParser(CustomStringReader.from(text))
        var check = parser.readAnyText(text1)

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())

        check = parser.readAnyText(text2)

        Assertions.assertEquals(okText, check)
        Assertions.assertEquals(okText.length, parser.reader.currentPosition())

        check = parser.readAnyText(text1)

        Assertions.assertEquals(okText2, check)
        Assertions.assertEquals(okText.length + okText2.length, parser.reader.currentPosition())

        // EOF case
        parser = LexemParser(CustomStringReader.from(""))
        check = parser.readAnyText(text1)

        Assertions.assertNull(check)
        Assertions.assertEquals(0, parser.reader.currentPosition())
    }

    @Test
    fun substring() {
        val text = "This is a text"
        val word = "This"

        val parser = LexemParser(CustomStringReader.from(text))
        val initCursor = parser.reader.saveCursor()
        parser.readText(word)
        var endCursor = parser.reader.saveCursor()
        var substr = parser.substring(initCursor, endCursor)

        Assertions.assertEquals(word.length, parser.reader.currentPosition())
        Assertions.assertEquals(word, substr)

        substr = parser.substring(initCursor, initCursor)
        Assertions.assertEquals("", substr)

        repeat(text.length) {
            parser.reader.advance()
        }

        endCursor = parser.reader.saveCursor()
        substr = parser.substring(endCursor, endCursor)
        Assertions.assertEquals("", substr)
    }
}