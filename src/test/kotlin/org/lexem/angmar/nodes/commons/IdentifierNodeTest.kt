package org.lexem.angmar.nodes.commons

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader
import kotlin.random.*

internal class IdentifierNodeTest {
    @Test
    fun `parse simple identifier`() {
        // 1 char: H
        for (hChar in IdentifierNode.headChars) {
            okCheck("${hChar.first}")
        }

        // 2 chars: H H
        for (hChar in IdentifierNode.headChars) {
            okCheck("${hChar.first}${hChar.last}")
        }

        // 2 chars: H E
        for (hChar in IdentifierNode.headChars) {
            for (eChar in IdentifierNode.endChars) {
                okCheck("${hChar.first}${hChar.first}")
            }
        }

        // n chars
        for (size in 1..100) {
            for (repetitions in 0..100) {
                okCheck(generateSimpleIdentifier(size))
            }
        }
    }

    @Test
    fun `parse complex identifier`() {
        for (size in 1..100) {
            for (repetitions in 0..100) {
                okCheck(generateComplexIdentifier(size))
            }
        }
    }

    @Test
    fun `parse quoted identifier`() {
        val id = "this is an example with escapes \\k \\ll"
        val text = "${QuotedIdentifierNode.startQuote}$id${QuotedIdentifierNode.endQuote}"
        val res = IdentifierNode.parse(
            LexemParser(
                CustomStringReader.from(
                    text
                )
            )
        )
        Assertions.assertNotNull(res, text)
        res as IdentifierNode

        Assertions.assertEquals(text, res.content)
        Assertions.assertTrue(res.isQuotedIdentifier)
        Assertions.assertNotNull(res.quotedIdentifier)
        Assertions.assertEquals(text, res.toString())
    }

    @Test
    fun `parse incorrect identifiers`() {
        for (nextCharRange in IdentifierNode.endChars) {
            val text = "${((nextCharRange.last.toInt() + nextCharRange.first.toInt()) / 2).toChar()}"
            val res = IdentifierNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNull(res, text)
        }
    }

    @Test
    fun `check identifier`() {
        let {
            val text = "a"
            val res = IdentifierNode.checkIdentifier(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertTrue(res)
        }

        let {
            val text = ""
            val res = IdentifierNode.checkIdentifier(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertFalse(res)
        }
    }

    @Test
    fun `parse a correct specific keyword`() {
        for (keyword in IdentifierNode.keywords) {
            val res = IdentifierNode.parseKeyword(
                LexemParser(
                    CustomStringReader.from(
                        keyword
                    )
                ), keyword)

            Assertions.assertTrue(res, keyword)
        }
    }

    @Test
    fun `parse an incorrect specific keyword`() {
        for (keyword in IdentifierNode.keywords) {
            val text = "${keyword}a"
            val res = IdentifierNode.parseKeyword(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                ), keyword)

            Assertions.assertFalse(res, text)
        }
    }

    @Test
    fun `parse any correct keyword`() {
        for (keyword in IdentifierNode.keywords) {
            val res = IdentifierNode.parseAnyKeyword(
                LexemParser(
                    CustomStringReader.from(
                        keyword
                    )
                )
            )

            Assertions.assertEquals(res, keyword)
        }
    }

    @Test
    fun `parse any incorrect keyword`() {
        for (keyword in IdentifierNode.keywords) {
            val text = "${keyword}a"
            val res = IdentifierNode.parseAnyKeyword(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNull(res, keyword)
        }
    }

    // AUX METHODS ------------------------------------------------------------

    private fun generateComplexIdentifier(size: Int): String {
        var res = generateSimpleIdentifier(Random.nextInt(12))

        for (i in 0..size) {
            res += "${IdentifierNode.middleChar}${generateSimpleIdentifier(Random.nextInt(12))}"
        }

        return res
    }

    private fun generateSimpleIdentifier(size: Int): String {
        var range = IdentifierNode.headChars[Random.nextInt(0, IdentifierNode.headChars.size)]
        var res = "${Random.nextInt(range.first.toInt(), range.last.toInt() + 1).toChar()}"

        while (res.length < size) {
            if (Random.nextBoolean()) {
                range = IdentifierNode.headChars[Random.nextInt(0, IdentifierNode.headChars.size)]
                res += "${Random.nextInt(range.first.toInt(), range.last.toInt() + 1).toChar()}"
            } else {
                range = IdentifierNode.endChars[Random.nextInt(0, IdentifierNode.endChars.size)]
                res += "${Random.nextInt(range.first.toInt(), range.last.toInt() + 1).toChar()}"
            }
        }

        return res
    }

    private fun okCheck(text: String) {
        val res = IdentifierNode.parse(
            LexemParser(
                CustomStringReader.from(
                    text
                )
            )
        )

        Assertions.assertNotNull(res, text)
        res as IdentifierNode

        Assertions.assertEquals(text, res.content)
        Assertions.assertFalse(res.isQuotedIdentifier)
        Assertions.assertNull(res.quotedIdentifier)
        Assertions.assertEquals(text.split(IdentifierNode.middleChar).size, res.simpleIdentifiers.size, text)
        for (word in text.split(IdentifierNode.middleChar).withIndex()) {
            Assertions.assertEquals(word.value, res.simpleIdentifiers[word.index], text)
        }
        Assertions.assertEquals(text, res.toString())
    }
}