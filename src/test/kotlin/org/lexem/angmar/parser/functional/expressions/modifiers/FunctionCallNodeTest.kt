package org.lexem.angmar.parser.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class FunctionCallNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${FunctionCallNode.startToken}${FunctionCallNamedArgumentNodeTest.testExpression}${FunctionCallNode.endToken}"

        @JvmStatic
        private fun provideCorrectFunctionCalls(): Stream<Arguments> {
            val sequence = sequence {
                for (positionalCount in 0..3) {
                    val positionalArguments = List(positionalCount) { ExpressionsCommonsTest.testExpression }

                    for (namedCount in 0..3) {
                        val namedArguments = List(namedCount) { FunctionCallNamedArgumentNodeTest.testExpression }

                        for (spreadCount in 0..3) {
                            val spreadArguments =
                                    List(spreadCount) { "${FunctionCallNode.spreadOperator}${ExpressionsCommonsTest.testExpression}" }

                            for (properties in 0..1) {
                                val arguments = positionalArguments + namedArguments + spreadArguments
                                var text = arguments.joinToString(" ${FunctionCallNode.argumentSeparator} ")
                                text = "${FunctionCallNode.startToken} $text ${FunctionCallNode.endToken}"

                                if (properties == 1) {
                                    text += FunctionCallExpressionPropertiesNodeTest.testExpression
                                }

                                yield(Arguments.of(text, positionalCount, namedCount, spreadCount, properties == 1))


                                text = arguments.joinToString(FunctionCallNode.argumentSeparator)
                                text = "${FunctionCallNode.startToken}$text${FunctionCallNode.endToken}"

                                if (properties == 1) {
                                    text += FunctionCallExpressionPropertiesNodeTest.testExpression
                                }

                                yield(Arguments.of(text, positionalCount, namedCount, spreadCount, properties == 1))
                            }
                        }
                    }
                }
            }

            return sequence.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is FunctionCallNode, "The node is not a FunctionCallNode")
            node as FunctionCallNode

            Assertions.assertEquals(0, node.positionalArguments.size, "The number of positionalArguments is incorrect")
            Assertions.assertEquals(1, node.namedArguments.size, "The number of namedArguments is incorrect")
            Assertions.assertEquals(0, node.spreadArguments.size, "The number of spreadArguments is incorrect")
            Assertions.assertNull(node.propertiesExpression, "The expressionProperties property must be null")
            FunctionCallNamedArgumentNodeTest.checkTestExpression(node.namedArguments.first())
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectFunctionCalls")
    fun `parse correct function call`(text: String, positionalCount: Int, namedCount: Int, spreadCount: Int,
            hasExpressionProperties: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionCallNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionCallNode

        Assertions.assertEquals(positionalCount, res.positionalArguments.size,
                "The number of positionalArguments is incorrect")
        Assertions.assertEquals(namedCount, res.namedArguments.size, "The number of namedArguments is incorrect")
        Assertions.assertEquals(spreadCount, res.spreadArguments.size, "The number of spreadArguments is incorrect")

        if (hasExpressionProperties) {
            Assertions.assertNotNull(res.propertiesExpression, "The expressionProperties property cannot be null")
            FunctionCallExpressionPropertiesNodeTest.checkTestExpression(res.propertiesExpression!!)
        } else {
            Assertions.assertNull(res.propertiesExpression, "The expressionProperties property must be null")
        }

        for (arg in res.positionalArguments) {
            ExpressionsCommonsTest.checkTestExpression(arg)
        }

        for (arg in res.namedArguments) {
            FunctionCallNamedArgumentNodeTest.checkTestExpression(arg)
        }

        for (arg in res.spreadArguments) {
            ExpressionsCommonsTest.checkTestExpression(arg)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect function call without expression after spread element`() {
        TestUtils.assertParserException {
            val text = "${FunctionCallNode.startToken}${FunctionCallNode.spreadOperator}${FunctionCallNode.endToken}"
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionCallNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect function call without close parenthesis`() {
        TestUtils.assertParserException {
            val text = FunctionCallNode.startToken
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionCallNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionCallNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

