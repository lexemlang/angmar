package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*

internal class FunctionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${FunctionNode.keyword}${BlockStmtNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectLists() = ListNodeTest.provideCorrectLists()

        @JvmStatic
        private fun provideCorrectListsWithWS() = ListNodeTest.provideCorrectListsWithWS()

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is FunctionNode, "The node is not a FunctionNode")
            node as FunctionNode

            Assertions.assertNull(node.argumentList, "The argumentList property must be null")
            BlockStmtNodeTest.checkTestExpression(node.block)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(
            strings = ["${FunctionNode.keyword}${BlockStmtNodeTest.testExpression}", "${FunctionNode.keyword}  ${BlockStmtNodeTest.testExpression}", "${FunctionNode.keyword}\n${BlockStmtNodeTest.testExpression}"])
    fun `parse correct function node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionNode

        Assertions.assertNull(res.argumentList, "The argumentList property must be null")
        BlockStmtNodeTest.checkTestExpression(res.block)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["${FunctionNode.keyword}${FunctionArgumentListNodeTest.testExpression}${BlockStmtNodeTest.testExpression}", "${FunctionNode.keyword} ${FunctionArgumentListNodeTest.testExpression} ${BlockStmtNodeTest.testExpression}", "${FunctionNode.keyword}\n ${FunctionArgumentListNodeTest.testExpression} \n ${BlockStmtNodeTest.testExpression}"])
    fun `parse correct function node with arguments`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionNode

        Assertions.assertNotNull(res.argumentList, "The argumentList property cannot be null")
        FunctionArgumentListNodeTest.checkTestExpression(res.argumentList!!)
        BlockStmtNodeTest.checkTestExpression(res.block)
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = [FunctionNode.keyword, "${FunctionNode.keyword}${FunctionArgumentListNodeTest.testExpression}"])
    fun `parse incorrect function node without a block`(text: String) {
        assertParserException {
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}