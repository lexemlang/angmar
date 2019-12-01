package org.lexem.angmar.analyzer.stdlib.types

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ObjectTypeTest {
    @Test
    fun `test newFrom`() {
        val propName = "a"
        val value = LxmInteger.Num1
        val fnCallArguments =
                "${ObjectNode.startToken}$propName${MapElementNode.keyValueSeparator}$value${ObjectNode.endToken}"
        val fnCall = "${ObjectType.TypeName}${AccessExplicitMemberNode.accessToken}${ObjectType.NewFrom}"
        val grammar = "$fnCall${FunctionCallNode.startToken}$fnCallArguments${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            val obj =
                    result?.dereference(analyzer.memory) as? LxmObject ?: throw Error("The result must be a LxmObject")

            Assertions.assertEquals(0, obj.getAllIterableProperties().size, "The size of the result is incorrect")

            val prototype = obj.getPrototypeAsObject(analyzer.memory)
            Assertions.assertEquals(value, prototype.getPropertyDescriptor(analyzer.memory, propName)!!.value,
                    "The prototype is incorrect")
        }
    }

    @Test
    fun `test assign`() {
        val target = mapOf("a" to LxmInteger.Num1, "b" to LxmInteger.Num2)
        val source1 = mapOf("b" to LxmInteger.Num0, "c" to LxmInteger.Num10)
        val source2 = mapOf("c" to LxmLogic.True, "d" to LxmLogic.False)
        val fnCallArguments = listOf(target, source1, source2)
        val fnCall = "${ObjectType.TypeName}${AccessExplicitMemberNode.accessToken}${ObjectType.Assign}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator) {
            "${ObjectNode.startToken}${it.entries.joinToString(ObjectNode.elementSeparator) {
                "${it.key}${MapElementNode.keyValueSeparator}${it.value}"
            }}${ObjectNode.endToken}"
        }}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            val obj =
                    result?.dereference(analyzer.memory) as? LxmObject ?: throw Error("The result must be a LxmObject")

            // Make the result.
            val targetResult: MutableMap<String, LexemPrimitive> = target.toMutableMap()
            targetResult.putAll(source1)
            targetResult.putAll(source2)

            var size = 0
            for ((key, prop) in obj.getAllIterableProperties()) {
                val inTarget = targetResult[key] ?: throw Error("The result[$key] is incorrect")

                Assertions.assertEquals(inTarget, prop.value, "The result[$key] is incorrect")

                size += 1
            }
            Assertions.assertEquals(targetResult.size, size, "The number of elements is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test newFrom - incorrect prototype type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val fnCallArguments = LxmInteger.Num0
            val fnCall = "${ObjectType.TypeName}${AccessExplicitMemberNode.accessToken}${ObjectType.NewFrom}"
            val grammar = "$fnCall${FunctionCallNode.startToken}$fnCallArguments${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test assign - incorrect target type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val source1 = mapOf("b" to LxmInteger.Num0, "c" to LxmInteger.Num10)
            val source2 = mapOf("c" to LxmLogic.True, "d" to LxmLogic.False)
            val fnCallArguments = listOf(source1, source2)
            val fnCall = "${ObjectType.TypeName}${AccessExplicitMemberNode.accessToken}${ObjectType.Assign}"
            val grammar =
                    "$fnCall${FunctionCallNode.startToken}3${FunctionCallNode.argumentSeparator}${fnCallArguments.joinToString(
                            FunctionCallNode.argumentSeparator) {
                        "${ObjectNode.startToken}${it.entries.joinToString(ObjectNode.elementSeparator) {
                            "${it.key}${MapElementNode.keyValueSeparator}${it.value}"
                        }}${ObjectNode.endToken}"
                    }}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test assign - incorrect source type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val target = mapOf("a" to LxmInteger.Num1, "b" to LxmInteger.Num2)
            val source1 = mapOf("b" to LxmInteger.Num0, "c" to LxmInteger.Num10)
            val fnCallArguments = listOf(target, source1)
            val fnCall = "${ObjectType.TypeName}${AccessExplicitMemberNode.accessToken}${ObjectType.Assign}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator) {
                "${ObjectNode.startToken}${it.entries.joinToString(ObjectNode.elementSeparator) {
                    "${it.key}${MapElementNode.keyValueSeparator}${it.value}"
                }}${ObjectNode.endToken}"
            }}${FunctionCallNode.argumentSeparator}3${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }
}
