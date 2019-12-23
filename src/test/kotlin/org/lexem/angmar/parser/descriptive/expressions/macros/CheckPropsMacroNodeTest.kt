package org.lexem.angmar.parser.descriptive.expressions.macros

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class CheckPropsMacroNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${CheckPropsMacroNode.macroName}${PropertyStyleObjectBlockNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is CheckPropsMacroNode, "The node is not a MacroCheckPropsNode")
            node as CheckPropsMacroNode

            PropertyStyleObjectBlockNodeTest.checkTestExpression(node.properties)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["${CheckPropsMacroNode.macroName}${PropertyStyleObjectBlockNodeTest.testExpression}"])
    fun `parse correct macro check props`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = CheckPropsMacroNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as CheckPropsMacroNode

        PropertyStyleObjectBlockNodeTest.checkTestExpression(res.properties)
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [CheckPropsMacroNode.macroName])
    fun `parse incorrect macro check props without prop-style object`(text: String) {
        TestUtils.assertParserException(
                AngmarParserExceptionType.MacroCheckPropsWithoutPropertyStyleBlockAfterMacroName) {
            val parser = LexemParser(IOStringReader.from(text))
            CheckPropsMacroNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3", "no_macro!"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = CheckPropsMacroNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

