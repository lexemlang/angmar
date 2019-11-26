package org.lexem.angmar.parser.functional.expressions.modifiers

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

internal class FunctionCallNamedArgumentNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${CommonsTest.testDynamicIdentifier}${FunctionCallNamedArgumentNode.relationalToken}${ExpressionsCommonsTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is FunctionCallNamedArgumentNode,
                    "The node is not a FunctionCallMiddleArgumentNode")
            node as FunctionCallNamedArgumentNode

            CommonsTest.checkTestDynamicIdentifier(node.identifier)
            ExpressionsCommonsTest.checkTestExpression(node.expression)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(
            strings = ["${CommonsTest.testDynamicIdentifier}${FunctionCallNamedArgumentNode.relationalToken}${ExpressionsCommonsTest.testExpression}", "${CommonsTest.testDynamicIdentifier}  ${FunctionCallNamedArgumentNode.relationalToken}  ${ExpressionsCommonsTest.testExpression}"])
    fun `parse correct function call named argument`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = FunctionCallNamedArgumentNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as FunctionCallNamedArgumentNode

        CommonsTest.checkTestDynamicIdentifier(res.identifier)
        ExpressionsCommonsTest.checkTestExpression(res.expression)
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = ["${CommonsTest.testDynamicIdentifier}${FunctionCallNamedArgumentNode.relationalToken}"])
    fun `parse incorrect function call named argument without expression`(text: String) {
        TestUtils.assertParserException(
                AngmarParserExceptionType.FunctionCallMiddleArgumentWithoutExpressionAfterRelationalToken) {
            val parser = LexemParser(IOStringReader.from(text))
            FunctionCallNamedArgumentNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3", CommonsTest.testDynamicIdentifier])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = FunctionCallNamedArgumentNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

