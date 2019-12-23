package org.lexem.angmar.parser.descriptive.selectors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import java.util.stream.*
import kotlin.streams.*

internal class PropertyAbbreviationSelectorNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = IdentifierNodeTest.testExpression

        @JvmStatic
        private fun provideSimpleProperty(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegated in listOf(false, true)) {
                    for (isAtIdentifier in listOf(false, true)) {
                        var text = IdentifierNodeTest.testExpression

                        if (isAtIdentifier) {
                            text = "${PropertyAbbreviationSelectorNode.atPrefix}$text"
                        }

                        if (isNegated) {
                            text = "${PropertyAbbreviationSelectorNode.notOperator}$text"
                        }

                        yield(Arguments.of(text, isNegated, isAtIdentifier))
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideSimplePropertyForAddition(): Stream<Arguments> {
            val sequence = sequence {
                for (isNegated in listOf(false, true)) {
                    var text = IdentifierNodeTest.testExpression

                    if (isNegated) {
                        text = "${PropertyAbbreviationSelectorNode.notOperator}$text"
                    }

                    yield(Arguments.of(text, isNegated))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun providePropertyWithBlock(): Stream<Arguments> {
            val sequence = sequence {
                for (isAtIdentifier in listOf(false, true)) {
                    var text = IdentifierNodeTest.testExpression

                    if (isAtIdentifier) {
                        text = "${PropertyAbbreviationSelectorNode.atPrefix}$text"
                    }

                    text += PropertyBlockSelectorNodeTest.testExpression

                    yield(Arguments.of(text, isAtIdentifier))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, isAddition: Boolean) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as PropertyAbbreviationSelectorNode

            Assertions.assertFalse(node.isNegated, "The isNegated property is incorrect")
            Assertions.assertFalse(node.isAtIdentifier, "The property isAtIdentifier is incorrect")
            Assertions.assertNotNull(node.name, "The property name cannot be null")
            IdentifierNodeTest.checkTestExpression(node.name)
            Assertions.assertNull(node.propertyBlock, "The property propertyBlock must be null")
            Assertions.assertEquals(isAddition, node.isAddition, "The isAddition property is incorrect")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideSimpleProperty")
    fun `parse correct simple property`(text: String, isNegated: Boolean, isAtIdentifier: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertyAbbreviationSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyAbbreviationSelectorNode

        Assertions.assertEquals(isNegated, res.isNegated, "The isNegated property is incorrect")
        Assertions.assertEquals(isAtIdentifier, res.isAtIdentifier, "The property isAtIdentifier is incorrect")
        Assertions.assertNotNull(res.name, "The property name cannot be null")
        IdentifierNodeTest.checkTestExpression(res.name)
        Assertions.assertNull(res.propertyBlock, "The property propertyBlock must be null")
        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideSimplePropertyForAddition")
    fun `parse correct simple property - for addition`(text: String, isNegated: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertyAbbreviationSelectorNode.parseForAddition(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyAbbreviationSelectorNode

        Assertions.assertEquals(isNegated, res.isNegated, "The isNegated property is incorrect")
        Assertions.assertFalse(res.isAtIdentifier, "The property isAtIdentifier is incorrect")
        Assertions.assertNotNull(res.name, "The property name cannot be null")
        IdentifierNodeTest.checkTestExpression(res.name)
        Assertions.assertNull(res.propertyBlock, "The property propertyBlock must be null")
        Assertions.assertTrue(res.isAddition, "The isAddition property is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("providePropertyWithBlock")
    fun `parse correct property with block`(text: String, isAtIdentifier: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertyAbbreviationSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyAbbreviationSelectorNode

        Assertions.assertFalse(res.isNegated, "The isNegated property is incorrect")
        Assertions.assertEquals(isAtIdentifier, res.isAtIdentifier, "The property isAtIdentifier is incorrect")
        Assertions.assertNotNull(res.name, "The property name cannot be null")
        IdentifierNodeTest.checkTestExpression(res.name)
        Assertions.assertNotNull(res.propertyBlock, "The property name cannot be null")
        PropertyBlockSelectorNodeTest.checkTestExpression(res.propertyBlock!!, isAddition = false)
        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct property with block - for addition`() {
        val text = IdentifierNodeTest.testExpression + PropertyBlockSelectorNodeTest.testExpression
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertyAbbreviationSelectorNode.parseForAddition(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyAbbreviationSelectorNode

        Assertions.assertFalse(res.isNegated, "The isNegated property is incorrect")
        Assertions.assertFalse(res.isAtIdentifier, "The property isAtIdentifier is incorrect")
        Assertions.assertNotNull(res.name, "The property name cannot be null")
        IdentifierNodeTest.checkTestExpression(res.name)
        Assertions.assertNotNull(res.propertyBlock, "The property name cannot be null")
        PropertyBlockSelectorNodeTest.checkTestExpression(res.propertyBlock!!, isAddition = true)
        Assertions.assertTrue(res.isAddition, "The isAddition property is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(
            strings = ["", PropertyAbbreviationSelectorNode.notOperator, PropertyAbbreviationSelectorNode.atPrefix, "${PropertyAbbreviationSelectorNode.notOperator}${PropertyAbbreviationSelectorNode.atPrefix}"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertyAbbreviationSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

