package org.lexem.angmar.parser.functional.expressions.macros

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

internal class MacroBacktrackTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${MacroBacktrack.macroName}${FunctionCallNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is MacroBacktrack, "The node is not a MacroBacktrack")
            node as MacroBacktrack

            Assertions.assertNotNull(node.value, "The value property cannot be null")
            FunctionCallNodeTest.checkTestExpression(node.value!!)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = [MacroBacktrack.macroName])
    fun `parse correct macro backtrack`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = MacroBacktrack.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MacroBacktrack

        Assertions.assertNull(res.value, "The value property must be null")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }


    @ParameterizedTest
    @ValueSource(strings = ["${MacroBacktrack.macroName}${FunctionCallNodeTest.testExpression}"])
    fun `parse correct full macro backtrack`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = MacroBacktrack.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MacroBacktrack

        Assertions.assertNotNull(res.value, "The value property cannot be null")
        FunctionCallNodeTest.checkTestExpression(res.value!!)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3", "no_macro!"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = MacroBacktrack.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}


