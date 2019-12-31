package org.lexem.angmar.compiler.descriptive

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.utils.*

internal class LexemePatternGroupCompiledTest {
    @Test
    fun `test quantifier {0}`() {
        val text = "c"
        val grammar = LexemePatternGroupAnalyzerTest.generateQuantifiedPatterns(listOf("a", "b", text), 0)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The patterns have not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test minimum greater than patterns`() {
        val text = "c"
        val grammar = LexemePatternGroupAnalyzerTest.generateQuantifiedPatterns(listOf("a", "b", text), 4)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The patterns have not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test simplify infinity`() {
        val text = "c"
        val grammar =
                LexemePatternGroupAnalyzerTest.generateQuantifiedPatterns(listOf("a", "b", text), 1, isInfinite = true)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The patterns have not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test simplify noops`() {
        val variableName = "testVariable"
        val text = "c"
        val grammar = LexemePatternGroupAnalyzerTest.generateQuantifiedPatterns(listOf("", "", text), variableName)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)
        context.setPropertyAsContext(variableName, LxmInteger.Num1)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The patterns have not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName, AnalyzerCommons.Identifiers.Node))
    }
}
