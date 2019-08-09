package org.lexem.angmar.parser.functional.expressions.macros

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import java.util.stream.*
import kotlin.streams.*

internal class MacroExpressionTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = MacroExpression.logicLineMacro

        @JvmStatic
        private fun provideCorrectMacros(): Stream<Arguments> {
            val result = sequence {
                for (macro in MacroExpression.macroNames) {
                    yield(macro)
                }
            }

            return result.map { Arguments.of(it) }.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is MacroExpression, "The node is not a MacroExpression")
            node as MacroExpression

            Assertions.assertEquals(testExpression, node.macroName, "The macroName property is incorrect")
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectMacros")
    fun `parse correct macro expression`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = MacroExpression.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MacroExpression

        Assertions.assertEquals(text, res.macroName, "The macroName property is incorrect")
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }


    @ParameterizedTest
    @ValueSource(strings = ["", "3", "no_macro!"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = MacroExpression.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

