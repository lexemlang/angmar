package org.lexem.angmar.parser.descriptive.selectors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class MethodSelectorNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${MethodSelectorNode.relationalToken}${IdentifierNodeTest.testExpression}"

        @JvmStatic
        private fun provideMethods(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegated in listOf(false, true)) {
                    var text = MethodSelectorNode.relationalToken

                    if (isNegated) {
                        text += MethodSelectorNode.notOperator
                    }

                    text += IdentifierNodeTest.testExpression

                    // Without arguments.
                    yield(Arguments.of(text, isNegated, 0))

                    // With property block.
                    val text2 = "$text${PropertyBlockSelectorNodeTest.testExpression}"
                    yield(Arguments.of(text2, isNegated, 1))

                    // With selector.
                    val text3 =
                            "$text${MethodSelectorNode.selectorStartToken}${SelectorNodeTest.testExpression}${MethodSelectorNode.selectorEndToken}"
                    yield(Arguments.of(text3, isNegated, 2))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as MethodSelectorNode

            Assertions.assertFalse(node.isNegated, "The isNegated property is incorrect")
            IdentifierNodeTest.checkTestExpression(node.name)
            Assertions.assertNull(node.argument, "The arguments property must be null")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideMethods")
    fun `parse correct method`(text: String, isNegated: Boolean, argsType: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = MethodSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MethodSelectorNode

        Assertions.assertEquals(isNegated, res.isNegated, "The isNegated property is incorrect")
        IdentifierNodeTest.checkTestExpression(res.name)

        when (argsType) {
            0 -> {
                Assertions.assertNull(res.argument, "The arguments property must be null")
            }
            1 -> {
                Assertions.assertNotNull(res.argument, "The arguments property cannot be null")
                PropertyBlockSelectorNodeTest.checkTestExpression(res.argument!!, isAddition = false)
            }
            2 -> {
                Assertions.assertNotNull(res.argument, "The arguments property cannot be null")
                SelectorNodeTest.checkTestExpression(res.argument!!, isAddition = false)
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect method without name`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorMethodWithoutName) {
            val text = MethodSelectorNode.relationalToken
            val parser = LexemParser(IOStringReader.from(text))
            MethodSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = MethodSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

