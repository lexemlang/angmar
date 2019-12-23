package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import java.util.stream.*
import kotlin.streams.*

internal class BitlistElementNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testBinaryExpression = "0101110_01110_01"
        const val testOctalExpression = "01_23456_7654_32_10"
        const val testHexadecimalExpression = "0_123_456_789a_bcde_fABCD_EF"

        @JvmStatic
        private fun provideNumberElements(): Stream<Arguments> {
            val result = sequence {
                for (radix in listOf(2, 8, 16)) {
                    for (i in listOf(0, 1, 25, 2556, 777, 589163)) {
                        val numText = i.toString(radix)
                        yield(Arguments.of(numText, radix))

                        if (numText.length > 2) {
                            yield(Arguments.of(numText.substring(0,
                                    numText.length / 2) + NumberNode.digitSeparator + numText.substring(
                                    numText.length / 2), radix))
                        }
                    }
                }
            }

            return result.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, number: String) {
            Assertions.assertTrue(node is BitlistElementNode, "The node is not a BitlistElementNode")
            node as BitlistElementNode

            Assertions.assertEquals(number.replace(NumberNode.digitSeparator, ""), node.number,
                    "The number property is incorrect")
            Assertions.assertNull(node.expression, "The expression property must be null")
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideNumberElements")
    fun `parse correct bitlist element`(text: String, radix: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = BitlistElementNode.parse(parser, ParserNode.Companion.EmptyParserNode, radix)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as BitlistElementNode

        Assertions.assertEquals(text.replace(NumberNode.digitSeparator, ""), res.number,
                "The number property is incorrect")
        Assertions.assertEquals(radix, res.radix, "The radix property is incorrect")
        Assertions.assertNull(res.expression, "The expression property must be null")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [EscapedExpressionNodeTest.testExpression])
    fun `parse correct bitlist element`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = BitlistElementNode.parse(parser, ParserNode.Companion.EmptyParserNode, 2) // The radix does not matter

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as BitlistElementNode

        Assertions.assertNull(res.number, "The number property must be null")
        Assertions.assertNotNull(res.expression, "The expression property cannot be null")
        Assertions.assertEquals(2, res.radix, "The radix property is incorrect")
        EscapedExpressionNodeTest.checkTestExpression(res.expression!!)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ObjectElementNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
