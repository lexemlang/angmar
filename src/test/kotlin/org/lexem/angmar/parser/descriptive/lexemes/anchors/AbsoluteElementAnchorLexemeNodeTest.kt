package org.lexem.angmar.parser.descriptive.lexemes.anchors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class AbsoluteElementAnchorLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = AbsoluteElementAnchorLexemeNode.fromStartIdentifier + IndexerNodeTest.testExpression


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as AbsoluteElementAnchorLexemeNode

            Assertions.assertEquals(AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart, node.type,
                    "The type property is incorrect")
            ExpressionsCommonsTest.checkTestExpression(node.expression)
        }
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `parse correct from start element`() {
        val text = AbsoluteElementAnchorLexemeNode.fromStartIdentifier + IndexerNodeTest.testExpression
        val parser = LexemParser(IOStringReader.from(text))
        val res = AbsoluteElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AbsoluteElementAnchorLexemeNode

        Assertions.assertEquals(AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart, res.type,
                "The type property is incorrect")
        ExpressionsCommonsTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct from end element`() {
        val text = AbsoluteElementAnchorLexemeNode.fromEndIdentifier + IndexerNodeTest.testExpression
        val parser = LexemParser(IOStringReader.from(text))
        val res = AbsoluteElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AbsoluteElementAnchorLexemeNode

        Assertions.assertEquals(AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromEnd, res.type,
                "The type property is incorrect")
        ExpressionsCommonsTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct from analysis beginning element`() {
        val text = AbsoluteElementAnchorLexemeNode.analysisBeginningIdentifier + IndexerNodeTest.testExpression
        val parser = LexemParser(IOStringReader.from(text))
        val res = AbsoluteElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AbsoluteElementAnchorLexemeNode

        Assertions.assertEquals(AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.AnalysisBeginning, res.type,
                "The type property is incorrect")
        ExpressionsCommonsTest.checkTestExpression(res.expression)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect element without expression`() {
        TestUtils.assertParserException(AngmarParserExceptionType.AbsoluteAnchorElementWithoutValue) {
            val text = AbsoluteElementAnchorLexemeNode.analysisBeginningIdentifier
            val parser = LexemParser(IOStringReader.from(text))
            AbsoluteElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AbsoluteElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
