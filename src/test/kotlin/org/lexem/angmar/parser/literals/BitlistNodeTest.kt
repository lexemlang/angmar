package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class BitlistNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${BitlistElementNodeTest.testHexadecimalExpression}${BitlistNode.endToken}"

        val prefix = listOf(BitlistNode.binaryPrefix, BitlistNode.octalPrefix, BitlistNode.hexadecimalPrefix)

        @JvmStatic
        private fun provideCorrectElements(): Stream<Arguments> {
            val result = sequence {
                for (radixPrefix in listOf(2, 8, 16).zip(prefix)) {
                    for (i in 0..3) {
                        val expression = when (radixPrefix.first) {
                            2 -> BitlistElementNodeTest.testBinaryExpression
                            8 -> BitlistElementNodeTest.testOctalExpression
                            16 -> BitlistElementNodeTest.testHexadecimalExpression
                            else -> throw AngmarUnreachableException()
                        }

                        val numList = List(i) { expression }.joinToString(" ")
                        yield(Arguments.of(
                                "${radixPrefix.second}${BitlistNode.startToken}$numList${BitlistNode.endToken}",
                                radixPrefix.first, i, expression))
                    }
                }
            }

            return result.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is BitlistNode, "The node is not a BitlistNode")
            node as BitlistNode

            Assertions.assertEquals(16, node.radix, "The number property is incorrect")
            Assertions.assertEquals(1, node.elements.size, "The number of elements is incorrect")
            BitlistElementNodeTest.checkTestExpression(node.elements.first(),
                    BitlistElementNodeTest.testHexadecimalExpression)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectElements")
    fun `parse correct bitlist literal`(text: String, radix: Int, numElements: Int, textNumber: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = BitlistNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as BitlistNode

        Assertions.assertEquals(radix, res.radix, "The number property is incorrect")
        Assertions.assertEquals(numElements, res.elements.size, "The number of elements is incorrect")

        for (el in res.elements) {
            BitlistElementNodeTest.checkTestExpression(el, textNumber)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect bitlist literal without endToken`() {
        TestUtils.assertParserException {
            val text =
                    "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${BitlistElementNodeTest.testHexadecimalExpression}"
            val parser = LexemParser(CustomStringReader.from(text))
            BitlistNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = BitlistNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

