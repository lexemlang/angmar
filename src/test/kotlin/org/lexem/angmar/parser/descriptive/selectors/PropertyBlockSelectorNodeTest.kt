package org.lexem.angmar.parser.descriptive.selectors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class PropertyBlockSelectorNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${PropertyBlockSelectorNode.startToken}${ExpressionsCommonsTest.testExpression}${PropertyBlockSelectorNode.endToken}"

        @JvmStatic
        private fun provideSimpleBlock(): Stream<Arguments> {
            val sequence = sequence {
                var text =
                        "${PropertyBlockSelectorNode.startToken}${ExpressionsCommonsTest.testExpression}${PropertyBlockSelectorNode.endToken}"
                yield(Arguments.of(text))

                text =
                        "${PropertyBlockSelectorNode.startToken} ${ExpressionsCommonsTest.testExpression} ${PropertyBlockSelectorNode.endToken}"
                yield(Arguments.of(text))
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideBlockWithAlias(): Stream<Arguments> {
            val sequence = sequence {
                var text =
                        "${PropertyBlockSelectorNode.startToken}${IdentifierNodeTest.testExpression}${PropertyBlockSelectorNode.relationalToken}${ExpressionsCommonsTest.testExpression}${PropertyBlockSelectorNode.endToken}"
                yield(Arguments.of(text))

                text =
                        "${PropertyBlockSelectorNode.startToken} ${IdentifierNodeTest.testExpression} ${PropertyBlockSelectorNode.relationalToken} ${ExpressionsCommonsTest.testExpression} ${PropertyBlockSelectorNode.endToken}"
                yield(Arguments.of(text))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, isAddition: Boolean) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as PropertyBlockSelectorNode

            Assertions.assertNull(node.identifier, "The identifier property is incorrect")
            Assertions.assertEquals(isAddition, node.isAddition, "The isAddition property is incorrect")
            ExpressionsCommonsTest.checkTestExpression(node.condition)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideSimpleBlock")
    fun `parse correct simple block`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertyBlockSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyBlockSelectorNode

        Assertions.assertNull(res.identifier, "The identifier property must be null")
        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")
        ExpressionsCommonsTest.checkTestExpression(res.condition)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideSimpleBlock")
    fun `parse correct simple block - for addition`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertyBlockSelectorNode.parseForAddition(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyBlockSelectorNode

        Assertions.assertNull(res.identifier, "The identifier property must be null")
        Assertions.assertTrue(res.isAddition, "The isAddition property is incorrect")
        ExpressionsCommonsTest.checkTestExpression(res.condition)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideBlockWithAlias")
    fun `parse correct block with alias`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertyBlockSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyBlockSelectorNode

        Assertions.assertNotNull(res.identifier, "The identifier property cannot be null")
        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")
        IdentifierNodeTest.checkTestExpression(res.identifier!!)
        ExpressionsCommonsTest.checkTestExpression(res.condition)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect block without condition`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorPropertyBlockWithoutCondition) {
            val text = "${PropertyBlockSelectorNode.startToken}${PropertyBlockSelectorNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            PropertyBlockSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect block without condition - for addition`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorPropertyBlockWithoutCondition) {
            val text = "${PropertyBlockSelectorNode.startToken}${PropertyBlockSelectorNode.endToken}"
            val parser = LexemParser(IOStringReader.from(text))
            PropertyBlockSelectorNode.parseForAddition(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect block without end token`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorPropertyBlockWithoutEndToken) {
            val text = "${PropertyBlockSelectorNode.startToken}${ExpressionsCommonsTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            PropertyBlockSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect block without end token - for addition`() {
        TestUtils.assertParserException(AngmarParserExceptionType.SelectorPropertyBlockWithoutEndToken) {
            val text = "${PropertyBlockSelectorNode.startToken}${ExpressionsCommonsTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            PropertyBlockSelectorNode.parseForAddition(parser, ParserNode.Companion.EmptyParserNode)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = PropertyBlockSelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

