package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.utils.*

internal class QuantifiedGroupModifierAnalyzerTest {
    @Test
    fun `test quantifier with nothing`() {
        val text = "${QuantifiedGroupModifierNode.startToken}${QuantifiedGroupModifierNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(-1, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(-1, result.max, "The max property is incorrect")
        Assertions.assertFalse(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertFalse(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test quantifier with only minimum`() {
        val minimum = 5
        val text = "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
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
                "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.elementSeparator}$maximum${QuantifiedGroupModifierNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
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
    fun `test quantifier without maximum`() {
        val minimum = 5
        val text =
                "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.elementSeparator}${QuantifiedGroupModifierNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(minimum, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(-1, result.max, "The max property is incorrect")
        Assertions.assertFalse(result.isLazy, "The isLazy property is incorrect")
        Assertions.assertFalse(result.isAtomic, "The isAtomic property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test quantifier without minimum`() {
        val maximum = 5
        val text =
                "${QuantifiedGroupModifierNode.startToken}${QuantifiedGroupModifierNode.elementSeparator}$maximum${QuantifiedGroupModifierNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmQuantifier ?: throw Error(
                "The result must be a LxmQuantifier")
        Assertions.assertEquals(-1, result.min, "The min property is incorrect")
        Assertions.assertFalse(result.isInfinite, "The isInfinite property is incorrect")
        Assertions.assertEquals(maximum, result.max, "The max property is incorrect")
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
            val text = "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test quantifier with minimum lower than zero`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectQuantifierBounds) {
            val minimum = -1
            val text = "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
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
                    "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.elementSeparator}$maximum${QuantifiedGroupModifierNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
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
                    "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.elementSeparator}$maximum${QuantifiedGroupModifierNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
