package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class SetPropsMacroStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${SetPropsMacroStmtNode.macroName}${PropertyStyleObjectBlockNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is SetPropsMacroStmtNode, "The node is not a SetPropsMacroStmtNode")
            node as SetPropsMacroStmtNode

            PropertyStyleObjectBlockNodeTest.checkTestExpression(node.properties)
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["${SetPropsMacroStmtNode.macroName}${PropertyStyleObjectBlockNodeTest.testExpression}"])
    fun `parse correct conditional statement`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = SetPropsMacroStmtNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as SetPropsMacroStmtNode

        PropertyStyleObjectBlockNodeTest.checkTestExpression(res.properties)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect set props macro without property style object`() {
        assertParserException {
            val text = SetPropsMacroStmtNode.macroName
            val parser = LexemParser(CustomStringReader.from(text))
            SetPropsMacroStmtNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = SetPropsMacroStmtNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

