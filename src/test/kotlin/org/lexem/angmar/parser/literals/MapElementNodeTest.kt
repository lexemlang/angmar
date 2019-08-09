package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*

internal class MapElementNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val correctMapElement =
                "${ExpressionsCommonsTest.testExpression}${MapElementNode.keyValueSeparator}${ExpressionsCommonsTest.testExpression}"
        const val correctMapElementWithWS =
                "${ExpressionsCommonsTest.testExpression}  ${MapElementNode.keyValueSeparator}  ${ExpressionsCommonsTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is MapElementNode, "The node is not a MapElementNode")
            node as MapElementNode

            ExpressionsCommonsTest.checkTestExpression(node.key)
            ExpressionsCommonsTest.checkTestExpression(node.value)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [correctMapElement])
    fun `parse correct map element`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = MapElementNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MapElementNode

        checkTestExpression(res)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [correctMapElementWithWS])
    fun `parse correct map element with whitespaces`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = MapElementNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MapElementNode

        checkTestExpression(res)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect map element with no keyValueSeparator`() {
        assertParserException {
            val text = ExpressionsCommonsTest.testExpression
            val parser = LexemParser(CustomStringReader.from(text))
            MapElementNode.parse(parser)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect map element with no value`() {
        assertParserException {
            val text = "${ExpressionsCommonsTest.testExpression}${MapElementNode.keyValueSeparator}"
            val parser = LexemParser(CustomStringReader.from(text))
            MapElementNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = MapElementNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
