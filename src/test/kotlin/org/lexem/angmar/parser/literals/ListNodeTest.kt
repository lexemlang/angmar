package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class ListNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${ListNode.startToken}${ExpressionsCommonsTest.testExpression}${ListNode.endToken}"

        @JvmStatic
        fun provideCorrectLists(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..6) {
                    yield(Arguments.of(
                            List(i) { ExpressionsCommonsTest.testExpression }.joinToString(ListNode.elementSeparator),
                            i))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        fun provideCorrectListsWithWS(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..6) {
                    yield(Arguments.of(List(i) { ExpressionsCommonsTest.testExpression }.joinToString(
                            " ${ListNode.elementSeparator} "), i))
                }
            }

            return result.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ListNode, "The node is not a ListNode")
            node as ListNode

            Assertions.assertFalse(node.isConstant, "The isConstant property is incorrect")
            Assertions.assertEquals(1, node.elements.size, "The number of elements did not match")
            ExpressionsCommonsTest.checkTestExpression(node.elements.first())
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectLists")
    fun `parse correct list literal`(list: String, size: Int) {
        val text = "${ListNode.startToken}$list${ListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ListNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ListNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        for (exp in res.elements) {
            ExpressionsCommonsTest.checkTestExpression(exp)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectLists", "provideCorrectListsWithWS")
    fun `parse correct constant list literal`(list: String, size: Int) {
        val text = "${ListNode.constantToken}${ListNode.startToken}$list${ListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ListNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ListNode

        Assertions.assertTrue(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        for (exp in res.elements) {
            ExpressionsCommonsTest.checkTestExpression(exp)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectListsWithWS")
    fun `parse correct list literal with whitespaces`(list: String, size: Int) {
        val text = "${ListNode.startToken} $list ${ListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ListNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ListNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        for (exp in res.elements) {
            ExpressionsCommonsTest.checkTestExpression(exp)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectLists")
    fun `parse correct list literal with trailing comma`(list: String, size: Int) {
        val text = "${ListNode.startToken}$list${ListNode.elementSeparator}${ListNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ListNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ListNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        for (exp in res.elements) {
            ExpressionsCommonsTest.checkTestExpression(exp)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect list literal with no expression`() {
        assertParserException {
            val text = ListNode.startToken
            val parser = LexemParser(CustomStringReader.from(text))
            ListNode.parse(parser)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(strings = [ExpressionsCommonsTest.testExpression])
    fun `parse incorrect list literal with no endToken`(expression: String) {
        assertParserException {
            val text = "${ListNode.startToken}$expression"
            val parser = LexemParser(CustomStringReader.from(text))
            ListNode.parse(parser)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3", ListNode.constantToken])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = ListNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
