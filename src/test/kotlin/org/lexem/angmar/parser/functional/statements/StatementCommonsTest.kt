package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.descriptive.statements.loops.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.loops.*
import java.util.stream.*
import kotlin.streams.*

internal class StatementCommonsTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testAnyStatement = FunctionalExpressionStmtNodeTest.testExpression
        const val testAnyDescriptiveStatement = OnBackBlockStmtNodeTest.testExpression
        const val testAnyPublicMacroStmt = VarDeclarationStmtNodeTest.testExpression
        const val testAnyControlStmt = ControlWithExpressionStmtNodeTest.testExpression

        val statements = listOf(BlockStmtNodeTest.testExpression, ConditionalStmtNodeTest.testExpression,
                SelectiveStmtNodeTest.testExpression, ConditionalLoopStmtNodeTest.testExpression,
                IteratorLoopStmtNodeTest.testExpression, InfiniteLoopStmtNodeTest.testExpression,
                testAnyPublicMacroStmt, PublicMacroStmtNodeTest.testExpression, testAnyControlStmt,
                FunctionalExpressionStmtNodeTest.testExpression)
        val descriptiveStatements =
                listOf(OnBackBlockStmtNodeTest.testExpression, QuantifiedLoopStmtNodeTest.testExpression,
                        SetPropsMacroStmtNodeTest.testExpression, testAnyStatement,
                        LexemePatternGroupNodeTest.testExpression, LexemePatternNodeTest.testExpression)

        @JvmStatic
        private fun provideStatements(): Stream<Arguments> {
            val sequence = sequence {
                for (test in statements.withIndex()) {
                    yield(Arguments.of(test.value, test.index))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideDescriptiveStatements(): Stream<Arguments> {
            val sequence = sequence {
                for (test in descriptiveStatements.withIndex()) {
                    yield(Arguments.of(test.value, test.index))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun providePublicMacroStatement(): Stream<Arguments> {
            val sequence = sequence {
                val tests = listOf(VarDeclarationStmtNodeTest.testExpression, FunctionStmtNodeTest.testExpression,
                        ExpressionStmtNodeTest.testExpression, FilterStmtNodeTest.testExpression)

                for (test in tests.withIndex()) {
                    yield(Arguments.of(test.value, test.index))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideControlStatement(): Stream<Arguments> {
            val sequence = sequence {
                val tests = listOf(ControlWithExpressionStmtNodeTest.testExpression,
                        ControlWithoutExpressionStmtNodeTest.testExpression)

                for (test in tests.withIndex()) {
                    yield(Arguments.of(test.value, test.index))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestAnyStatement(node: ParserNode) = FunctionalExpressionStmtNodeTest.checkTestExpression(node)
        fun checkTestAnyDescriptiveStatement(node: ParserNode) = OnBackBlockStmtNodeTest.checkTestExpression(node)
        fun checkTestAnyPublicMacroStmt(node: ParserNode) = VarDeclarationStmtNodeTest.checkTestExpression(node)
        fun checkTestAnyControlStmt(node: ParserNode) = ControlWithExpressionStmtNodeTest.checkTestExpression(node)
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideStatements")
    fun `parse any correct statement`(text: String, type: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = StatementCommons.parseAnyStatement(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> BlockStmtNodeTest.checkTestExpression(res)
            1 -> ConditionalStmtNodeTest.checkTestExpression(res)
            2 -> SelectiveStmtNodeTest.checkTestExpression(res)
            3 -> ConditionalLoopStmtNodeTest.checkTestExpression(res)
            4 -> IteratorLoopStmtNodeTest.checkTestExpression(res)
            5 -> InfiniteLoopStmtNodeTest.checkTestExpression(res)
            6 -> checkTestAnyPublicMacroStmt(res)
            7 -> PublicMacroStmtNodeTest.checkTestExpression(res)
            8 -> checkTestAnyControlStmt(res)
            9 -> FunctionalExpressionStmtNodeTest.checkTestExpression(res)
            else -> throw AngmarUnreachableException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideDescriptiveStatements")
    fun `parse any correct descriptive statement`(text: String, type: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        parser.isDescriptiveCode = true
        val res = StatementCommons.parseAnyDescriptiveStatement(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> OnBackBlockStmtNodeTest.checkTestExpression(res)
            1 -> QuantifiedLoopStmtNodeTest.checkTestExpression(res)
            2 -> SetPropsMacroStmtNodeTest.checkTestExpression(res)
            3 -> checkTestAnyStatement(res)
            4 -> LexemePatternGroupNodeTest.checkTestExpression(res)
            5 -> LexemePatternNodeTest.checkTestExpression(res)
            else -> throw AngmarUnreachableException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("providePublicMacroStatement")
    fun `parse any correct public macro statement`(text: String, type: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = StatementCommons.parseAnyPublicMacroStatement(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> VarDeclarationStmtNodeTest.checkTestExpression(res)
            1 -> FunctionStmtNodeTest.checkTestExpression(res)
            2 -> ExpressionStmtNodeTest.checkTestExpression(res)
            3 -> FilterStmtNodeTest.checkTestExpression(res)
            else -> throw AngmarUnreachableException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideControlStatement")
    fun `parse any correct control statement`(text: String, type: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = StatementCommons.parseAnyControlStatement(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode

        when (type) {
            0 -> ControlWithExpressionStmtNodeTest.checkTestExpression(res)
            1 -> ControlWithoutExpressionStmtNodeTest.checkTestExpression(res)
            else -> throw AngmarUnreachableException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }
}
