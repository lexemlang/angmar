package org.lexem.angmar.parser.descriptive.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class OnBackBlockStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = OnBackBlockStmtNode.macroName + BlockStmtNodeTest.testExpression

        @JvmStatic
        private fun provideStatement(): Stream<Arguments> {
            val sequence = sequence {
                for (hasParameters in listOf(false, true)) {
                    var text = OnBackBlockStmtNode.macroName

                    if (hasParameters) {
                        text += FunctionParameterListNodeTest.testExpression
                    }

                    text += BlockStmtNodeTest.testExpression

                    yield(Arguments.of(text, hasParameters))

                    // With whitespaces
                    text = OnBackBlockStmtNode.macroName

                    if (hasParameters) {
                        text += " " + FunctionParameterListNodeTest.testExpression
                    }

                    text += " " + BlockStmtNodeTest.testExpression

                    yield(Arguments.of(text, hasParameters))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideConditionalStatement(): Stream<Arguments> {
            val sequence = sequence {
                for (hasParameters in listOf(false, true)) {
                    var text = OnBackBlockStmtNode.macroName

                    if (hasParameters) {
                        text += FunctionParameterListNodeTest.testExpression
                    }

                    text += ConditionalStmtNodeTest.testExpression

                    yield(Arguments.of(text, hasParameters))

                    // With whitespaces
                    text = OnBackBlockStmtNode.macroName

                    if (hasParameters) {
                        text += " " + FunctionParameterListNodeTest.testExpression
                    }

                    text += " " + ConditionalStmtNodeTest.testExpression

                    yield(Arguments.of(text, hasParameters))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideSelectiveStatement(): Stream<Arguments> {
            val sequence = sequence {
                for (hasParameters in listOf(false, true)) {
                    var text = OnBackBlockStmtNode.macroName

                    if (hasParameters) {
                        text += FunctionParameterListNodeTest.testExpression
                    }

                    text += SelectiveStmtNodeTest.testExpression

                    yield(Arguments.of(text, hasParameters))

                    // With whitespaces
                    text = OnBackBlockStmtNode.macroName

                    if (hasParameters) {
                        text += " " + FunctionParameterListNodeTest.testExpression
                    }

                    text += " " + SelectiveStmtNodeTest.testExpression

                    yield(Arguments.of(text, hasParameters))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as OnBackBlockStmtNode

            Assertions.assertNull(node.parameters, "The parameters property must be null")

            BlockStmtNodeTest.checkTestExpression(node.block)
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideStatement")
    fun `parse correct onBack statement`(text: String, hasParameters: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = OnBackBlockStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as OnBackBlockStmtNode

        if (hasParameters) {
            Assertions.assertNotNull(res.parameters, "The parameters property cannot be null")
            FunctionParameterListNodeTest.checkTestExpression(res.parameters!!)
        } else {
            Assertions.assertNull(res.parameters, "The parameters property must be null")
        }

        BlockStmtNodeTest.checkTestExpression(res.block)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideConditionalStatement")
    fun `parse correct onBack statement with conditional`(text: String, hasParameters: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = OnBackBlockStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as OnBackBlockStmtNode

        if (hasParameters) {
            Assertions.assertNotNull(res.parameters, "The parameters property cannot be null")
            FunctionParameterListNodeTest.checkTestExpression(res.parameters!!)
        } else {
            Assertions.assertNull(res.parameters, "The parameters property must be null")
        }

        ConditionalStmtNodeTest.checkTestExpression(res.block)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideSelectiveStatement")
    fun `parse correct onBack statement with selective`(text: String, hasParameters: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = OnBackBlockStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as OnBackBlockStmtNode

        if (hasParameters) {
            Assertions.assertNotNull(res.parameters, "The parameters property cannot be null")
            FunctionParameterListNodeTest.checkTestExpression(res.parameters!!)
        } else {
            Assertions.assertNull(res.parameters, "The parameters property must be null")
        }

        SelectiveStmtNodeTest.checkTestExpression(res.block)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect onBack statement without block`() {
        TestUtils.assertParserException(AngmarParserExceptionType.OnBackBlockWithoutBlock) {
            val text = OnBackBlockStmtNode.macroName
            val parser = LexemParser(IOStringReader.from(text))
            OnBackBlockStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = OnBackBlockStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
