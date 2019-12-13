package org.lexem.angmar.parser

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.statements.*
import java.util.stream.*
import kotlin.streams.*

internal class GlobalCommonsTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testLexeme = AnyLexemeNodeTest.testExpression
        const val testFilterLexeme = AnyLexemeNodeTest.testFilterExpression

        @JvmStatic
        private fun provideBlocks(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of(ConditionalStmtNodeTest.testExpression, 0, false))
                yield(Arguments.of(OnBackBlockStmtNodeTest.testExpression, 1, true))
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideLexemes(): Stream<Arguments> {
            val sequence = sequence {
                yield(Arguments.of(AnyLexemeNodeTest.testExpression, 0, false))
                yield(Arguments.of(AnyFilterLexemeNodeTest.testExpression, 1, true))
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestLexeme(node: ParserNode) = AnyLexemeNodeTest.checkTestExpression(node)
        fun checkTestFilterLexeme(node: ParserNode) = AnyLexemeNodeTest.checkTestFilterExpression(node)
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideBlocks")
    fun `parse correct block statement`(text: String, type: Int, isDescriptiveCode: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = isDescriptiveCode
        val res = GlobalCommons.parseBlockStatement(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> ConditionalStmtNodeTest.checkTestExpression(res)
            1 -> OnBackBlockStmtNodeTest.checkTestExpression(res)
            else -> throw AngmarUnreachableException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideLexemes")
    fun `parse correct lexeme`(text: String, type: Int, isFilterCode: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        parser.isFilterCode = isFilterCode
        val res = GlobalCommons.parseLexem(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> AnyLexemeNodeTest.checkTestExpression(res)
            1 -> AnyFilterLexemeNodeTest.checkTestExpression(res)
            else -> throw AngmarUnreachableException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }
}
