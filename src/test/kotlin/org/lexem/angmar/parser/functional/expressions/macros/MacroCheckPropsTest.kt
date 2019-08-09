package org.lexem.angmar.parser.functional.expressions.macros

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class MacroCheckPropsTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${MacroCheckProps.macroName}${PropertyStyleObjectBlockNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is MacroCheckProps, "The node is not a MacroCheckProps")
            node as MacroCheckProps

            PropertyStyleObjectBlockNodeTest.checkTestExpression(node.value)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["${MacroCheckProps.macroName}${PropertyStyleObjectBlockNodeTest.testExpression}"])
    fun `parse correct macro check props`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = MacroCheckProps.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MacroCheckProps

        PropertyStyleObjectBlockNodeTest.checkTestExpression(res.value)
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [MacroCheckProps.macroName])
    fun `parse incorrect macro check props without prop-style object`(text: String) {
        assertParserException {
            val parser = LexemParser(CustomStringReader.from(text))
            MacroCheckProps.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3", "no_macro!"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = MacroCheckProps.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

