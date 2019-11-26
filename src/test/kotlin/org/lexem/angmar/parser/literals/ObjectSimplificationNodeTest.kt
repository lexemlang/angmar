package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
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
                "${IdentifierNodeTest.testExpression}${FunctionParameterListNodeTest.testExpression}${BlockStmtNodeTest.testExpression}"
        const val correctConstantObjectSimplificationElementLikeFunction =
                "${ObjectSimplificationNode.constantToken}$correctObjectSimplificationElementLikeFunction"
        const val correctObjectSimplificationElementWithWSLikeFunction =
                "${IdentifierNodeTest.testExpression}  ${FunctionParameterListNodeTest.testExpression}  ${BlockStmtNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, isConstant: Boolean, isFunction: Boolean) {
            Assertions.assertTrue(node is ObjectSimplificationNode, "The node is not a ObjectSimplificationNode")
            node as ObjectSimplificationNode

            if (!isFunction) {
                Assertions.assertEquals(isConstant, node.isConstant, "The isConstant property is incorrect")
                IdentifierNodeTest.checkTestExpression(node.identifier)
                Assertions.assertNull(node.parameterList, "The arguments property must be null")
                Assertions.assertNull(node.block, "The body property must be null")
            } else {
                Assertions.assertEquals(isConstant, node.isConstant, "The isConstant property is incorrect")
                IdentifierNodeTest.checkTestExpression(node.identifier)
                Assertions.assertNotNull(node.parameterList, "The arguments property cannot be null")
                Assertions.assertNotNull(node.block, "The body property cannot be null")
                FunctionParameterListNodeTest.checkTestExpression(node.parameterList!!)
                BlockStmtNodeTest.checkTestExpression(node.block!!)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["${IdentifierNodeTest.testExpression}  ${ObjectNode.elementSeparator}", "${IdentifierNodeTest.testExpression}${ObjectNode.endToken}"])
    fun `parse correct object simplification`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ObjectSimplificationNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectSimplificationNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        IdentifierNodeTest.checkTestExpression(res.identifier)
        Assertions.assertNull(res.parameterList, "The arguments property must be null")
        Assertions.assertNull(res.block, "The body property must be null")

        val finalString =
                text.substring(0, text.indexOfAny(listOf(ObjectNode.endToken, ObjectNode.elementSeparator))).trim()
        Assertions.assertEquals(finalString.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["${ObjectSimplificationNode.constantToken}${IdentifierNodeTest.testExpression}${ObjectNode.elementSeparator}", "${ObjectSimplificationNode.constantToken}${IdentifierNodeTest.testExpression}  ${ObjectNode.endToken}"])
    fun `parse correct constant object simplification`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ObjectSimplificationNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectSimplificationNode

        Assertions.assertTrue(res.isConstant, "The isConstant property is incorrect")
        IdentifierNodeTest.checkTestExpression(res.identifier)
        Assertions.assertNull(res.parameterList, "The arguments property must be null")
        Assertions.assertNull(res.block, "The body property must be null")

        val finalString =
                text.substring(0, text.indexOfAny(listOf(ObjectNode.endToken, ObjectNode.elementSeparator))).trim()
        Assertions.assertEquals(finalString.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["${IdentifierNodeTest.testExpression}${FunctionParameterListNodeTest.testExpression}${BlockStmtNodeTest.testExpression}", "${IdentifierNodeTest.testExpression}  ${FunctionParameterListNodeTest.testExpression}  ${BlockStmtNodeTest.testExpression}"])
    fun `parse correct object simplification like function`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ObjectSimplificationNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectSimplificationNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        IdentifierNodeTest.checkTestExpression(res.identifier)
        Assertions.assertNotNull(res.parameterList, "The arguments property cannot be null")
        Assertions.assertNotNull(res.block, "The body property cannot be null")
        FunctionParameterListNodeTest.checkTestExpression(res.parameterList!!)
        BlockStmtNodeTest.checkTestExpression(res.block!!)
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["${IdentifierNodeTest.testExpression}${FunctionParameterListNodeTest.testExpression}"])
    fun `parse incorrect object simplification without a block`(text: String) {
        TestUtils.assertParserException(AngmarParserExceptionType.ObjectSimplificationWithoutBlock) {
            val parser = LexemParser(IOStringReader.from(text))
            ObjectSimplificationNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["", ObjectSimplificationNode.constantToken, IdentifierNodeTest.testExpression, "${ObjectSimplificationNode.constantToken}${IdentifierNodeTest.testExpression} a"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ObjectSimplificationNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
