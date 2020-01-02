package org.lexem.angmar.analyzer.nodes.descriptive

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class LexemePatternContentAnalyzerTest {
    @Test
    fun `test two lexemes`() {
        val textA = "this is"
        val textB = " a test"
        val text = "$textA$textB"
        val grammar =
                "${StringNode.startToken}$textA${StringNode.endToken} ${StringNode.startToken}$textB${StringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternContentNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without tag`(keyword: String) {
        val grammar = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternContentNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag`(keyword: String) {
        val tagName = "tag"
        val blockExpression = "$keyword${BlockStmtNode.tagPrefix}$tagName"
        val grammar = "${BlockStmtNode.startToken} $blockExpression ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternContentNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagName, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test return control signal`(keyword: String) {
        val value = LxmInteger.Num10
        val blockExpression = "$keyword $value"
        val grammar = "${BlockStmtNode.startToken} $blockExpression ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternContentNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
