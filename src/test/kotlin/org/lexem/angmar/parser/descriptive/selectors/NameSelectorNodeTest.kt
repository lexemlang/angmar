package org.lexem.angmar.parser.descriptive.selectors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class NameSelectorNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = IdentifierNodeTest.testExpression

        @JvmStatic
        private fun provideNameGroups(): Stream<Arguments> {
            val sequence = sequence {
                for (i in 1..5) {
                    val names = List(i) { IdentifierNodeTest.testExpression }
                    var nameGroupText = names.joinToString(NameSelectorNode.elementSeparator)

                    yield(Arguments.of(
                            "${NameSelectorNode.groupStartToken}$nameGroupText${NameSelectorNode.groupEndToken}", i))


                    nameGroupText = names.joinToString(" ${NameSelectorNode.elementSeparator} ")

                    yield(Arguments.of(
                            "${NameSelectorNode.groupStartToken} $nameGroupText ${NameSelectorNode.groupEndToken}", i))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, isAddition: Boolean) {
            Assertions.assertTrue(node is NameSelectorNode, "The node is not a NameSelectorNode")
            node as NameSelectorNode

            Assertions.assertFalse(node.isNegated, "The isNegated property is incorrect")
            Assertions.assertEquals(1, node.names.size, "The number of names is incorrect")
            Assertions.assertEquals(isAddition, node.isAddition, "The isAddition property is incorrect")

            IdentifierNodeTest.checkTestExpression(node.names.first())
        }
    }


    // TESTS ------------------------------------------------------------------


    @Test
    fun `parse correct affirmative simple name`() {
        val text = IdentifierNodeTest.testExpression
        val parser = LexemParser(IOStringReader.from(text))
        val res = NameSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as NameSelectorNode

        Assertions.assertFalse(res.isNegated, "The isNegated property is incorrect")
        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")
        Assertions.assertEquals(1, res.names.size, "The number of names is incorrect")

        IdentifierNodeTest.checkTestExpression(res.names.first())

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct affirmative simple name - for additive`() {
        val text = IdentifierNodeTest.testExpression
        val parser = LexemParser(IOStringReader.from(text))
        val res = NameSelectorNode.parseForAddition(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as NameSelectorNode

        Assertions.assertFalse(res.isNegated, "The isNegated property is incorrect")
        Assertions.assertTrue(res.isAddition, "The isAddition property is incorrect")
        Assertions.assertEquals(1, res.names.size, "The number of names is incorrect")

        IdentifierNodeTest.checkTestExpression(res.names.first())

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    fun `parse correct negative simple name`() {
        val text = "${NameSelectorNode.notOperator}${IdentifierNodeTest.testExpression}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = NameSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as NameSelectorNode

        Assertions.assertTrue(res.isNegated, "The isNegated property is incorrect")
        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")
        Assertions.assertEquals(1, res.names.size, "The number of names is incorrect")

        IdentifierNodeTest.checkTestExpression(res.names.first())

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideNameGroups")
    fun `parse correct affirmative group of names`(text: String, nameCount: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = NameSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as NameSelectorNode

        Assertions.assertFalse(res.isNegated, "The isNegated property is incorrect")
        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")
        Assertions.assertEquals(nameCount, res.names.size, "The number of names is incorrect")

        res.names.forEach {
            IdentifierNodeTest.checkTestExpression(it)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideNameGroups")
    fun `parse correct negative group of names`(groupNames: String, nameCount: Int) {
        val text = "${NameSelectorNode.notOperator}$groupNames"
        val parser = LexemParser(IOStringReader.from(text))
        val res = NameSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as NameSelectorNode

        Assertions.assertTrue(res.isNegated, "The isNegated property is incorrect")
        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")
        Assertions.assertEquals(nameCount, res.names.size, "The number of names is incorrect")

        res.names.forEach {
            IdentifierNodeTest.checkTestExpression(it)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect group of names without any name`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorNameGroupWithoutNames) {
            val text = "${NameSelectorNode.groupStartToken}${NameSelectorNode.groupEndToken}"
            val parser = LexemParser(IOStringReader.from(text))
            NameSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect group of names without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorNameGroupWithoutEndToken) {
            val text = "${NameSelectorNode.groupStartToken}${IdentifierNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            NameSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", NameSelectorNode.notOperator])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = NameSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
