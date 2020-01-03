package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ListAnalyzerTest {
    val values = listOf(1, 2, 3, 4)
    val valuesText = values.joinToString(ListNode.elementSeparator)

    // TESTS ------------------------------------------------------------------

    @Test
    fun `test normal`() {
        val text = "${ListNode.startToken}$valuesText${ListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ListNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getLastFromStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val list = resultRef.dereferenceAs<LxmList>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmList")

        Assertions.assertEquals(values.size, list.size, "The number of cells is incorrect")

        for ((index, i) in list.getAllCells().withIndex()) {
            val value = i as? LxmInteger ?: throw Error("The property must be a LxmInteger")
            Assertions.assertEquals(values[index], value.primitive, "The primitive property is incorrect")
        }

        Assertions.assertFalse(list.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test constant`() {
        val text = "${ListNode.constantToken}${ListNode.startToken}$valuesText${ListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ListNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getLastFromStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val list = resultRef.dereferenceAs<LxmList>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmList")

        Assertions.assertEquals(values.size, list.size, "The number of cells is incorrect")

        for ((index, i) in list.getAllCells().withIndex()) {
            val value = i as? LxmInteger ?: throw Error("The property must be a LxmInteger")
            Assertions.assertEquals(values[index], value.primitive, "The primitive property is incorrect")
        }

        Assertions.assertTrue(list.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty normal`() {
        val text = "${ListNode.startToken}${ListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ListNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getLastFromStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val list = resultRef.dereferenceAs<LxmList>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmList")

        Assertions.assertEquals(0, list.size, "The number of cells is incorrect")
        Assertions.assertFalse(list.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty constant`() {
        val text = "${ListNode.constantToken}${ListNode.startToken}${ListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ListNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getLastFromStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val list = resultRef.dereferenceAs<LxmList>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmList")

        Assertions.assertEquals(0, list.size, "The number of cells is incorrect")
        Assertions.assertTrue(list.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
