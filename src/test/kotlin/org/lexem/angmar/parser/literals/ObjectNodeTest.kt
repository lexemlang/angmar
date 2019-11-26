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

internal class ObjectNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ObjectNode.startToken}${ObjectElementNodeTest.correctObjectElement}${ObjectNode.endToken}"

        @JvmStatic
        private fun provideCorrectObjects(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..3) {
                    yield(Arguments.of(List(i) { ObjectElementNodeTest.correctObjectElement }.joinToString(
                            ObjectNode.elementSeparator), i, 1))
                }

                for (i in 1..3) {
                    yield(Arguments.of(
                            List(i) { ObjectSimplificationNodeTest.correctObjectSimplificationElement }.joinToString(
                                    ObjectNode.elementSeparator), i, 2))
                }

                for (i in 1..3) {
                    yield(Arguments.of(
                            List(i) { ObjectSimplificationNodeTest.correctObjectSimplificationElementLikeFunction }.joinToString(
                                    ObjectNode.elementSeparator), i, 3))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideCorrectConstantObjects(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..3) {
                    yield(Arguments.of(List(i) { ObjectElementNodeTest.correctConstantObjectElement }.joinToString(
                            ObjectNode.elementSeparator), i, 1))
                }

                for (i in 1..3) {
                    yield(Arguments.of(
                            List(i) { ObjectSimplificationNodeTest.correctConstantObjectSimplificationElement }.joinToString(
                                    ObjectNode.elementSeparator), i, 2))
                }

                for (i in 1..3) {
                    yield(Arguments.of(
                            List(i) { ObjectSimplificationNodeTest.correctConstantObjectSimplificationElementLikeFunction }.joinToString(
                                    ObjectNode.elementSeparator), i, 3))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideCorrectObjectWithWS(): Stream<Arguments> {
            val result = sequence {
                for (i in 0..3) {
                    yield(Arguments.of(List(i) { ObjectElementNodeTest.correctObjectElementWithWS }.joinToString(
                            " ${ObjectNode.elementSeparator} "), i, 1))
                }

                for (i in 1..3) {
                    yield(Arguments.of(
                            List(i) { ObjectSimplificationNodeTest.correctObjectSimplificationElementWithWS }.joinToString(
                                    ObjectNode.elementSeparator), i, 2))
                }

                for (i in 1..3) {
                    yield(Arguments.of(
                            List(i) { ObjectSimplificationNodeTest.correctObjectSimplificationElementWithWSLikeFunction }.joinToString(
                                    ObjectNode.elementSeparator), i, 3))
                }
            }

            return result.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is ObjectNode, "The node is not a ObjectNode")
            node as ObjectNode

            Assertions.assertFalse(node.isConstant, "The isConstant property is incorrect")
            Assertions.assertEquals(1, node.elements.size, "The number of elements did not match")
            ObjectElementNodeTest.checkTestExpression(node.elements.first(), false)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectObjects")
    fun `parse correct object literal`(objects: String, size: Int, type: Int) {
        val text = "${ObjectNode.startToken}$objects${ObjectNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = ObjectNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        when (type) {
            1 -> {
                for (exp in res.elements) {
                    ObjectElementNodeTest.checkTestExpression(exp, isConstant = false)
                }
            }
            2 -> {
                for (exp in res.elements) {
                    ObjectSimplificationNodeTest.checkTestExpression(exp, isConstant = false, isFunction = false)
                }
            }
            3 -> {
                for (exp in res.elements) {
                    ObjectSimplificationNodeTest.checkTestExpression(exp, isConstant = false, isFunction = true)
                }
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectConstantObjects")
    fun `parse correct constant object literal`(objects: String, size: Int, type: Int) {
        val text = "${ObjectNode.constantToken}${ObjectNode.startToken}$objects${ObjectNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = ObjectNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectNode

        Assertions.assertTrue(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        when (type) {
            1 -> {
                for (exp in res.elements) {
                    ObjectElementNodeTest.checkTestExpression(exp, isConstant = true)
                }
            }
            2 -> {
                for (exp in res.elements) {
                    ObjectSimplificationNodeTest.checkTestExpression(exp, isConstant = true, isFunction = false)
                }
            }
            3 -> {
                for (exp in res.elements) {
                    ObjectSimplificationNodeTest.checkTestExpression(exp, isConstant = true, isFunction = true)
                }
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectObjectWithWS")
    fun `parse correct object literal with whitespaces`(objects: String, size: Int, type: Int) {
        val text = "${ObjectNode.startToken} $objects ${ObjectNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = ObjectNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        when (type) {
            1 -> {
                for (exp in res.elements) {
                    ObjectElementNodeTest.checkTestExpression(exp, isConstant = false)
                }
            }
            2 -> {
                for (exp in res.elements) {
                    ObjectSimplificationNodeTest.checkTestExpression(exp, isConstant = false, isFunction = false)
                }
            }
            3 -> {
                for (exp in res.elements) {
                    ObjectSimplificationNodeTest.checkTestExpression(exp, isConstant = false, isFunction = true)
                }
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideCorrectObjects")
    fun `parse correct object literal with trailing comma`(objects: String, size: Int, type: Int) {
        val text = "${ObjectNode.startToken}$objects${ObjectNode.elementSeparator}${ObjectNode.endToken}"
        val parser = LexemParser(IOStringReader.from(text))
        val res = ObjectNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ObjectNode

        Assertions.assertFalse(res.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(size, res.elements.size, "The number of elements did not match")

        when (type) {
            1 -> {
                for (exp in res.elements) {
                    ObjectElementNodeTest.checkTestExpression(exp, isConstant = false)
                }
            }
            2 -> {
                for (exp in res.elements) {
                    ObjectSimplificationNodeTest.checkTestExpression(exp, isConstant = false, isFunction = false)
                }
            }
            3 -> {
                for (exp in res.elements) {
                    ObjectSimplificationNodeTest.checkTestExpression(exp, isConstant = false, isFunction = true)
                }
            }
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect object literal with no endToken`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ObjectWithoutEndToken) {
            val text = "${ObjectNode.startToken}${ObjectElementNodeTest.correctObjectElement}"
            val parser = LexemParser(IOStringReader.from(text))
            ObjectNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3", ObjectNode.constantToken])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ObjectNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

