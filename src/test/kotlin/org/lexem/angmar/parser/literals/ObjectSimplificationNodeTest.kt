package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*

internal class ObjectSimplificationNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val correctObjectSimplificationElement = IdentifierNodeTest.testExpression
        const val correctConstantObjectSimplificationElement =
                "${ObjectSimplificationNode.constantToken}$correctObjectSimplificationElement"
        const val correctObjectSimplificationElementWithWS = correctObjectSimplificationElement
        const val correctObjectSimplificationElementLikeFunction =
                "${IdentifierNodeTest.testExpression}${FunctionArgumentListNodeTest.testExpression}${BlockStmtNodeTest.testExpression}"
        const val correctConstantObjectSimplificationElementLikeFunction =
                "${ObjectSimplificationNode.constantToken}$correctObjectSimplificationElementLikeFunction"
        const val correctObjectSimplificationElementWithWSLikeFunction =
                "${IdentifierNodeTest.testExpression}  ${FunctionArgumentListNodeTest.testExpression}  ${BlockStmtNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, isConstant: Boolean, isFunction: Boolean) {
            Assertions.assertTrue(node is ObjectSimplificationNode, "The node is not a ObjectSimplificationNode")
            node as ObjectSimplificationNode

            if (!isFunction) {
                Assertions.assertEquals(isConstant, node.isConstant, "The isConstant property is incorrect")
                IdentifierNodeTest.checkTestExpression(node.identifier)
                Assertions.assertNull(node.arguments, "The arguments property must be null")
                Assertions.assertNull(node.body, "The body property must be null")
            } else {
                Assertions.assertEquals(isConstant, node.isConstant, "The isConstant property is incorrect")
                IdentifierNodeTest.checkTestExpression(node.identifier)
                Assertions.assertNotNull(node.arguments, "The arguments property cannot be null")
                Assertions.assertNotNull(node.body, "The body property cannot be null")
                FunctionArgumentListNodeTest.checkTestExpression(node.arguments!!)
                BlockStmtNodeTest.checkTestExpression(node.body!!)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["${IdentifierNodeTest.testExpression}  ${ObjectNode.elementSeparator}", "${IdentifierNodeTest.testExpression}${ObjectNode.endToken}"])
    fun `parse correct object simplification`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ObjectSimplificationNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectSimplificationNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        IdentifierNodeTest.checkTestExpression(res.identifier)
        Assertions.assertNull(res.arguments, "The arguments property must be null")
        Assertions.assertNull(res.body, "The body property must be null")

        val finalString =
                text.substring(0, text.indexOfAny(listOf(ObjectNode.endToken, ObjectNode.elementSeparator))).trim()
        Assertions.assertEquals(finalString.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["${ObjectSimplificationNode.constantToken}${IdentifierNodeTest.testExpression}${ObjectNode.elementSeparator}", "${ObjectSimplificationNode.constantToken}${IdentifierNodeTest.testExpression}  ${ObjectNode.endToken}"])
    fun `parse correct constant object simplification`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ObjectSimplificationNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectSimplificationNode

        Assertions.assertTrue(res.isConstant, "The isConstant property is incorrect")
        IdentifierNodeTest.checkTestExpression(res.identifier)
        Assertions.assertNull(res.arguments, "The arguments property must be null")
        Assertions.assertNull(res.body, "The body property must be null")

        val finalString =
                text.substring(0, text.indexOfAny(listOf(ObjectNode.endToken, ObjectNode.elementSeparator))).trim()
        Assertions.assertEquals(finalString.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["${IdentifierNodeTest.testExpression}${FunctionArgumentListNodeTest.testExpression}${BlockStmtNodeTest.testExpression}", "${IdentifierNodeTest.testExpression}  ${FunctionArgumentListNodeTest.testExpression}  ${BlockStmtNodeTest.testExpression}"])
    fun `parse correct object simplification like function`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ObjectSimplificationNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectSimplificationNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        IdentifierNodeTest.checkTestExpression(res.identifier)
        Assertions.assertNotNull(res.arguments, "The arguments property cannot be null")
        Assertions.assertNotNull(res.body, "The body property cannot be null")
        FunctionArgumentListNodeTest.checkTestExpression(res.arguments!!)
        BlockStmtNodeTest.checkTestExpression(res.body!!)
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["${IdentifierNodeTest.testExpression}${FunctionArgumentListNodeTest.testExpression}"])
    fun `parse incorrect object simplification without a block`(text: String) {
        assertParserException {
            val parser = LexemParser(CustomStringReader.from(text))
            ObjectSimplificationNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["", ObjectSimplificationNode.constantToken, IdentifierNodeTest.testExpression, "${ObjectSimplificationNode.constantToken}${IdentifierNodeTest.testExpression} a"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ObjectSimplificationNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
