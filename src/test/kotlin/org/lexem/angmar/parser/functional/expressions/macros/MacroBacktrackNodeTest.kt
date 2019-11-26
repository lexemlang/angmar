package org.lexem.angmar.parser.functional.expressions.macros

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import java.util.stream.*
import kotlin.streams.*

internal class MacroBacktrackNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = MacroBacktrackNode.macroName

        @JvmStatic
        private fun provideMacros(): Stream<Arguments> {
            val sequence = sequence {
                for (hasArguments in listOf(false, true)) {
                    var text = MacroBacktrackNode.macroName

                    if (hasArguments) {
                        text += FunctionCallNodeTest.testExpression
                    }

                    yield(Arguments.of(text, hasArguments))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as MacroBacktrackNode

            Assertions.assertNull(node.arguments, "The arguments property must be null")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideMacros")
    fun `parse correct macro`(text: String, hasArguments: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = MacroBacktrackNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MacroBacktrackNode

        if (hasArguments) {
            Assertions.assertNotNull(res.arguments, "The arguments property cannot be null")
            FunctionCallNodeTest.checkTestExpression(res.arguments!!)
        } else {
            Assertions.assertNull(res.arguments, "The arguments property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = MacroBacktrackNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

