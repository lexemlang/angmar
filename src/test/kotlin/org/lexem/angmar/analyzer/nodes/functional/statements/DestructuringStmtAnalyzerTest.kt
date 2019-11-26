package org.lexem.angmar.analyzer.nodes.functional.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*

internal class DestructuringStmtAnalyzerTest {
    @Test
    fun `test alias`() {
        val alias = "identifier"
        val text =
                "$alias ${DestructuringStmtNode.elementSeparator} ${DestructuringStmtNode.startToken} ${DestructuringStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.getLastFromStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertEquals(alias, destructuring.alias, "The alias property is incorrect")
        Assertions.assertNull(destructuring.spread, "The spread property is incorrect")
        Assertions.assertEquals(0, elements.size, "The number of elements is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test elements`() {
        val elementOriginal = "elementOriginal"
        val elementAlias = "elementAlias"
        val text =
                "${DestructuringStmtNode.startToken} $elementOriginal ${DestructuringStmtNode.elementSeparator} $elementOriginal ${DestructuringElementStmtNode.aliasToken} $elementAlias ${DestructuringStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.getLastFromStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertNull(destructuring.alias, "The alias property is incorrect")
        Assertions.assertEquals(2, elements.size, "The number of elements is incorrect")
        Assertions.assertNull(destructuring.spread, "The spread property is incorrect")

        val firstElement = elements[0]
        Assertions.assertEquals(elementOriginal, firstElement.alias, "The alias property is incorrect")
        Assertions.assertEquals(elementOriginal, firstElement.original, "The original property is incorrect")
        Assertions.assertFalse(firstElement.isConstant, "The isConstant property is incorrect")

        val secondElement = elements[1]
        Assertions.assertEquals(elementAlias, secondElement.alias, "The alias property is incorrect")
        Assertions.assertEquals(elementOriginal, secondElement.original, "The original property is incorrect")
        Assertions.assertFalse(secondElement.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test spread`() {
        val spreadAlias = "spreadAlias"
        val text =
                "${DestructuringStmtNode.startToken} ${DestructuringSpreadStmtNode.spreadToken}$spreadAlias ${DestructuringStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.getLastFromStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertNull(destructuring.alias, "The alias property is incorrect")
        Assertions.assertEquals(0, elements.size, "The number of elements is incorrect")

        val spread = destructuring.spread!!
        Assertions.assertEquals(spreadAlias, spread.alias, "The alias property is incorrect")
        Assertions.assertEquals(spreadAlias, spread.original, "The original property is incorrect")
        Assertions.assertFalse(spread.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test alias-elements`() {
        val alias = "identifier"
        val elementOriginal = "elementOriginal"
        val elementAlias = "elementAlias"
        val text =
                "$alias ${DestructuringStmtNode.elementSeparator} ${DestructuringStmtNode.startToken} $elementOriginal ${DestructuringStmtNode.elementSeparator} $elementOriginal ${DestructuringElementStmtNode.aliasToken} $elementAlias ${DestructuringStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.getLastFromStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertEquals(alias, destructuring.alias, "The alias property is incorrect")
        Assertions.assertEquals(2, elements.size, "The number of elements is incorrect")
        Assertions.assertNull(destructuring.spread, "The spread property is incorrect")

        val firstElement = elements[0]
        Assertions.assertEquals(elementOriginal, firstElement.alias, "The alias property is incorrect")
        Assertions.assertEquals(elementOriginal, firstElement.original, "The original property is incorrect")
        Assertions.assertFalse(firstElement.isConstant, "The isConstant property is incorrect")

        val secondElement = elements[1]
        Assertions.assertEquals(elementAlias, secondElement.alias, "The alias property is incorrect")
        Assertions.assertEquals(elementOriginal, secondElement.original, "The original property is incorrect")
        Assertions.assertFalse(secondElement.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test alias-spread`() {
        val alias = "identifier"
        val spreadAlias = "spreadAlias"
        val text =
                "$alias ${DestructuringStmtNode.elementSeparator} ${DestructuringStmtNode.startToken} ${DestructuringSpreadStmtNode.spreadToken}$spreadAlias ${DestructuringStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.getLastFromStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertEquals(alias, destructuring.alias, "The alias property is incorrect")
        Assertions.assertEquals(0, elements.size, "The number of elements is incorrect")

        val spread = destructuring.spread!!
        Assertions.assertEquals(spreadAlias, spread.alias, "The alias property is incorrect")
        Assertions.assertEquals(spreadAlias, spread.original, "The original property is incorrect")
        Assertions.assertFalse(spread.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test elements-spread`() {
        val elementOriginal = "elementOriginal"
        val elementAlias = "elementAlias"
        val spreadAlias = "spreadAlias"
        val text =
                "${DestructuringStmtNode.startToken} $elementOriginal ${DestructuringStmtNode.elementSeparator} $elementOriginal ${DestructuringElementStmtNode.aliasToken} $elementAlias ${DestructuringStmtNode.elementSeparator} ${DestructuringSpreadStmtNode.spreadToken}$spreadAlias ${DestructuringStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.getLastFromStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertNull(destructuring.alias, "The alias property is incorrect")
        Assertions.assertEquals(2, elements.size, "The number of elements is incorrect")

        val firstElement = elements[0]
        Assertions.assertEquals(elementOriginal, firstElement.alias, "The alias property is incorrect")
        Assertions.assertEquals(elementOriginal, firstElement.original, "The original property is incorrect")
        Assertions.assertFalse(firstElement.isConstant, "The isConstant property is incorrect")

        val secondElement = elements[1]
        Assertions.assertEquals(elementAlias, secondElement.alias, "The alias property is incorrect")
        Assertions.assertEquals(elementOriginal, secondElement.original, "The original property is incorrect")
        Assertions.assertFalse(secondElement.isConstant, "The isConstant property is incorrect")

        val spread = destructuring.spread!!
        Assertions.assertEquals(spreadAlias, spread.alias, "The alias property is incorrect")
        Assertions.assertEquals(spreadAlias, spread.original, "The original property is incorrect")
        Assertions.assertFalse(spread.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test alias-elements-spread`() {
        val alias = "identifier"
        val elementOriginal = "elementOriginal"
        val elementAlias = "elementAlias"
        val spreadAlias = "spreadAlias"
        val text =
                "$alias ${DestructuringStmtNode.elementSeparator} ${DestructuringStmtNode.startToken} $elementOriginal ${DestructuringStmtNode.elementSeparator} $elementOriginal ${DestructuringElementStmtNode.aliasToken} $elementAlias ${DestructuringStmtNode.elementSeparator} ${DestructuringSpreadStmtNode.spreadToken}$spreadAlias ${DestructuringStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.getLastFromStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertEquals(alias, destructuring.alias, "The alias property is incorrect")
        Assertions.assertEquals(2, elements.size, "The number of elements is incorrect")

        val firstElement = elements[0]
        Assertions.assertEquals(elementOriginal, firstElement.alias, "The alias property is incorrect")
        Assertions.assertEquals(elementOriginal, firstElement.original, "The original property is incorrect")
        Assertions.assertFalse(firstElement.isConstant, "The isConstant property is incorrect")

        val secondElement = elements[1]
        Assertions.assertEquals(elementAlias, secondElement.alias, "The alias property is incorrect")
        Assertions.assertEquals(elementOriginal, secondElement.original, "The original property is incorrect")
        Assertions.assertFalse(secondElement.isConstant, "The isConstant property is incorrect")

        val spread = destructuring.spread!!
        Assertions.assertEquals(spreadAlias, spread.alias, "The alias property is incorrect")
        Assertions.assertEquals(spreadAlias, spread.original, "The original property is incorrect")
        Assertions.assertFalse(spread.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
