package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.utils.*

internal class ExplicitQuantifierLexemeAnalyzerTest {
    @Test
    fun `test quantifier with only minimum`() {
        val minimum = 5
        val text = "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = ExplicitQuantifierLexemeNode.Companion::parse)
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
    fun `test quantifier with minimum and maximum`() {
        val minimum = 5
        val maximum = 10
        val text =
                "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.elementSeparator}$maximum${ExplicitQuantifierLexemeNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = ExplicitQuantifierLexemeNode.Companion::parse)
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
    fun `test quantifier to infinite`() {
        val minimum = 5
        val text =
                "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.elementSeparator}${ExplicitQuantifierLexemeNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = ExplicitQuantifierLexemeNode.Companion::parse)
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
    @Incorrect
    fun `test quantifier with non integer minimum`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val minimum = LxmLogic.False
            val text = "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = ExplicitQuantifierLexemeNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test quantifier with minimum lower than zero`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectQuantifierBounds) {
            val minimum = -1
            val text = "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = ExplicitQuantifierLexemeNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test quantifier with non integer maximum`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val minimum = 5
            val maximum = LxmNil
            val text =
                    "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.elementSeparator}$maximum${ExplicitQuantifierLexemeNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = ExplicitQuantifierLexemeNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test quantifier with maximum lower than minimum`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectQuantifierBounds) {
            val minimum = 5
            val maximum = 1
            val text =
                    "${ExplicitQuantifierLexemeNode.startToken}$minimum${ExplicitQuantifierLexemeNode.elementSeparator}$maximum${ExplicitQuantifierLexemeNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = ExplicitQuantifierLexemeNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
