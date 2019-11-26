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

internal class MacroCheckPropsNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${MacroCheckPropsNode.macroName}${PropertyStyleObjectBlockNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is MacroCheckPropsNode, "The node is not a MacroCheckPropsNode")
            node as MacroCheckPropsNode

            PropertyStyleObjectBlockNodeTest.checkTestExpression(node.value)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["${MacroCheckPropsNode.macroName}${PropertyStyleObjectBlockNodeTest.testExpression}"])
    fun `parse correct macro check props`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = MacroCheckPropsNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MacroCheckPropsNode

        PropertyStyleObjectBlockNodeTest.checkTestExpression(res.value)
        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [MacroCheckPropsNode.macroName])
    fun `parse incorrect macro check props without prop-style object`(text: String) {
        TestUtils.assertParserException(
                AngmarParserExceptionType.MacroCheckPropsWithoutPropertyStyleBlockAfterMacroName) {
            val parser = LexemParser(IOStringReader.from(text))
            MacroCheckPropsNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3", "no_macro!"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = MacroCheckPropsNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

