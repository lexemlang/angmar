package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*
import java.util.stream.*
import kotlin.streams.*

internal class BinarySequenceLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = BitlistNodeTest.testExpression

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegative in listOf(false, true)) {
                    for (hasPropertyPostfix in listOf(false, true)) {
                        var text = BitlistNodeTest.testExpression

                        if (isNegative) {
                            text = BinarySequenceLexemeNode.notOperator + text
                        }

                        if (hasPropertyPostfix) {
                            text += LexemPropertyPostfixNodeTest.testExpression
                        }

                        yield(Arguments.of(text, isNegative, hasPropertyPostfix))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as BinarySequenceLexemeNode

            Assertions.assertFalse(node.isNegated, "The isNegated property is incorrect")
            Assertions.assertNull(node.propertyPostfix, "The propertyPostfix property must be null")

            BitlistNodeTest.checkTestExpression(node.bitlist)
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct node`(text: String, isNegative: Boolean, hasPropertyPostfix: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = BinarySequenceLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as BinarySequenceLexemeNode

        Assertions.assertEquals(isNegative, res.isNegated, "The isNegated property is incorrect")

        if (hasPropertyPostfix) {
            Assertions.assertNotNull(res.propertyPostfix, "The propertyPostfix property cannot be null")
            LexemPropertyPostfixNodeTest.checkTestExpression(res.propertyPostfix!!)
        } else {
            Assertions.assertNull(res.propertyPostfix, "The propertyPostfix property must be null")
        }

        BitlistNodeTest.checkTestExpression(res.bitlist)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", BinarySequenceLexemeNode.notOperator])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = BinarySequenceLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
