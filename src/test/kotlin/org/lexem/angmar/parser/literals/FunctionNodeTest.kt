package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class FunctionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${FunctionNode.keyword}${BlockStmtNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectFunctionStmt(): Stream<Arguments> {
            val sequence = sequence {
                for (hasArguments in listOf(false, true)) {
                    for (isLambda in listOf(false, true)) {
                        var text = FunctionNode.keyword

                        if (hasArguments) {
                            text += " ${FunctionParameterListNodeTest.testExpression}"
                        }

                        if (isLambda) {
                            text += " ${LambdaStmtNodeTest.testExpression}"
                        } else {
                            text += " ${BlockStmtNodeTest.testExpression}"
                        }

                        yield(Arguments.of(text, hasArguments, isLambda))

                        // Without whitespaces.
                        text = FunctionNode.keyword

                        if (hasArguments) {
                            text += FunctionParameterListNodeTest.testExpression
                        }

                        if (isLambda) {
                            text += LambdaStmtNodeTest.testExpression
                        } else {
                            text += BlockStmtNodeTest.testExpression
                        }

                        yield(Arguments.of(text, hasArguments, isLambda))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is FunctionNode, "The node is not a FunctionNode")
            node as FunctionNode

            Assertions.assertNull(node.parameterList, "The parameterList property must be null")
            BlockStmtNodeTest.checkTestExpression(node.block)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectFunctionStmt")
    fun `parse correct function statement`(text: String, hasArguments: Boolean, isLambda: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = FunctionNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionNode

        if (hasArguments) {
            Assertions.assertNotNull(res.parameterList, "The argumentList property cannot be null")
            FunctionParameterListNodeTest.checkTestExpression(res.parameterList!!)
        } else {
            Assertions.assertNull(res.parameterList, "The argumentList property must be null")
        }

        if (isLambda) {
            LambdaStmtNodeTest.checkTestExpression(res.block)
        } else {
            BlockStmtNodeTest.checkTestExpression(res.block)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = [FunctionNode.keyword, "${FunctionNode.keyword}${FunctionParameterListNodeTest.testExpression}"])
    fun `parse incorrect function node without a block`(text: String) {
        TestUtils.assertParserException(AngmarParserExceptionType.FunctionWithoutBlock) {
            val parser = LexemParser(IOStringReader.from(text))
            FunctionNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = FunctionNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
