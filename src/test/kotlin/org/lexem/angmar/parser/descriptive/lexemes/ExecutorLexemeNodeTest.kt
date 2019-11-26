package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class ExecutorLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ExecutorLexemeNode.startToken}${ExpressionsCommonsTest.testExpression}${ExecutorLexemeNode.endToken}"

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (isConditional in listOf(false, true)) {
                    for (expressionCount in 1..5) {
                        val list = List(expressionCount) { ExpressionsCommonsTest.testExpression }

                        if (isConditional) {
                            var text =
                                    "${ExecutorLexemeNode.startToken}${ExecutorLexemeNode.conditionalToken}${list.joinToString(
                                            ExecutorLexemeNode.separatorToken)}${ExecutorLexemeNode.endToken}"

                            yield(Arguments.of(text, isConditional, expressionCount))

                            text =
                                    "${ExecutorLexemeNode.startToken}${ExecutorLexemeNode.conditionalToken} ${list.joinToString(
                                            " ${ExecutorLexemeNode.separatorToken} ")} ${ExecutorLexemeNode.endToken}"

                            yield(Arguments.of(text, isConditional, expressionCount))
                        } else {
                            var text = "${ExecutorLexemeNode.startToken}${list.joinToString(
                                    ExecutorLexemeNode.separatorToken)}${ExecutorLexemeNode.endToken}"

                            yield(Arguments.of(text, isConditional, expressionCount))

                            text = "${ExecutorLexemeNode.startToken} ${list.joinToString(
                                    " ${ExecutorLexemeNode.separatorToken} ")} ${ExecutorLexemeNode.endToken}"

                            yield(Arguments.of(text, isConditional, expressionCount))

                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as ExecutorLexemeNode

            Assertions.assertFalse(node.isConditional, "The isConditional property is incorrect")
            Assertions.assertEquals(1, node.expressions.size, "The expression count is incorrect")

            node.expressions.forEach {
                ExpressionsCommonsTest.checkTestExpression(it)
            }
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct executor`(text: String, isConditional: Boolean, expressionCount: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ExecutorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ExecutorLexemeNode

        Assertions.assertEquals(isConditional, res.isConditional, "The isConditional property is incorrect")
        Assertions.assertEquals(expressionCount, res.expressions.size, "The expression count is incorrect")

        res.expressions.forEach {
            ExpressionsCommonsTest.checkTestExpression(it)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect executor without any expression`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ExecutorWithoutExpression) {
            val text = "${ExecutorLexemeNode.startToken}${ExecutorLexemeNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            ExecutorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect executor without a expression after the separator`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ExecutorWithoutExpressionAfterSeparator) {
            val text =
                    "${ExecutorLexemeNode.startToken}${ExpressionsCommonsTest.testExpression}${ExecutorLexemeNode.separatorToken}${ExecutorLexemeNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            ExecutorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ExecutorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

