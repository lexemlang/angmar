package org.lexem.angmar.analyzer.nodes.functional.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*

internal class DestructuringElementStmtAnalyzerTest {
    @Test
    fun `test original`() {
        val original = "original"
        val text = original
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringElementStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmDestructuring())

        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.popStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertNull(destructuring.alias, "The alias property is incorrect")
        Assertions.assertNull(destructuring.spread, "The alias property is incorrect")
        Assertions.assertEquals(1, elements.size, "The number of elements is incorrect")

        val element = elements[0]
        Assertions.assertEquals(original, element.alias, "The alias property is incorrect")
        Assertions.assertEquals(original, element.original, "The original property is incorrect")
        Assertions.assertFalse(element.isConstant, "The isConstant property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test original-alias`() {
        val original = "original"
        val alias = "alias"
        val text = "$original ${DestructuringElementStmtNode.aliasToken} $alias"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringElementStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmDestructuring())

        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.popStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertNull(destructuring.alias, "The alias property is incorrect")
        Assertions.assertNull(destructuring.spread, "The alias property is incorrect")
        Assertions.assertEquals(1, elements.size, "The number of elements is incorrect")

        val element = elements[0]
        Assertions.assertEquals(alias, element.alias, "The alias property is incorrect")
        Assertions.assertEquals(original, element.original, "The original property is incorrect")
        Assertions.assertFalse(element.isConstant, "The isConstant property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test constant original`() {
        val original = "original"
        val text = "${DestructuringElementStmtNode.constantToken}$original"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringElementStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmDestructuring())

        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.popStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertNull(destructuring.alias, "The alias property is incorrect")
        Assertions.assertNull(destructuring.spread, "The alias property is incorrect")
        Assertions.assertEquals(1, elements.size, "The number of elements is incorrect")

        val element = elements[0]
        Assertions.assertEquals(original, element.alias, "The alias property is incorrect")
        Assertions.assertEquals(original, element.original, "The original property is incorrect")
        Assertions.assertTrue(element.isConstant, "The isConstant property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test constant original-alias`() {
        val original = "original"
        val alias = "alias"
        val text =
                "$original ${DestructuringElementStmtNode.aliasToken} ${DestructuringElementStmtNode.constantToken}$alias"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringElementStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmDestructuring())

        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.popStack() as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertNull(destructuring.alias, "The alias property is incorrect")
        Assertions.assertNull(destructuring.spread, "The alias property is incorrect")
        Assertions.assertEquals(1, elements.size, "The number of elements is incorrect")

        val element = elements[0]
        Assertions.assertEquals(alias, element.alias, "The alias property is incorrect")
        Assertions.assertEquals(original, element.original, "The original property is incorrect")
        Assertions.assertTrue(element.isConstant, "The isConstant property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
