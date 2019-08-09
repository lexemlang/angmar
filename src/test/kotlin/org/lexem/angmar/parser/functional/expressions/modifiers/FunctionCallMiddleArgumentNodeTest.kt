package org.lexem.angmar.parser.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*

internal class FunctionCallMiddleArgumentNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${CommonsTest.testDynamicIdentifier}${FunctionCallMiddleArgumentNode.relationalToken}${ExpressionsCommonsTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is FunctionCallMiddleArgumentNode,
                    "The node is not a FunctionCallMiddleArgumentNode")
            node as FunctionCallMiddleArgumentNode

            CommonsTest.checkTestDynamicIdentifier(node.identifier)
            ExpressionsCommonsTest.checkTestExpression(node.expression)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(
            strings = ["${CommonsTest.testDynamicIdentifier}${FunctionCallMiddleArgumentNode.relationalToken}${ExpressionsCommonsTest.testExpression}", "${CommonsTest.testDynamicIdentifier}  ${FunctionCallMiddleArgumentNode.relationalToken}  ${ExpressionsCommonsTest.testExpression}"])
    fun `parse correct function call middle argument`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionCallMiddleArgumentNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionCallMiddleArgumentNode

        CommonsTest.checkTestDynamicIdentifier(res.identifier)
        ExpressionsCommonsTest.checkTestExpression(res.expression)
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["${CommonsTest.testDynamicIdentifier}${FunctionCallMiddleArgumentNode.relationalToken}"])
    fun `parse incorrect macro check props without prop-style object`(text: String) {
        assertParserException {
            val parser = LexemParser(CustomStringReader.from(text))
            FunctionCallMiddleArgumentNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3", CommonsTest.testDynamicIdentifier])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = FunctionCallMiddleArgumentNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

