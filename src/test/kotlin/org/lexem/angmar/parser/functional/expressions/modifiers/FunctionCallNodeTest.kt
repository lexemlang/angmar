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
                "${FunctionCallNode.startToken}${FunctionCallMiddleArgumentNodeTest.testExpression}${FunctionCallNode.endToken}"

        @JvmStatic
        private fun provideCorrectFunctionCalls(): Stream<Arguments> {
            val sequence = sequence {
                for (i in 0..3) {
                    for (spread in 0..1) {
                        for (properties in 0..1) {
                            for (firstExpression in 0..1) {
                                var argCount = i
                                val arguments = MutableList(i) { FunctionCallMiddleArgumentNodeTest.testExpression }

                                if (firstExpression == 1) {
                                    arguments.add(0, ExpressionsCommonsTest.testExpression)
                                    argCount += 1
                                }

                                if (spread == 1) {
                                    arguments.add(
                                            "${FunctionCallNode.spreadOperator}${ExpressionsCommonsTest.testExpression}")
                                }

                                var text = arguments.joinToString(" ${FunctionCallNode.argumentSeparator} ")
                                text = "${FunctionCallNode.startToken} $text ${FunctionCallNode.endToken}"

                                if (properties == 1) {
                                    text += FunctionCallExpressionPropertiesNodeTest.testExpression
                                }

                                yield(Arguments.of(text, argCount, firstExpression == 1, spread == 1, properties == 1))


                                text = arguments.joinToString(FunctionCallNode.argumentSeparator)
                                text = "${FunctionCallNode.startToken}$text${FunctionCallNode.endToken}"

                                if (properties == 1) {
                                    text += FunctionCallExpressionPropertiesNodeTest.testExpression
                                }

                                yield(Arguments.of(text, argCount, firstExpression == 1, spread == 1, properties == 1))
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

            Assertions.assertEquals(1, node.arguments.size, "The number of arguments is incorrect")
            Assertions.assertNull(node.spreadArgument, "The spreadArgument property must be null")
            Assertions.assertNull(node.expressionProperties, "The expressionProperties property must be null")
            FunctionCallMiddleArgumentNodeTest.checkTestExpression(node.arguments.first())
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectFunctionCalls")
    fun `parse correct function call`(text: String, numArguments: Int, isFirstExpression: Boolean,
            hasSpreadArgument: Boolean, hasExpressionProperties: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionCallNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionCallNode

        Assertions.assertEquals(numArguments, res.arguments.size, "The number of arguments is incorrect")

        if (hasSpreadArgument) {
            Assertions.assertNotNull(res.spreadArgument, "The spreadArgument property cannot be null")
            ExpressionsCommonsTest.checkTestExpression(res.spreadArgument!!)
        } else {
            Assertions.assertNull(res.spreadArgument, "The spreadArgument property must be null")
        }

        if (hasExpressionProperties) {
            Assertions.assertNotNull(res.expressionProperties, "The expressionProperties property cannot be null")
            FunctionCallExpressionPropertiesNodeTest.checkTestExpression(res.expressionProperties!!)
        } else {
            Assertions.assertNull(res.expressionProperties, "The expressionProperties property must be null")
        }

        var drop = 0
        if (isFirstExpression) {
            drop = 1
            ExpressionsCommonsTest.checkTestExpression(res.arguments.first())
        }

        for (arg in res.arguments.asSequence().drop(drop)) {
            FunctionCallMiddleArgumentNodeTest.checkTestExpression(arg)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect function call without expression after spread element`() {
        assertParserException {
            val text = "${FunctionCallNode.startToken}${FunctionCallNode.spreadOperator}${FunctionCallNode.endToken}"
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionCallNode.parse(parser)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect function call without close parenthesis`() {
        assertParserException {
            val text = "${FunctionCallNode.startToken}"
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionCallNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionCallNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

