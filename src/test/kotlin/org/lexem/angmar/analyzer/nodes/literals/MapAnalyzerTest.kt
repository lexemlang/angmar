package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class MapAnalyzerTest {
    private val values = mapOf(1 to 100, 2 to 200, 3 to 300, 4 to 400)
    private val valuesText = values.asSequence().joinToString(MapNode.elementSeparator) {
        "${it.key}${MapElementNode.keyValueSeparator}${it.value}"
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `test normal`() {
        val text = "${MapNode.macroName}${MapNode.startToken}$valuesText${MapNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = MapNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getLastFromStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val obj = resultRef.dereferenceAs<LxmMap>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmMap")

        Assertions.assertEquals(values.size, obj.size, "The number of properties is incorrect")

        for ((index, value) in values) {
            val resValue = obj.getPropertyValue(LxmInteger.from(index)) as? LxmInteger ?: throw Error(
                    "The property must be a LxmInteger")
            Assertions.assertEquals(value, resValue.primitive, "The primitive property is incorrect")
        }

        Assertions.assertFalse(obj.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test constant`() {
        val text = "${MapNode.macroName}${MapNode.constantToken}${MapNode.startToken}$valuesText${MapNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = MapNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getLastFromStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val obj = resultRef.dereferenceAs<LxmMap>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmMap")

        Assertions.assertEquals(values.size, obj.size, "The number of properties is incorrect")

        for ((index, value) in values) {
            val resValue = obj.getPropertyValue(LxmInteger.from(index)) as? LxmInteger ?: throw Error(
                    "The property must be a LxmInteger")
            Assertions.assertEquals(value, resValue.primitive, "The primitive property is incorrect")
        }

        Assertions.assertTrue(obj.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty normal`() {
        val text = "${MapNode.macroName}${MapNode.startToken}${MapNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = MapNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getLastFromStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val obj = resultRef.dereferenceAs<LxmMap>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmMap")

        Assertions.assertEquals(0, obj.size, "The number of properties is incorrect")
        Assertions.assertFalse(obj.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test empty constant`() {
        val text = "${MapNode.macroName}${MapNode.constantToken}${MapNode.startToken}${MapNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = MapNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getLastFromStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val obj = resultRef.dereferenceAs<LxmMap>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmMap")

        Assertions.assertEquals(0, obj.size, "The number of properties is incorrect")
        Assertions.assertTrue(obj.isConstant, "The isConstant property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
