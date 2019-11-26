package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ObjectElementAnalyzerTest {
    @Test
    fun `test normal`() {
        val value = 123
        val key = "test"
        val text = "$key${ObjectElementNode.keyValueSeparator}$value"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ObjectElementNode.Companion::parse)

        // Prepare stack.
        val obj = LxmObject()
        val objRef = analyzer.memory.add(obj)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, objRef)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmReference ?: throw Error(
                        "The result must be a LxmReference")
        val objDeref =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")
        val property = objDeref.getOwnPropertyDescriptor(analyzer.memory, key)!!
        val element = property.value as? LxmInteger ?: throw Error("The element must be a LxmInteger")

        Assertions.assertEquals(value, element.primitive, "The primitive property is incorrect")
        Assertions.assertFalse(property.isConstant, "The isConstant property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test constant`() {
        val value = 123
        val key = "test"
        val text = "${ObjectElementNode.constantToken}$key${ObjectElementNode.keyValueSeparator}$value"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ObjectElementNode.Companion::parse)

        // Prepare stack.
        val obj = LxmObject()
        val objRef = analyzer.memory.add(obj)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, objRef)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmReference ?: throw Error(
                        "The result must be a LxmReference")
        val objDeref =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")
        val property = objDeref.getOwnPropertyDescriptor(analyzer.memory, key)!!
        val element = property.value as? LxmInteger ?: throw Error("The element must be a LxmInteger")

        Assertions.assertEquals(value, element.primitive, "The primitive property is incorrect")
        Assertions.assertTrue(property.isConstant, "The isConstant property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
