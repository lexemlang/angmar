package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class SetAnalyzerTest {
    val values = setOf(1, 2, 3, 4)
    val valuesText = values.joinToString(ListNode.elementSeparator)

    // TESTS ------------------------------------------------------------------

    @Test
    fun `test normal`() {
        val text = "${SetNode.macroName}${ListNode.startToken}$valuesText${ListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SetNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val set = resultRef.dereferenceAs<LxmSet>(analyzer.memory) ?: throw Error("The result must be a LxmSet")
        val allValues = set.getAllValues()

        Assertions.assertEquals(values.size, values.size, "The number of values is incorrect")

        for (i in allValues) {
            for (j in i.value) {
                val value = j.value as? LxmInteger ?: throw Error("The property must be a LxmInteger")
                Assertions.assertTrue(value.primitive in values, "The primitive property is incorrect")
            }
        }

        Assertions.assertFalse(set.isImmutable, "The isImmutable property is incorrect")

        // Remove the set.
        resultRef.decreaseReferenceCount(analyzer.memory)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test constant`() {
        val text = "${SetNode.macroName}${ListNode.constantToken}${ListNode.startToken}$valuesText${ListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SetNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val set = resultRef.dereferenceAs<LxmSet>(analyzer.memory) ?: throw Error("The result must be a LxmSet")
        val allValues = set.getAllValues()

        Assertions.assertEquals(values.size, values.size, "The number of values is incorrect")

        for (i in allValues) {
            for (j in i.value) {
                val value = j.value as? LxmInteger ?: throw Error("The property must be a LxmInteger")
                Assertions.assertTrue(value.primitive in values, "The primitive property is incorrect")
            }
        }

        Assertions.assertTrue(set.isImmutable, "The isImmutable property is incorrect")

        // Remove the set.
        resultRef.decreaseReferenceCount(analyzer.memory)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty normal`() {
        val text = "${SetNode.macroName}${ListNode.startToken}${ListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SetNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val set = resultRef.dereferenceAs<LxmSet>(analyzer.memory) ?: throw Error("The result must be a LxmSet")
        val values = set.getAllValues()

        Assertions.assertEquals(0, values.size, "The number of cells is incorrect")
        Assertions.assertFalse(set.isImmutable, "The isImmutable property is incorrect")

        // Remove the set.
        resultRef.decreaseReferenceCount(analyzer.memory)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty constant`() {
        val text = "${SetNode.macroName}${ListNode.constantToken}${ListNode.startToken}${ListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SetNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val set = resultRef.dereferenceAs<LxmSet>(analyzer.memory) ?: throw Error("The result must be a LxmSet")
        val values = set.getAllValues()

        Assertions.assertEquals(0, values.size, "The number of cells is incorrect")
        Assertions.assertTrue(set.isImmutable, "The isImmutable property is incorrect")

        // Remove the set.
        resultRef.decreaseReferenceCount(analyzer.memory)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
