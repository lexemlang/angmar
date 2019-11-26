package org.lexem.angmar.parser.descriptive.selectors

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

internal class PropertySelectorNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${PropertySelectorNode.token}${PropertyAbbreviationSelectorNodeTest.testExpression}"

        @JvmStatic
        private fun providePropertyGroups(): Stream<Arguments> {
            val sequence = sequence {
                for (i in 1..5) {
                    val list = List(i) { PropertyAbbreviationSelectorNodeTest.testExpression }
                    var text = list.joinToString(PropertySelectorNode.elementSeparator)
                    text =
                            "${PropertySelectorNode.token}${PropertySelectorNode.groupStartToken}$text${PropertySelectorNode.groupEndToken}"

                    yield(Arguments.of(text, i))

                    text = list.joinToString(" ${PropertySelectorNode.elementSeparator} ")
                    text =
                            "${PropertySelectorNode.token}${PropertySelectorNode.groupStartToken} $text ${PropertySelectorNode.groupEndToken}"

                    yield(Arguments.of(text, i))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, isAddition: Boolean) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as PropertySelectorNode

            Assertions.assertEquals(isAddition, node.isAddition, "The isAddition property is incorrect")

            Assertions.assertEquals(1, node.properties.size, "The number of properties is incorrect")
            for (prop in node.properties) {
                PropertyAbbreviationSelectorNodeTest.checkTestExpression(prop, isAddition)
            }
        }
    }


    // TESTS ------------------------------------------------------------------


    @Test
    fun `parse correct simple property`() {
        val text = "${PropertySelectorNode.token}${PropertyAbbreviationSelectorNodeTest.testExpression}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertySelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertySelectorNode

        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")
        Assertions.assertEquals(1, res.properties.size, "The number of properties is incorrect")
        for (prop in res.properties) {
            PropertyAbbreviationSelectorNodeTest.checkTestExpression(prop, isAddition = false)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct simple property - for addition`() {
        val text = "${PropertySelectorNode.token}${PropertyAbbreviationSelectorNodeTest.testExpression}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertySelectorNode.parseForAddition(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertySelectorNode

        Assertions.assertTrue(res.isAddition, "The isAddition property is incorrect")
        Assertions.assertEquals(1, res.properties.size, "The number of properties is incorrect")
        for (prop in res.properties) {
            PropertyAbbreviationSelectorNodeTest.checkTestExpression(prop, isAddition = true)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("providePropertyGroups")
    fun `parse correct group of properties`(text: String, propCount: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertySelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertySelectorNode

        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")
        Assertions.assertEquals(propCount, res.properties.size, "The number of properties is incorrect")
        for (prop in res.properties) {
            PropertyAbbreviationSelectorNodeTest.checkTestExpression(prop, isAddition = false)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect property`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorPropertyWithoutIdentifier) {
            val text = PropertySelectorNode.token
            val parser = LexemParser(IOStringReader.from(text))
            PropertySelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect property - for addition`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorPropertyWithoutIdentifier) {
            val text = PropertySelectorNode.token
            val parser = LexemParser(IOStringReader.from(text))
            PropertySelectorNode.parseForAddition(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertySelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect property group without properties`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorPropertyGroupWithoutProperties) {
            val text =
                    "${PropertySelectorNode.token}${PropertySelectorNode.groupStartToken}${PropertySelectorNode.groupEndToken}"
            val parser = LexemParser(IOStringReader.from(text))
            PropertySelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect property group without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorPropertyGroupWithoutEndToken) {
            val text =
                    "${PropertySelectorNode.token}${PropertySelectorNode.groupStartToken}${PropertyAbbreviationSelectorNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            PropertySelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }
}

