package org.lexem.angmar.analyzer.stdlib.types

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class MapTypeTest {
    @Test
    fun `test assign`() {
        val varName = "test"
        val target = mapOf("a" to LxmInteger.Num1, "b" to LxmInteger.Num2)
        val source1 = mapOf("b" to LxmInteger.Num0, "c" to LxmInteger.Num10)
        val source2 = mapOf("c" to LxmLogic.True, "d" to LxmLogic.False)
        val fnCallArguments = listOf(target, source1, source2)
        val fnCall = "${MapType.TypeName}${AccessExplicitMemberNode.accessToken}${MapType.Assign}"
        val grammar =
                "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                        FunctionCallNode.argumentSeparator) {
                    "${MapNode.macroName}${MapNode.startToken}${it.entries.joinToString(MapNode.elementSeparator) {
                        "${StringNode.startToken}${it.key}${StringNode.endToken}${MapElementNode.keyValueSeparator}${it.value}"
                    }}${MapNode.endToken}"
                }}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val map = context.getPropertyValue(analyzer.memory, varName)?.dereference(analyzer.memory) as? LxmMap
                ?: throw Error("The result must be a LxmMap")

        // Make the result.
        val targetResult: MutableMap<String, LexemPrimitive> = target.toMutableMap()
        targetResult.putAll(source1)
        targetResult.putAll(source2)

        var size = 0
        for ((_, propList) in map.getAllProperties()) {
            for (prop in propList) {
                val inTarget = targetResult[(prop.key as LxmString).primitive] ?: throw Error(
                        "The result[${prop.key}] is incorrect")

                Assertions.assertEquals(inTarget, prop.value, "The result[${prop.key}] is incorrect")

                size += 1
            }
        }
        Assertions.assertEquals(targetResult.size, size, "The number of elements is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }


    @Test
    @Incorrect
    fun `test assign - incorrect target type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val varName = "test"
            val source1 = mapOf("b" to LxmInteger.Num0, "c" to LxmInteger.Num10)
            val source2 = mapOf("c" to LxmLogic.True, "d" to LxmLogic.False)
            val fnCallArguments = listOf(source1, source2)
            val fnCall = "${MapType.TypeName}${AccessExplicitMemberNode.accessToken}${MapType.Assign}"
            val grammar =
                    "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}3${FunctionCallNode.argumentSeparator}${fnCallArguments.joinToString(
                            FunctionCallNode.argumentSeparator) {
                        "${MapNode.macroName}${MapNode.startToken}${it.entries.joinToString(MapNode.elementSeparator) {
                            "${StringNode.startToken}${it.key}${StringNode.endToken}${MapElementNode.keyValueSeparator}${it.value}"
                        }}${MapNode.endToken}"
                    }}${FunctionCallNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test assign - incorrect source type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val varName = "test"
            val target = mapOf("a" to LxmInteger.Num1, "b" to LxmInteger.Num2)
            val source1 = mapOf("b" to LxmInteger.Num0, "c" to LxmInteger.Num10)
            val fnCallArguments = listOf(target, source1)
            val fnCall = "${MapType.TypeName}${AccessExplicitMemberNode.accessToken}${MapType.Assign}"
            val grammar =
                    "$varName ${AssignOperatorNode.assignOperator} $fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                            FunctionCallNode.argumentSeparator) {
                        "${MapNode.macroName}${MapNode.startToken}${it.entries.joinToString(MapNode.elementSeparator) {
                            "${StringNode.startToken}${it.key}${StringNode.endToken}${MapElementNode.keyValueSeparator}${it.value}"
                        }}${MapNode.endToken}"
                    }}${FunctionCallNode.argumentSeparator}3${FunctionCallNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
