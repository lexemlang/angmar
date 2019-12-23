package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class DestructuringElementStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${DestructuringElementStmtNode.constantToken}${IdentifierNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectDestructuringElementStmt(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of(IdentifierNodeTest.testExpression, false, false))
                yield(Arguments.of("${DestructuringElementStmtNode.constantToken}${IdentifierNodeTest.testExpression}",
                        false, true))
                yield(Arguments.of(
                        "${IdentifierNodeTest.testExpression} ${DestructuringElementStmtNode.aliasToken} ${IdentifierNodeTest.testExpression}",
                        true, false))
                yield(Arguments.of(
                        "${IdentifierNodeTest.testExpression} ${DestructuringElementStmtNode.aliasToken} ${DestructuringElementStmtNode.constantToken}${IdentifierNodeTest.testExpression}",
                        true, true))
                yield(Arguments.of(
                        "${IdentifierNodeTest.testExpression} ${DestructuringElementStmtNode.aliasToken}${DestructuringElementStmtNode.constantToken}${IdentifierNodeTest.testExpression}",
                        true, true))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is DestructuringElementStmtNode,
                    "The node is not a DestructuringElementStmtNode")
            node as DestructuringElementStmtNode

            Assertions.assertTrue(node.isConstant, "The isConstant property is incorrect")
            Assertions.assertNull(node.original, "The original property must be null")
            Assertions.assertNotNull(node.alias, "The alias property cannot be null")
            IdentifierNodeTest.checkTestExpression(node.alias)

        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectDestructuringElementStmt")
    fun `parse correct destructuring element statement`(text: String, hasOriginal: Boolean, isConstant: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = DestructuringElementStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as DestructuringElementStmtNode

        Assertions.assertEquals(isConstant, res.isConstant, "The isConstant property is incorrect")

        if (hasOriginal) {
            Assertions.assertNotNull(res.original, "The original property cannot be null")
            IdentifierNodeTest.checkTestExpression(res.original!!)
        } else {
            Assertions.assertNull(res.original, "The original property must be null")
        }

        Assertions.assertNotNull(res.alias, "The alias property cannot be null")
        IdentifierNodeTest.checkTestExpression(res.alias)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect destructuring element statement with original but without alias`() {
        TestUtils.assertParserException(
                AngmarParserExceptionType.DestructuringElementStatementWithoutIdentifierAfterAliasToken) {
            val text = "${IdentifierNodeTest.testExpression} ${DestructuringElementStmtNode.aliasToken}"
            val parser = LexemParser(IOStringReader.from(text))
            DestructuringElementStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = DestructuringElementStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
