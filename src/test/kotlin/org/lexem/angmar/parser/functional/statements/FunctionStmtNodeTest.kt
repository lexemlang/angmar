package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class FunctionStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${FunctionStmtNode.keyword} ${IdentifierNodeTest.testExpression} ${FunctionParameterListNodeTest.testExpression} ${BlockStmtNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectFunctionStmt(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of(
                        "${FunctionStmtNode.keyword} ${IdentifierNodeTest.testExpression} ${FunctionParameterListNodeTest.testExpression} ${BlockStmtNodeTest.testExpression}",
                        true))
                yield(Arguments.of(
                        "${FunctionStmtNode.keyword} ${IdentifierNodeTest.testExpression}${FunctionParameterListNodeTest.testExpression}${BlockStmtNodeTest.testExpression}",
                        true))
                yield(Arguments.of(
                        "${FunctionStmtNode.keyword} ${IdentifierNodeTest.testExpression} ${BlockStmtNodeTest.testExpression}",
                        false))
                yield(Arguments.of(
                        "${FunctionStmtNode.keyword} ${IdentifierNodeTest.testExpression}${BlockStmtNodeTest.testExpression}",
                        false))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is FunctionStmtNode, "The node is not a FunctionStmtNode")
            node as FunctionStmtNode

            IdentifierNodeTest.checkTestExpression(node.name)
            BlockStmtNodeTest.checkTestExpression(node.block)

            Assertions.assertNotNull(node.parameterList, "The argumentList property cannot be null")
            FunctionParameterListNodeTest.checkTestExpression(node.parameterList!!)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectFunctionStmt")
    fun `parse correct function statement`(text: String, hasArguments: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = FunctionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionStmtNode

        IdentifierNodeTest.checkTestExpression(res.name)
        BlockStmtNodeTest.checkTestExpression(res.block)

        if (hasArguments) {
            Assertions.assertNotNull(res.parameterList, "The argumentList property cannot be null")
            FunctionParameterListNodeTest.checkTestExpression(res.parameterList!!)
        } else {
            Assertions.assertNull(res.parameterList, "The argumentList property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect function statement without block`() {
        TestUtils.assertParserException(AngmarParserExceptionType.FunctionStatementWithoutBlock) {
            val text =
                    "${FunctionStmtNode.keyword} ${IdentifierNodeTest.testExpression} ${FunctionParameterListNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            FunctionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = FunctionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
