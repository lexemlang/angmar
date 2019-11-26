package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.utils.*

internal class PropertyBlockSelectorAnalyzerTest {
    @Test
    fun `test addition`() {
        val value = LxmInteger.Num10
        val grammar = "${PropertyBlockSelectorNode.startToken}${value.primitive}${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyBlockSelectorNode.Companion::parseForAddition)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmInteger.Num10, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - without alias`() {
        val value = LxmInteger.Num10
        val grammar =
                "${PropertyBlockSelectorNode.startToken}${AnalyzerCommons.Identifiers.DefaultPropertyName}${PropertyBlockSelectorNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = PropertyBlockSelectorNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Property, value)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmInteger.Num10, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Property and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Property)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - with alias`() {
        val value = LxmInteger.Num10
        val alias = "alias"
        val grammar =
                "${PropertyBlockSelectorNode.startToken}$alias ${PropertyBlockSelectorNode.relationalToken} $alias${PropertyBlockSelectorNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = PropertyBlockSelectorNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Property, value)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmInteger.Num10, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Property and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Property)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
