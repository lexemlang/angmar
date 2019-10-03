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

internal class FunctionParameterListNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${FunctionParameterListNode.startToken} ${FunctionParameterNodeTest.correctFunctionParameterWithValue} ${FunctionParameterListNode.endToken}"

        @JvmStatic
        private fun provideCorrectParameters(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..6) {
                    yield(Arguments.of(List(i) { FunctionParameterNodeTest.correctFunctionParameter }.joinToString(
                            FunctionParameterListNode.parameterSeparator), i))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideCorrectParametersWithValues(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..6) {
                    yield(Arguments.of(
                            List(i) { FunctionParameterNodeTest.correctFunctionParameterWithValue }.joinToString(
                                    FunctionParameterListNode.parameterSeparator), i))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideCorrectParametersWithValuesAndWS(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..6) {
                    yield(Arguments.of(
                            List(i) { FunctionParameterNodeTest.correctFunctionParameterWithValueAndWS }.joinToString(
                                    " ${FunctionParameterListNode.parameterSeparator} "), i))
                }
            }

            return result.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is FunctionParameterListNode, "The node is not a FunctionParameterListNode")
            node as FunctionParameterListNode

            Assertions.assertEquals(1, node.parameters.size, "The number of elements did not match")
            Assertions.assertNull(node.positionalSpread, "The positionalSpread property must be null")
            Assertions.assertNull(node.namedSpread, "The namedSpread property must be null")

            FunctionParameterNodeTest.checkTestExpression(node.parameters.first(), true)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectParameters")
    fun `parse correct parameter list`(parameters: String, size: Int) {
        val text = "${FunctionParameterListNode.startToken}$parameters${FunctionParameterListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionParameterListNode

        Assertions.assertEquals(size, res.parameters.size, "The number of elements did not match")
        Assertions.assertNull(res.positionalSpread, "The positionalSpread property must be null")
        Assertions.assertNull(res.namedSpread, "The namedSpread property must be null")

        for (exp in res.parameters) {
            FunctionParameterNodeTest.checkTestExpression(exp, false)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectParametersWithValues")
    fun `parse correct parameter list with values`(parameters: String, size: Int) {
        val text = "${FunctionParameterListNode.startToken}$parameters${FunctionParameterListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionParameterListNode

        Assertions.assertEquals(size, res.parameters.size, "The number of elements did not match")
        Assertions.assertNull(res.positionalSpread, "The positionalSpread property must be null")
        Assertions.assertNull(res.namedSpread, "The namedSpread property must be null")

        for (exp in res.parameters) {
            FunctionParameterNodeTest.checkTestExpression(exp, true)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectParametersWithValuesAndWS")
    fun `parse correct parameter list with whitespaces`(parameters: String, size: Int) {
        val text = "${FunctionParameterListNode.startToken} $parameters ${FunctionParameterListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionParameterListNode

        Assertions.assertEquals(size, res.parameters.size, "The number of elements did not match")
        Assertions.assertNull(res.positionalSpread, "The positionalSpread property must be null")
        Assertions.assertNull(res.namedSpread, "The namedSpread property must be null")

        for (exp in res.parameters) {
            FunctionParameterNodeTest.checkTestExpression(exp, true)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectParameters")
    fun `parse correct parameter list with trailing comma`(parameters: String, size: Int) {
        val text =
                "${FunctionParameterListNode.startToken}$parameters${FunctionParameterListNode.parameterSeparator}${FunctionParameterListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionParameterListNode

        Assertions.assertEquals(size, res.parameters.size, "The number of elements did not match")
        Assertions.assertNull(res.positionalSpread, "The positionalSpread property must be null")
        Assertions.assertNull(res.namedSpread, "The namedSpread property must be null")

        for (exp in res.parameters) {
            FunctionParameterNodeTest.checkTestExpression(exp, false)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectParameters")
    fun `parse correct parameter list with positional spread element`(parameters: String, size: Int) {
        val text = if (size > 0) {
            "${FunctionParameterListNode.startToken}$parameters${FunctionParameterListNode.parameterSeparator}${FunctionParameterListNode.positionalSpreadOperator}${IdentifierNodeTest.testExpression}${FunctionParameterListNode.endToken}"
        } else {
            "${FunctionParameterListNode.startToken}${FunctionParameterListNode.positionalSpreadOperator}${IdentifierNodeTest.testExpression}${FunctionParameterListNode.endToken}"
        }
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionParameterListNode

        Assertions.assertEquals(size, res.parameters.size, "The number of elements did not match")
        Assertions.assertNotNull(res.positionalSpread, "The positionalSpread property cannot be null")
        Assertions.assertNull(res.namedSpread, "The namedSpread property must be null")
        IdentifierNodeTest.checkTestExpression(res.positionalSpread!!)

        for (exp in res.parameters) {
            FunctionParameterNodeTest.checkTestExpression(exp, false)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectParameters")
    fun `parse correct parameter list with named spread element`(parameters: String, size: Int) {
        val text = if (size > 0) {
            "${FunctionParameterListNode.startToken}$parameters${FunctionParameterListNode.parameterSeparator}${FunctionParameterListNode.namedSpreadOperator}${IdentifierNodeTest.testExpression}${FunctionParameterListNode.endToken}"
        } else {
            "${FunctionParameterListNode.startToken}${FunctionParameterListNode.namedSpreadOperator}${IdentifierNodeTest.testExpression}${FunctionParameterListNode.endToken}"
        }
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionParameterListNode

        Assertions.assertEquals(size, res.parameters.size, "The number of elements did not match")
        Assertions.assertNull(res.positionalSpread, "The positionalSpread property must be null")
        Assertions.assertNotNull(res.namedSpread, "The namedSpread property cannot be null")
        IdentifierNodeTest.checkTestExpression(res.namedSpread!!)

        for (exp in res.parameters) {
            FunctionParameterNodeTest.checkTestExpression(exp, false)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectParameters")
    fun `parse correct parameter list with both spread elements`(parameters: String, size: Int) {
        val text = if (size > 0) {
            "${FunctionParameterListNode.startToken}$parameters${FunctionParameterListNode.parameterSeparator}${FunctionParameterListNode.positionalSpreadOperator}${IdentifierNodeTest.testExpression}${FunctionParameterListNode.parameterSeparator}${FunctionParameterListNode.namedSpreadOperator}${IdentifierNodeTest.testExpression}${FunctionParameterListNode.endToken}"
        } else {
            "${FunctionParameterListNode.startToken}${FunctionParameterListNode.positionalSpreadOperator}${IdentifierNodeTest.testExpression}${FunctionParameterListNode.parameterSeparator}${FunctionParameterListNode.namedSpreadOperator}${IdentifierNodeTest.testExpression}${FunctionParameterListNode.endToken}"
        }
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionParameterListNode

        Assertions.assertEquals(size, res.parameters.size, "The number of elements did not match")
        Assertions.assertNotNull(res.positionalSpread, "The positionalSpread property cannot be null")
        Assertions.assertNotNull(res.namedSpread, "The namedSpread property cannot be null")
        IdentifierNodeTest.checkTestExpression(res.positionalSpread!!)
        IdentifierNodeTest.checkTestExpression(res.namedSpread!!)

        for (exp in res.parameters) {
            FunctionParameterNodeTest.checkTestExpression(exp, false)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect parameter list with no expression`() {
        TestUtils.assertParserException {
            val text = FunctionParameterListNode.startToken
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse incorrect parameter list with no endToken`(expression: String) {
        TestUtils.assertParserException {
            val text = "${FunctionParameterListNode.startToken}$expression"
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect parameter list with no expression after positional spread operator`() {
        TestUtils.assertParserException {
            val text =
                    "${FunctionParameterListNode.startToken}${FunctionParameterListNode.positionalSpreadOperator}${FunctionParameterListNode.endToken}"
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect parameter list with no expression after named spread operator`() {
        TestUtils.assertParserException {
            val text =
                    "${FunctionParameterListNode.startToken}${FunctionParameterListNode.namedSpreadOperator}${FunctionParameterListNode.endToken}"
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionParameterListNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

