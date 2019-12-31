package org.lexem.angmar.compiler.descriptive

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.utils.*

internal class LexemePatternCompiledTest {
    @Test
    fun `test matching optional pattern`() {
        val text = ""
        val grammar = LexemePatternAnalyzerTest.generatePattern(text, LexemePatternNode.Companion.PatternType.Optional)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 1)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The pattern has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test not-matching optional pattern`() {
        val text = ""
        val grammar = LexemePatternAnalyzerTest.generateNegativePattern(text,
                LexemePatternNode.Companion.PatternType.Optional)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test matching negative pattern`() {
        val text = ""
        val grammar = LexemePatternAnalyzerTest.generatePattern(text, LexemePatternNode.Companion.PatternType.Negative)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test not-matching negative pattern`() {
        val text = ""
        val grammar = LexemePatternAnalyzerTest.generateNegativePattern(text,
                LexemePatternNode.Companion.PatternType.Negative)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 1)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The pattern has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
