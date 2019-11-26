package org.lexem.angmar.analyzer.nodes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.utils.*

internal class LexemFileAnalyzerTest {
    @Test
    fun `test empty`() {
        val text = ""
        val analyzer = TestUtils.createAnalyzerFrom(text) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        TestUtils.processAndCheckEmpty(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filled`() {
        val varName = "test"
        val value = LxmInteger.Num10
        val text = "$varName ${AssignOperatorNode.assignOperator} $value"
        val analyzer = TestUtils.createAnalyzerFrom(text) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(value, result, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without expression`(keyword: String) {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.UnhandledControlStatementSignal) {
            val text = keyword
            val analyzer = TestUtils.createAnalyzerFrom(text) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag and without expression`(keyword: String) {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.UnhandledControlStatementSignal) {
            val tagName = "tag"
            val text = "$keyword${BlockStmtNode.tagPrefix}$tagName"
            val analyzer = TestUtils.createAnalyzerFrom(text) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test control signals with expression`(keyword: String) {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.UnhandledControlStatementSignal) {
            val value = LxmInteger.Num10
            val text = "$keyword $value"
            val analyzer = TestUtils.createAnalyzerFrom(text) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
