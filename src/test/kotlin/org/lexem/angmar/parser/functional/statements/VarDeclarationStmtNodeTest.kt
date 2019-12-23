package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class VarDeclarationStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${VarDeclarationStmtNode.variableKeyword} ${CommonsTest.testDynamicIdentifier} ${VarDeclarationStmtNode.assignOperator} ${ExpressionsCommonsTest.testExpression}"

        @JvmStatic
        fun proviceCorrectVariableDeclaration(): Stream<Arguments> {
            val sequence = sequence {
                for (isConst in listOf(false, true)) {
                    val keyword = if (!isConst) {
                        VarDeclarationStmtNode.variableKeyword
                    } else {
                        VarDeclarationStmtNode.constKeyword
                    }

                    for (isDestructuring in listOf(false, true)) {
                        val name = if (isDestructuring) {
                            DestructuringStmtNodeTest.testExpression
                        } else {
                            CommonsTest.testDynamicIdentifier
                        }

                        var text =
                                "$keyword $name${VarDeclarationStmtNode.assignOperator}${ExpressionsCommonsTest.testExpression}"
                        yield(Arguments.of(text, isConst, isDestructuring))

                        // With whitespace
                        text =
                                "$keyword $name ${VarDeclarationStmtNode.assignOperator} ${ExpressionsCommonsTest.testExpression}"
                        yield(Arguments.of(text, isConst, isDestructuring))
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is VarDeclarationStmtNode, "The node is not a VarDeclarationStmtNode")
            node as VarDeclarationStmtNode

            Assertions.assertFalse(node.isConstant, "The isConst property is incorrect")
            CommonsTest.checkTestDynamicIdentifier(node.identifier)
            ExpressionsCommonsTest.checkTestExpression(node.value)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("proviceCorrectVariableDeclaration")
    fun `parse correct var declaration`(text: String, isConst: Boolean, isDestructuring: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = VarDeclarationStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as VarDeclarationStmtNode

        Assertions.assertEquals(isConst, res.isConstant, "The isConst property is incorrect")

        if (isDestructuring) {
            DestructuringStmtNodeTest.checkTestExpression(res.identifier)
        } else {
            CommonsTest.checkTestDynamicIdentifier(res.identifier)
        }

        ExpressionsCommonsTest.checkTestExpression(res.value)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect variable declaration without identifier`() {
        TestUtils.assertParserException(AngmarParserExceptionType.VarDeclarationStatementWithoutIdentifier) {
            val text = VarDeclarationStmtNode.variableKeyword
            val parser = LexemParser(IOStringReader.from(text))
            VarDeclarationStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect variable declaration without assign operator`() {
        TestUtils.assertParserException(AngmarParserExceptionType.VarDeclarationStatementWithoutAssignOperator) {
            val text = "${VarDeclarationStmtNode.variableKeyword} ${IdentifierNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            VarDeclarationStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect variable declaration without expression`() {
        TestUtils.assertParserException(
                AngmarParserExceptionType.VarDeclarationStatementWithoutExpressionAfterAssignOperator) {
            val text =
                    "${VarDeclarationStmtNode.variableKeyword} ${IdentifierNodeTest.testExpression} ${VarDeclarationStmtNode.assignOperator}"
            val parser = LexemParser(IOStringReader.from(text))
            VarDeclarationStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = VarDeclarationStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
