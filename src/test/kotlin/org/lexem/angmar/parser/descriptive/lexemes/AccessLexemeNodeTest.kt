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

internal class AccessLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = ExpressionsCommonsTest.testLeftExpression

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegative in listOf(false, true)) {
                    var text = ExpressionsCommonsTest.testLeftExpression

                    if (isNegative) {
                        text = AccessLexemeNode.notOperator + text
                    }

                    yield(Arguments.of(text, isNegative, false))

                    // Whit whitespaces
                    text = ExpressionsCommonsTest.testLeftExpression

                    if (isNegative) {
                        text = AccessLexemeNode.notOperator + text
                    }

                    yield(Arguments.of(text, isNegative, false))

                    if (!isNegative) {
                        // With access
                        text =
                                "${ExpressionsCommonsTest.testLeftExpression}${AccessLexemeNode.nextAccessToken}$testExpression"

                        yield(Arguments.of(text, isNegative, true))

                        // Whit whitespaces
                        text =
                                "${ExpressionsCommonsTest.testLeftExpression} ${AccessLexemeNode.nextAccessToken} $testExpression"

                        yield(Arguments.of(text, isNegative, true))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as AccessLexemeNode

            Assertions.assertFalse(node.isNegated, "The isNegated property is incorrect")
            Assertions.assertNull(node.nextAccess, "The nextAccess property must be null")

            AccessExpressionLexemeNodeTest.checkTestExpression(node.expression)
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct node`(text: String, isNegative: Boolean, hasNextAccess: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AccessLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AccessLexemeNode

        Assertions.assertEquals(isNegative, res.isNegated, "The isNegated property is incorrect")

        if (hasNextAccess) {
            Assertions.assertNotNull(res.nextAccess, "The nextAccess property cannot be null")
            checkTestExpression(res.nextAccess!!)
        } else {
            Assertions.assertNull(res.nextAccess, "The nextAccess property must be null")
        }

        AccessExpressionLexemeNodeTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect negated node with next access`() {
        TestUtils.assertParserException(AngmarParserExceptionType.NegatedAccessWithNextAccess) {
            val text =
                    "${AccessLexemeNode.notOperator}${ExpressionsCommonsTest.testLeftExpression}${AccessLexemeNode.nextAccessToken}$testExpression"
            val parser = LexemParser(IOStringReader.from(text))
            AccessLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", AccessLexemeNode.notOperator])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AccessLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

