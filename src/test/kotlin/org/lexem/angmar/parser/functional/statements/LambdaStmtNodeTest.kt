package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LambdaStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${LambdaStmtNode.token}${ExpressionsCommonsTest.testExpression}"
        const val testLexemExpression = "${LambdaStmtNode.token}${LexemePatternContentNodeTest.testExpression}"
        const val testFilterLexemExpression =
                "${LambdaStmtNode.token}${LexemePatternContentNodeTest.testExpressionFilter}"

        @JvmStatic
        private fun provideCorrectFunctionalLambdas(): Stream<Arguments> {
            val result = sequence {
                yield(Arguments.of("${LambdaStmtNode.token}${ExpressionsCommonsTest.testExpression}"))
                yield(Arguments.of("${LambdaStmtNode.token} ${ExpressionsCommonsTest.testExpression}"))
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideCorrectDescriptiveExpressionsLambdas(): Stream<Arguments> {
            val result = sequence {
                yield(Arguments.of("${LambdaStmtNode.token}${LexemePatternContentNodeTest.testExpression}"))
                yield(Arguments.of("${LambdaStmtNode.token} ${LexemePatternContentNodeTest.testExpression}"))
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideCorrectDescriptiveFiltersLambdas(): Stream<Arguments> {
            val result = sequence {
                yield(Arguments.of("${LambdaStmtNode.token}${LexemePatternContentNodeTest.testExpressionFilter}"))
                yield(Arguments.of("${LambdaStmtNode.token} ${LexemePatternContentNodeTest.testExpressionFilter}"))
            }

            return result.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is LambdaStmtNode, "The node is not a LambdaStmtNode")
            node as LambdaStmtNode

            ExpressionsCommonsTest.checkTestExpression(node.expression)
            Assertions.assertFalse(node.isFilterCode, "The isFilterCode property is incorrect")
        }

        fun checkTestLexemExpression(node: ParserNode) {
            Assertions.assertTrue(node is LambdaStmtNode, "The node is not a LambdaStmtNode")
            node as LambdaStmtNode

            LexemePatternContentNodeTest.checkTestExpression(node.expression)
            Assertions.assertFalse(node.isFilterCode, "The isFilterCode property is incorrect")
        }

        fun checkTestFilterLexemExpression(node: ParserNode) {
            Assertions.assertTrue(node is LambdaStmtNode, "The node is not a LambdaStmtNode")
            node as LambdaStmtNode

            LexemePatternContentNodeTest.checkTestExpressionFilter(node.expression)
            Assertions.assertTrue(node.isFilterCode, "The isFilterCode property is incorrect")
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectFunctionalLambdas")
    fun `parse correct lambda - functional`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = LambdaStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LambdaStmtNode

        ExpressionsCommonsTest.checkTestExpression(res.expression)
        Assertions.assertFalse(res.isFilterCode, "The isFilterCode property is incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectDescriptiveExpressionsLambdas")
    fun `parse correct lambda - descriptive expressions`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = LambdaStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LambdaStmtNode

        LexemePatternContentNodeTest.checkTestExpression(res.expression)
        Assertions.assertFalse(res.isFilterCode, "The isFilterCode property is incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectDescriptiveFiltersLambdas")
    fun `parse correct lambda - descriptive filter`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        parser.isFilterCode = true
        val res = LambdaStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LambdaStmtNode

        LexemePatternContentNodeTest.checkTestExpressionFilter(res.expression)
        Assertions.assertTrue(res.isFilterCode, "The isFilterCode property is incorrect")
    }

    @Test
    @Incorrect
    fun `parse incorrect lambda without expression - functional`() {
        TestUtils.assertParserException(AngmarParserExceptionType.LambdaStatementWithoutExpression) {
            val text = LambdaStmtNode.token
            val parser = LexemParser(IOStringReader.from(text))
            LambdaStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect block without expression - descriptive`() {
        TestUtils.assertParserException(AngmarParserExceptionType.LambdaStatementWithoutExpression) {
            val text = LambdaStmtNode.token
            val parser = LexemParser(IOStringReader.from(text))
            parser.isDescriptiveCode = true
            LambdaStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = LambdaStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
