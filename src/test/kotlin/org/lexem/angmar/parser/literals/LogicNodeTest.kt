package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*

internal class LogicNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = LogicNode.trueLiteral

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is LogicNode, "The node is not a LogicNode")
            node as LogicNode

            Assertions.assertTrue(node.value, "The value of the node must be true")
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [LogicNode.trueLiteral, "${LogicNode.trueLiteral}-"])
    fun `parse correct true keyword`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = LogicNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LogicNode

        Assertions.assertTrue(res.value, "The value of the node must be true")
        Assertions.assertEquals(LogicNode.trueLiteral, res.toString(), "The content of the node is incorrect")
        Assertions.assertEquals(LogicNode.trueLiteral.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [LogicNode.falseLiteral, "${LogicNode.falseLiteral}-"])
    fun `parse correct false keyword`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = LogicNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LogicNode

        Assertions.assertFalse(res.value, "The value of the node must be false")
        Assertions.assertEquals(LogicNode.falseLiteral, res.toString(), "The content of the node is incorrect")
        Assertions.assertEquals(LogicNode.falseLiteral.length, parser.reader.currentPosition(),
                "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["${LogicNode.trueLiteral}able", "${LogicNode.trueLiteral}-able", "${LogicNode.falseLiteral}able", "${LogicNode.falseLiteral}-able"])
    fun `parse incorrect true and false keywords`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = LogicNode.parse(parser)

        Assertions.assertNull(res, "The input has not been correctly parsed")
        Assertions.assertEquals(0, parser.reader.currentPosition(),
                "The input has moved the cursor but has not capture anything")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = LogicNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
