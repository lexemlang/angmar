package org.lexem.angmar.parser.functional.expressions

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.literals.*
import java.util.stream.*
import kotlin.streams.*

internal class ExpressionsCommonsTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = NumberNodeTest.testExpression
        const val testMacro = MacroExpressionTest.testExpression
        const val testLiteral = LiteralCommonsTest.testAnyObject
        const val testLeftExpression = AccessExpressionNodeTest.testExpression
        const val testRightExpression = ConditionalExpressionNodeTest.testExpression

        @JvmStatic
        private fun provideExpressions(): Stream<Arguments> {
            val sequence = sequence {
                val tests = listOf(AssignExpressionNodeTest.testExpression, testRightExpression)

                for (test in tests.withIndex()) {
                    yield(Arguments.of(test.value, test.index))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideMacros(): Stream<Arguments> {
            val sequence = sequence {
                val tests = listOf(MacroCheckPropsTest.testExpression, MacroBacktrackTest.testExpression,
                        MacroExpressionTest.testExpression)

                for (test in tests.withIndex()) {
                    yield(Arguments.of(test.value, test.index))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideLiterals(): Stream<Arguments> {
            val sequence = sequence {
                val tests =
                        listOf(NilNodeTest.testExpression, LogicNodeTest.testExpression, NumberNodeTest.testExpression,
                                LiteralCommonsTest.testAnyString, LiteralCommonsTest.testAnyInterval,
                                BitlistNodeTest.testExpression, ListNodeTest.testExpression, SetNodeTest.testExpression,
                                LiteralCommonsTest.testAnyObject, MapNodeTest.testExpression,
                                FunctionNodeTest.testExpression)

                for (test in tests.withIndex()) {
                    yield(Arguments.of(test.value, test.index))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) = NumberNodeTest.checkTestExpression(node)
        fun checkTestMacro(node: ParserNode) = MacroExpressionTest.checkTestExpression(node)
        fun checkTestLiteral(node: ParserNode) = LiteralCommonsTest.checkTestAnyObject(node)
        fun checkTestLeftExpression(node: ParserNode) = AccessExpressionNodeTest.checkTestExpression(node)
        fun checkTestRightExpression(node: ParserNode) = ConditionalExpressionNodeTest.checkTestExpression(node)
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideExpressions")
    fun `parse correct expression`(text: String, type: Int) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ExpressionsCommons.parseExpression(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> AssignExpressionNodeTest.checkTestExpression(res)
            1 -> checkTestRightExpression(res)
            else -> throw AngmarUnimplementedException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideMacros")
    fun `parse correct macro`(text: String, type: Int) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ExpressionsCommons.parseMacro(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> MacroCheckPropsTest.checkTestExpression(res)
            1 -> MacroBacktrackTest.checkTestExpression(res)
            2 -> MacroExpressionTest.checkTestExpression(res)
            else -> throw AngmarUnimplementedException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideLiterals")
    fun `parse correct literal`(text: String, type: Int) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ExpressionsCommons.parseLiteral(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> NilNodeTest.checkTestExpression(res)
            1 -> LogicNodeTest.checkTestExpression(res)
            2 -> NumberNodeTest.checkTestExpression(res)
            3 -> LiteralCommonsTest.checkTestAnyString(res)
            4 -> LiteralCommonsTest.checkTestAnyInterval(res)
            5 -> BitlistNodeTest.checkTestExpression(res)
            6 -> ListNodeTest.checkTestExpression(res)
            7 -> SetNodeTest.checkTestExpression(res)
            8 -> LiteralCommonsTest.checkTestAnyObject(res)
            9 -> MapNodeTest.checkTestExpression(res)
            10 -> FunctionNodeTest.checkTestExpression(res)
            else -> throw AngmarUnimplementedException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct left expression`() {
        val text = AccessExpressionNodeTest.testExpression
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ExpressionsCommons.parseLeftExpression(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        AccessExpressionNodeTest.checkTestExpression(res!!)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct right expression`() {
        val text = ConditionalExpressionNodeTest.testExpression
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ExpressionsCommons.parseRightExpression(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        ConditionalExpressionNodeTest.checkTestExpression(res!!)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }
}
