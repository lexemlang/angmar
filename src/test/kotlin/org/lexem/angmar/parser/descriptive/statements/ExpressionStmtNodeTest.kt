package org.lexem.angmar.parser.descriptive.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class ExpressionStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${ExpressionStmtNode.keyword} ${IdentifierNodeTest.testExpression}${BlockStmtNodeTest.testExpression}"

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (hasProperties in listOf(false, true)) {
                    for (hasParameters in listOf(false, true)) {
                        var text = "${ExpressionStmtNode.keyword} ${IdentifierNodeTest.testExpression}"

                        if (hasProperties) {
                            text += PropertyStyleObjectBlockNodeTest.testExpression
                        }

                        if (hasParameters) {
                            text += FunctionParameterListNodeTest.testExpression
                        }

                        text += BlockStmtNodeTest.testExpression

                        yield(Arguments.of(text, hasProperties, hasParameters))

                        // With whitespaces
                        text = "${ExpressionStmtNode.keyword} ${IdentifierNodeTest.testExpression}"

                        if (hasProperties) {
                            text += " ${PropertyStyleObjectBlockNodeTest.testExpression}"
                        }

                        if (hasParameters) {
                            text += " ${FunctionParameterListNodeTest.testExpression}"
                        }

                        text += " ${BlockStmtNodeTest.testExpression}"

                        yield(Arguments.of(text, hasProperties, hasParameters))
                    }
                }
            }

            return sequence.asStream()
        }


        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as ExpressionStmtNode

            IdentifierNodeTest.checkTestExpression(node.name)
            BlockStmtNodeTest.checkTestExpression(node.block)
            Assertions.assertNull(node.properties, "The properties property must be null")
            Assertions.assertNull(node.parameterList, "The parameterList property must be null")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct nodes`(text: String, hasProperties: Boolean, hasParameters: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = ExpressionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ExpressionStmtNode

        IdentifierNodeTest.checkTestExpression(res.name)
        BlockStmtNodeTest.checkTestExpression(res.block)

        if (hasProperties) {
            Assertions.assertNotNull(res.properties, "The properties property cannot be null")
            PropertyStyleObjectBlockNodeTest.checkTestExpression(res.properties!!)
        } else {
            Assertions.assertNull(res.properties, "The properties property must be null")
        }

        if (hasParameters) {
            Assertions.assertNotNull(res.parameterList, "The parameterList property cannot be null")
            FunctionParameterListNodeTest.checkTestExpression(res.parameterList!!)
        } else {
            Assertions.assertNull(res.parameterList, "The parameterList property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect node without block`() {
        TestUtils.assertParserException(AngmarParserExceptionType.ExpressionStatementWithoutBlock) {
            val text = "${ExpressionStmtNode.keyword} ${IdentifierNodeTest.testExpression}"
            val parser = LexemParser(IOStringReader.from(text))
            ExpressionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = ExpressionStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

