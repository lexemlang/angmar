package org.lexem.angmar.analyzer.nodes.functional.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*

internal class DestructuringSpreadStmtAnalyzerTest {
    @Test
    fun `test identifier`() {
        val identifier = "identifier"
        val text = "${DestructuringSpreadStmtNode.spreadToken}$identifier"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringSpreadStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Destructuring, LxmDestructuring())

        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Destructuring) as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertNull(destructuring.alias, "The alias property is incorrect")
        Assertions.assertEquals(0, elements.size, "The number of elements is incorrect")

        val spread = destructuring.spread!!
        Assertions.assertEquals(identifier, spread.alias, "The alias property is incorrect")
        Assertions.assertEquals(identifier, spread.original, "The original property is incorrect")
        Assertions.assertFalse(spread.isConstant, "The isConstant property is incorrect")

        // Remove Destructuring from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Destructuring)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test constant identifier`() {
        val identifier = "identifier"
        val text = "${DestructuringSpreadStmtNode.spreadToken}${DestructuringSpreadStmtNode.constantToken}$identifier"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = DestructuringSpreadStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Destructuring, LxmDestructuring())

        TestUtils.processAndCheckEmpty(analyzer)

        val destructuring = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Destructuring) as LxmDestructuring
        val elements = destructuring.getElements()

        Assertions.assertNull(destructuring.alias, "The alias property is incorrect")
        Assertions.assertEquals(0, elements.size, "The number of elements is incorrect")

        val spread = destructuring.spread!!
        Assertions.assertEquals(identifier, spread.alias, "The alias property is incorrect")
        Assertions.assertEquals(identifier, spread.original, "The original property is incorrect")
        Assertions.assertTrue(spread.isConstant, "The isConstant property is incorrect")

        // Remove Destructuring from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Destructuring)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
