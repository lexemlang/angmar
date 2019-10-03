package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*

internal class SetNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${SetNode.macroName}${ListNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectLists() = ListNodeTest.provideCorrectLists()

        @JvmStatic
        private fun provideCorrectListsWithWS() = ListNodeTest.provideCorrectListsWithWS()

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is SetNode, "The node is not a SetNode")
            node as SetNode

            ListNodeTest.checkTestExpression(node.list)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectLists")
    fun `parse correct set literal`(list: String, size: Int) {
        val text = "${SetNode.macroName}${ListNode.startToken}$list${ListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = SetNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as SetNode

        let {
            val listNode = res.list

            Assertions.assertFalse(listNode.isConstant, "The isConstant property is incorrect")
            Assertions.assertEquals(size, listNode.elements.size, "The number of elements did not match")

            for (exp in listNode.elements) {
                ExpressionsCommonsTest.checkTestExpression(exp)
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectLists", "provideCorrectListsWithWS")
    fun `parse correct constant set literal`(list: String, size: Int) {
        val text = "${SetNode.macroName}${ListNode.constantToken}${ListNode.startToken}$list${ListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = SetNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as SetNode

        let {
            val listNode = res.list

            Assertions.assertTrue(listNode.isConstant, "The isConstant property is incorrect")
            Assertions.assertEquals(size, listNode.elements.size, "The number of elements did not match")

            for (exp in listNode.elements) {
                ExpressionsCommonsTest.checkTestExpression(exp)
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectListsWithWS")
    fun `parse correct set literal with whitespaces`(list: String, size: Int) {
        val text = "${SetNode.macroName}${ListNode.startToken} $list ${ListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = SetNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as SetNode

        let {
            val listNode = res.list

            Assertions.assertFalse(listNode.isConstant, "The isConstant property is incorrect")
            Assertions.assertEquals(size, listNode.elements.size, "The number of elements did not match")

            for (exp in listNode.elements) {
                ExpressionsCommonsTest.checkTestExpression(exp)
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectLists")
    fun `parse correct set literal with trailing comma`(list: String, size: Int) {
        val text = "${SetNode.macroName}${ListNode.startToken}$list${ListNode.elementSeparator}${ListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = SetNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as SetNode

        let {
            val listNode = res.list

            Assertions.assertFalse(listNode.isConstant, "The isConstant property is incorrect")
            Assertions.assertEquals(size, listNode.elements.size, "The number of elements did not match")

            for (exp in listNode.elements) {
                ExpressionsCommonsTest.checkTestExpression(exp)
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect set literal with no startToken`() {
        TestUtils.assertParserException {
            val text = SetNode.macroName
            val parser = LexemParser(CustomStringReader.from(text))
            SetNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect set literal with no expression`() {
        TestUtils.assertParserException {
            val text = "${SetNode.macroName}${ListNode.startToken}"
            val parser = LexemParser(CustomStringReader.from(text))
            SetNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @Test
    @Incorrect
    fun `parse incorrect set literal with no endToken`() {
        TestUtils.assertParserException {
            val text = "${SetNode.macroName}${ListNode.startToken}${ExpressionsCommonsTest.testExpression}"
            val parser = LexemParser(CustomStringReader.from(text))
            SetNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = SetNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

