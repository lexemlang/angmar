package org.lexem.angmar.parser.descriptive.lexemes.anchors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*

internal class RelativeElementAnchorLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = RelativeElementAnchorLexemeNode.textIdentifier


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as RelativeElementAnchorLexemeNode

            Assertions.assertEquals(RelativeElementAnchorLexemeNode.RelativeAnchorType.Text, node.type,
                    "The type property is incorrect")
            Assertions.assertNull(node.value, "The value property must be null")
        }
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `parse correct text element`() {
        val text = RelativeElementAnchorLexemeNode.textIdentifier
        val parser = LexemParser(IOStringReader.from(text))
        val res = RelativeElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as RelativeElementAnchorLexemeNode

        Assertions.assertEquals(RelativeElementAnchorLexemeNode.RelativeAnchorType.Text, res.type,
                "The type property is incorrect")
        Assertions.assertNull(res.value, "The value property must be null")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct line element`() {
        val text = RelativeElementAnchorLexemeNode.lineIdentifier
        val parser = LexemParser(IOStringReader.from(text))
        val res = RelativeElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as RelativeElementAnchorLexemeNode

        Assertions.assertEquals(RelativeElementAnchorLexemeNode.RelativeAnchorType.Line, res.type,
                "The type property is incorrect")
        Assertions.assertNull(res.value, "The value property must be null")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct string element`() {
        val text = LiteralCommonsTest.testAnyString
        val parser = LexemParser(IOStringReader.from(text))
        val res = RelativeElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as RelativeElementAnchorLexemeNode

        Assertions.assertEquals(RelativeElementAnchorLexemeNode.RelativeAnchorType.StringValue, res.type,
                "The type property is incorrect")
        Assertions.assertNotNull(res.value, "The value property cannot be null")
        LiteralCommonsTest.checkTestAnyString(res.value!!)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct bitlist element`() {
        val text = BitlistNodeTest.testExpression
        val parser = LexemParser(IOStringReader.from(text))
        val res = RelativeElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as RelativeElementAnchorLexemeNode

        Assertions.assertEquals(RelativeElementAnchorLexemeNode.RelativeAnchorType.BitListValue, res.type,
                "The type property is incorrect")
        Assertions.assertNotNull(res.value, "The value property cannot be null")
        BitlistNodeTest.checkTestExpression(res.value!!)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct interval element`() {
        val text = LiteralCommonsTest.testAnyIntervalForLexem
        val parser = LexemParser(IOStringReader.from(text))
        val res = RelativeElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as RelativeElementAnchorLexemeNode

        Assertions.assertEquals(RelativeElementAnchorLexemeNode.RelativeAnchorType.IntervalValue, res.type,
                "The type property is incorrect")
        Assertions.assertNotNull(res.value, "The value property cannot be null")
        LiteralCommonsTest.checkTestAnyIntervalForLexem(res.value!!)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = RelativeElementAnchorLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

