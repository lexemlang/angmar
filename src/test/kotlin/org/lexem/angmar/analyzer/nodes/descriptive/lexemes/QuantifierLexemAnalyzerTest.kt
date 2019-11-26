package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.utils.*

internal class QuantifierLexemAnalyzerTest {
    @Test
    fun `test greedy explicit quantifier`() {
        val minimum = 5
        val text = "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertFalse(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertFalse(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test lazy explicit quantifier`() {
        val minimum = 5
        val text =
                "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.endToken}${QuantifierLexemeNode.lazyAbbreviation}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertTrue(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertFalse(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test atomic greedy explicit quantifier`() {
        val minimum = 5
        val text =
                "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.endToken}${QuantifierLexemeNode.atomicGreedyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertFalse(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertTrue(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test atomic lazy explicit quantifier`() {
        val minimum = 5
        val text =
                "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.endToken}${QuantifierLexemeNode.atomicLazyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertTrue(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertTrue(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test greedy ? quantifier`() {
        val minimum = 0
        val maximum = 1
        val text = QuantifierLexemeNode.lazyAbbreviation
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(maximum, result.max, "The max property is incorrect")
        Assertions.assertFalse(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertFalse(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test lazy ? quantifier`() {
        val minimum = 0
        val maximum = 1
        val text = "${QuantifierLexemeNode.lazyAbbreviation}${QuantifierLexemeNode.lazyAbbreviation}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(maximum, result.max, "The max property is incorrect")
        Assertions.assertTrue(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertFalse(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test atomic greedy ? quantifier`() {
        val minimum = 0
        val maximum = 1
        val text = "${QuantifierLexemeNode.lazyAbbreviation}${QuantifierLexemeNode.atomicGreedyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(maximum, result.max, "The max property is incorrect")
        Assertions.assertFalse(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertTrue(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test atomic lazy ? quantifier`() {
        val minimum = 0
        val maximum = 1
        val text = "${QuantifierLexemeNode.lazyAbbreviation}${QuantifierLexemeNode.atomicLazyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(maximum, result.max, "The max property is incorrect")
        Assertions.assertTrue(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertTrue(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test greedy * quantifier`() {
        val minimum = 0
        val text = QuantifierLexemeNode.atomicLazyAbbreviations
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertTrue(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertFalse(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertFalse(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test lazy * quantifier`() {
        val minimum = 0
        val text = "${QuantifierLexemeNode.atomicLazyAbbreviations}${QuantifierLexemeNode.lazyAbbreviation}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertTrue(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertTrue(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertFalse(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test atomic greedy * quantifier`() {
        val minimum = 0
        val text = "${QuantifierLexemeNode.atomicLazyAbbreviations}${QuantifierLexemeNode.atomicGreedyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertTrue(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertFalse(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertTrue(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test atomic lazy * quantifier`() {
        val minimum = 0
        val text = "${QuantifierLexemeNode.atomicLazyAbbreviations}${QuantifierLexemeNode.atomicLazyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertTrue(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertTrue(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertTrue(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test greedy + quantifier`() {
        val minimum = 1
        val text = QuantifierLexemeNode.atomicGreedyAbbreviations
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertTrue(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertFalse(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertFalse(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test lazy + quantifier`() {
        val minimum = 1
        val text = "${QuantifierLexemeNode.atomicGreedyAbbreviations}${QuantifierLexemeNode.lazyAbbreviation}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertTrue(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertTrue(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertFalse(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test atomic greedy + quantifier`() {
        val minimum = 1
        val text = "${QuantifierLexemeNode.atomicGreedyAbbreviations}${QuantifierLexemeNode.atomicGreedyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertTrue(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertFalse(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertTrue(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test atomic lazy + quantifier`() {
        val minimum = 1
        val text = "${QuantifierLexemeNode.atomicGreedyAbbreviations}${QuantifierLexemeNode.atomicLazyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifierLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertTrue(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(minimum, result.max, "The max property is incorrect")
        Assertions.assertTrue(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertTrue(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
