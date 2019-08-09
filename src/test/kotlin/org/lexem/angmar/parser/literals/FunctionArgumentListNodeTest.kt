package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class FunctionArgumentListNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${FunctionArgumentListNode.startToken}${ExpressionsCommonsTest.testExpression}${FunctionArgumentListNode.endToken}"

        @JvmStatic
        private fun provideCorrectArguments(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..6) {
                    yield(Arguments.of(List(i) { FunctionArgumentNodeTest.correctFunctionArgument }.joinToString(
                            FunctionArgumentListNode.argumentSeparator), i))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideCorrectArgumentsWithValues(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..6) {
                    yield(Arguments.of(
                            List(i) { FunctionArgumentNodeTest.correctFunctionArgumentWithValue }.joinToString(
                                    FunctionArgumentListNode.argumentSeparator), i))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideCorrectArgumentsWithValuesAndWS(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..6) {
                    yield(Arguments.of(
                            List(i) { FunctionArgumentNodeTest.correctFunctionArgumentWithValueAndWS }.joinToString(
                                    " ${FunctionArgumentListNode.argumentSeparator} "), i))
                }
            }

            return result.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is FunctionArgumentListNode, "The node is not a FunctionArgumentListNode")
            node as FunctionArgumentListNode

            Assertions.assertEquals(1, node.arguments.size, "The number of elements did not match")
            Assertions.assertNull(node.spread, "The spread property must be null")

            ExpressionsCommonsTest.checkTestExpression(node.arguments.first())
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectArguments")
    fun `parse correct argument list`(arguments: String, size: Int) {
        val text = "${FunctionArgumentListNode.startToken}$arguments${FunctionArgumentListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionArgumentListNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionArgumentListNode

        Assertions.assertEquals(size, res.arguments.size, "The number of elements did not match")
        Assertions.assertNull(res.spread, "The spread property must be null")

        for (exp in res.arguments) {
            FunctionArgumentNodeTest.checkTestExpression(exp, false)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectArgumentsWithValues")
    fun `parse correct argument list with values`(arguments: String, size: Int) {
        val text = "${FunctionArgumentListNode.startToken}$arguments${FunctionArgumentListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionArgumentListNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionArgumentListNode

        Assertions.assertEquals(size, res.arguments.size, "The number of elements did not match")
        Assertions.assertNull(res.spread, "The spread property must be null")

        for (exp in res.arguments) {
            FunctionArgumentNodeTest.checkTestExpression(exp, true)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectArgumentsWithValuesAndWS")
    fun `parse correct argument list with whitespaces`(arguments: String, size: Int) {
        val text = "${FunctionArgumentListNode.startToken} $arguments ${FunctionArgumentListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionArgumentListNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionArgumentListNode

        Assertions.assertEquals(size, res.arguments.size, "The number of elements did not match")
        Assertions.assertNull(res.spread, "The spread property must be null")

        for (exp in res.arguments) {
            FunctionArgumentNodeTest.checkTestExpression(exp, true)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectArguments")
    fun `parse correct argument list with trailing comma`(arguments: String, size: Int) {
        val text =
                "${FunctionArgumentListNode.startToken}$arguments${FunctionArgumentListNode.argumentSeparator}${FunctionArgumentListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionArgumentListNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionArgumentListNode

        Assertions.assertEquals(size, res.arguments.size, "The number of elements did not match")
        Assertions.assertNull(res.spread, "The spread property must be null")

        for (exp in res.arguments) {
            FunctionArgumentNodeTest.checkTestExpression(exp, false)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectArguments")
    fun `parse correct argument list with spread element`(arguments: String, size: Int) {
        val text = if (size > 0) {
            "${FunctionArgumentListNode.startToken}$arguments${FunctionArgumentListNode.argumentSeparator}${FunctionArgumentListNode.spreadOperator}${IdentifierNodeTest.testExpression}${FunctionArgumentListNode.endToken}"
        } else {
            "${FunctionArgumentListNode.startToken}${FunctionArgumentListNode.spreadOperator}${IdentifierNodeTest.testExpression}${FunctionArgumentListNode.endToken}"
        }
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionArgumentListNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionArgumentListNode

        Assertions.assertEquals(size, res.arguments.size, "The number of elements did not match")
        IdentifierNodeTest.checkTestExpression(res.spread!!)

        for (exp in res.arguments) {
            FunctionArgumentNodeTest.checkTestExpression(exp, false)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect argument list with no expression`() {
        assertParserException {
            val text = FunctionArgumentListNode.startToken
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionArgumentListNode.parse(parser)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse incorrect argument list with no endToken`(expression: String) {
        assertParserException {
            val text = "${FunctionArgumentListNode.startToken}$expression"
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionArgumentListNode.parse(parser)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect argument list with no expression after spread operator`() {
        assertParserException {
            val text =
                    "${FunctionArgumentListNode.startToken}${FunctionArgumentListNode.spreadOperator}${FunctionArgumentListNode.endToken}"
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionArgumentListNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionArgumentListNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

