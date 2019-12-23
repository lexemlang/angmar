package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class MapNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${MapNode.macroName}${MapNode.startToken}${MapElementNodeTest.correctMapElement}${MapNode.endToken}"

        @JvmStatic
        private fun provideCorrectMaps(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..6) {
                    yield(Arguments.of(
                            List(i) { MapElementNodeTest.correctMapElement }.joinToString(MapNode.elementSeparator), i))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideCorrectMapsWithWS(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..6) {
                    yield(Arguments.of(List(i) { MapElementNodeTest.correctMapElementWithWS }.joinToString(
                            " ${MapNode.elementSeparator} "), i))
                }
            }

            return result.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is MapNode, "The node is not a MapNode")
            node as MapNode

            Assertions.assertFalse(node.isConstant, "The isConstant property is incorrect")
            Assertions.assertEquals(1, node.elements.size, "The number of elements did not match")
            MapElementNodeTest.checkTestExpression(node.elements.first())
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectMaps")
    fun `parse correct map literal`(map: String, size: Int) {
        val text = "${MapNode.macroName}${MapNode.startToken}$map${MapNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = MapNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MapNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        for (exp in res.elements) {
            MapElementNodeTest.checkTestExpression(exp)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectMaps", "provideCorrectMapsWithWS")
    fun `parse correct constant map literal`(map: String, size: Int) {
        val text = "${MapNode.macroName}${MapNode.constantToken}${MapNode.startToken}$map${MapNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = MapNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MapNode

        Assertions.assertTrue(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        for (exp in res.elements) {
            MapElementNodeTest.checkTestExpression(exp)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectMapsWithWS")
    fun `parse correct map literal with whitespaces`(map: String, size: Int) {
        val text = "${MapNode.macroName}${MapNode.startToken} $map ${MapNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = MapNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MapNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        for (exp in res.elements) {
            MapElementNodeTest.checkTestExpression(exp)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectMaps")
    fun `parse correct map literal with trailing comma`(map: String, size: Int) {
        val text = "${MapNode.macroName}${MapNode.startToken}$map${MapNode.elementSeparator}${MapNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = MapNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as MapNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        for (exp in res.elements) {
            MapElementNodeTest.checkTestExpression(exp)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect map literal with no startToken`() {
        TestUtils.assertParserException(AngmarParserExceptionType.MapWithoutStartToken) {
            val text = MapNode.macroName
            val parser = LexemParser(IOStringReader.from(text))
            MapNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect map literal with no endToken`() {
        TestUtils.assertParserException(AngmarParserExceptionType.MapWithoutEndToken) {
            val text = "${MapNode.macroName}${MapNode.startToken}${MapElementNodeTest.correctMapElement}"
            val parser = LexemParser(IOStringReader.from(text))
            MapNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = MapNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
