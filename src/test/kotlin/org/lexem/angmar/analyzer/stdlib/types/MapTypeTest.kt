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

internal class MapTypeTest {
    @Test
    fun `test assign`() {
        val target = mapOf("a" to LxmInteger.Num1, "b" to LxmInteger.Num2)
        val source1 = mapOf("b" to LxmInteger.Num0, "c" to LxmInteger.Num10)
        val source2 = mapOf("c" to LxmLogic.True, "d" to LxmLogic.False)
        val fnCallArguments = listOf(target, source1, source2)
        val fnCall = "${MapType.TypeName}${AccessExplicitMemberNode.accessToken}${MapType.Assign}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator) {
            "${MapNode.macroName}${MapNode.startToken}${it.entries.joinToString(MapNode.elementSeparator) {
                "${StringNode.startToken}${it.key}${StringNode.endToken}${MapElementNode.keyValueSeparator}${it.value}"
            }}${MapNode.endToken}"
        }}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            val map = result?.dereference(analyzer.memory, toWrite = false) as? LxmMap ?: throw Error(
                    "The result must be a LxmMap")

            // Make the result.
            val targetResult: MutableMap<String, LexemPrimitive> = target.toMutableMap()
            targetResult.putAll(source1)
            targetResult.putAll(source2)

            for ((key, value) in map.getAllProperties()) {
                val inTarget =
                        targetResult[(key as LxmString).primitive] ?: throw Error("The result[$key] is incorrect")

                Assertions.assertEquals(inTarget, value, "The result[$key] is incorrect")
            }
            Assertions.assertEquals(targetResult.size, map.size, "The number of elements is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test assign - incorrect target type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val source1 = mapOf("b" to LxmInteger.Num0, "c" to LxmInteger.Num10)
            val source2 = mapOf("c" to LxmLogic.True, "d" to LxmLogic.False)
            val fnCallArguments = listOf(source1, source2)
            val fnCall = "${MapType.TypeName}${AccessExplicitMemberNode.accessToken}${MapType.Assign}"
            val grammar =
                    "$fnCall${FunctionCallNode.startToken}3${FunctionCallNode.argumentSeparator}${fnCallArguments.joinToString(
                            FunctionCallNode.argumentSeparator) {
                        "${MapNode.macroName}${MapNode.startToken}${it.entries.joinToString(MapNode.elementSeparator) {
                            "${StringNode.startToken}${it.key}${StringNode.endToken}${MapElementNode.keyValueSeparator}${it.value}"
                        }}${MapNode.endToken}"
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
            val fnCall = "${MapType.TypeName}${AccessExplicitMemberNode.accessToken}${MapType.Assign}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator) {
                "${MapNode.macroName}${MapNode.startToken}${it.entries.joinToString(MapNode.elementSeparator) {
                    "${StringNode.startToken}${it.key}${StringNode.endToken}${MapElementNode.keyValueSeparator}${it.value}"
                }}${MapNode.endToken}"
            }}${FunctionCallNode.argumentSeparator}3${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }
}
