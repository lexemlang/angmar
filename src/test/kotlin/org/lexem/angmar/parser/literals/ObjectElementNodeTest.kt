package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*

internal class ObjectElementNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val correctObjectElement =
                "${CommonsTest.testDynamicIdentifier}${ObjectElementNode.keyValueSeparator}${ExpressionsCommonsTest.testExpression}"
        const val correctConstantObjectElement =
                "${ObjectElementNode.constantToken}${CommonsTest.testDynamicIdentifier}${ObjectElementNode.keyValueSeparator}${ExpressionsCommonsTest.testExpression}"
        const val correctObjectElementWithWS =
                "${CommonsTest.testDynamicIdentifier}  ${ObjectElementNode.keyValueSeparator}  ${ExpressionsCommonsTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, isConstant: Boolean) {
            Assertions.assertTrue(node is ObjectElementNode, "The node is not a ObjectElementNode")
            node as ObjectElementNode

            Assertions.assertEquals(isConstant, node.isConstant, "The isConstant property is not correct")
            CommonsTest.checkTestDynamicIdentifier(node.key)
            ExpressionsCommonsTest.checkTestExpression(node.value)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [correctObjectElement])
    fun `parse correct object element`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ObjectElementNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectElementNode

        checkTestExpression(res, false)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [correctConstantObjectElement])
    fun `parse correct constant object element`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ObjectElementNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectElementNode

        checkTestExpression(res, true)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [correctObjectElementWithWS])
    fun `parse correct object element with whitespaces`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ObjectElementNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectElementNode

        checkTestExpression(res, false)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect object element with no keyValueSeparator`() {
        assertParserException {
            val text = CommonsTest.testDynamicIdentifier
            val parser = LexemParser(CustomStringReader.from(text))
            ObjectElementNode.parse(parser)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect object element with no value`() {
        assertParserException {
            val text = "${CommonsTest.testDynamicIdentifier}${ObjectElementNode.keyValueSeparator}"
            val parser = LexemParser(CustomStringReader.from(text))
            ObjectElementNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ObjectElementNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

