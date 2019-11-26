package org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class AbsoluteAnchorLexemeAnalyzerTest {
    @Test
    fun `test one match`() {
        val text = "this is a test"
        val initialPosition = 3
        val anchor1 =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart.identifier}${IndexerNode.startToken}${initialPosition + 2}${IndexerNode.endToken}"
        val anchor2 =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart.identifier}${IndexerNode.startToken}$initialPosition${IndexerNode.endToken}"
        val grammar = "${AbsoluteAnchorLexemeNode.startToken} $anchor1 $anchor2 ${AbsoluteAnchorLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AbsoluteAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(initialPosition)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test no one match`() {
        val text = "this is a test"
        val initialPosition = 3
        val anchor1 =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart.identifier}${IndexerNode.startToken}${initialPosition + 2}${IndexerNode.endToken}"
        val anchor2 =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart.identifier}${IndexerNode.startToken}${initialPosition - 2}${IndexerNode.endToken}"
        val grammar = "${AbsoluteAnchorLexemeNode.startToken} $anchor1 $anchor2 ${AbsoluteAnchorLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AbsoluteAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(initialPosition)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negative with one match`() {
        val text = "this is a test"
        val initialPosition = 3
        val anchor1 =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart.identifier}${IndexerNode.startToken}${initialPosition + 2}${IndexerNode.endToken}"
        val anchor2 =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart.identifier}${IndexerNode.startToken}$initialPosition${IndexerNode.endToken}"
        val grammar =
                "${AbsoluteAnchorLexemeNode.notOperator}${AbsoluteAnchorLexemeNode.startToken} $anchor1 $anchor2 ${AbsoluteAnchorLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AbsoluteAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(initialPosition)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negative with no one match`() {
        val text = "this is a test"
        val initialPosition = 3
        val anchor1 =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart.identifier}${IndexerNode.startToken}${initialPosition + 2}${IndexerNode.endToken}"
        val anchor2 =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart.identifier}${IndexerNode.startToken}${initialPosition - 2}${IndexerNode.endToken}"
        val grammar =
                "${AbsoluteAnchorLexemeNode.notOperator}${AbsoluteAnchorLexemeNode.startToken} $anchor1 $anchor2 ${AbsoluteAnchorLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AbsoluteAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(initialPosition)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
