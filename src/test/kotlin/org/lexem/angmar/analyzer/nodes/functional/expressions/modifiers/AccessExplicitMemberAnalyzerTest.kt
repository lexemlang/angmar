package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.primitives.setters.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class AccessExplicitMemberAnalyzerTest {
    @Test
    fun `test getter`() {
        val varName = "test"
        val propName = "num"
        val text = "$varName${AccessExplicitMemberNode.accessToken}$propName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AccessExpressionNode.Companion::parse)

        // Prepare context.
        val value = LxmInteger.from(5)
        val obj = LxmObject(analyzer.memory)
        obj.setProperty(analyzer.memory, propName, value)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val objReference = analyzer.memory.add(obj)
        context.setProperty(analyzer.memory, varName, objReference)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmPropertySetter ?: throw Error(
                "The result must be a LxmPropertySetter")
        Assertions.assertEquals(objReference, result.obj, "The obj property is incorrect")
        Assertions.assertEquals(propName, result.property, "The property property is incorrect")
        Assertions.assertEquals(value, result.dereference(analyzer.memory, toWrite = false),
                "The value property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test setter`() {
        val varName = "test"
        val propName = "num"
        val right = 77
        val text =
                "$varName${AccessExplicitMemberNode.accessToken}$propName ${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val value = LxmInteger.from(5)
        val obj = LxmObject(analyzer.memory)
        obj.setProperty(analyzer.memory, propName, value)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val objReference = analyzer.memory.add(obj)
        context.setProperty(analyzer.memory, varName, objReference)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(right, result.primitive, "The element property is incorrect")

        val finalObject = objReference.dereference(analyzer.memory, toWrite = false) as? LxmObject ?: throw Error(
                "The result must be a LxmObject")
        val property = finalObject.getDereferencedProperty<LxmInteger>(analyzer.memory, propName, toWrite = false)
                ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(right, property.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }
}
