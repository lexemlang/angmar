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

internal class PropertyStyleObjectElementNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${CommonsTest.testDynamicIdentifier}${ParenthesisExpressionNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is PropertyStyleObjectElementNode,
                    "The node is not a PropertyStyleObjectElementNode")
            node as PropertyStyleObjectElementNode

            CommonsTest.checkTestDynamicIdentifier(node.key)
            ParenthesisExpressionNodeTest.checkTestExpression(node.value)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [testExpression])
    fun `parse correct property-style object element `(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PropertyStyleObjectElementNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyStyleObjectElementNode

        checkTestExpression(res)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect property-style object element with no value`() {
        TestUtils.assertParserException {
            val text = CommonsTest.testDynamicIdentifier
            val parser = LexemParser(CustomStringReader.from(text))
            PropertyStyleObjectElementNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PropertyStyleObjectElementNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
