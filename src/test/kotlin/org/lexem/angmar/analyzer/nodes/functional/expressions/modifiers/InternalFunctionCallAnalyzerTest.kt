package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class InternalFunctionCallAnalyzerTest {
    @Test
    fun `test finalizing`() {
        val functionName = "fn"
        val grammar = "$functionName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AccessExpressionNode.Companion::parse)

        // Prepare context.
        var executed = false
        val function = LxmInternalFunction { _, _, _ ->
            executed = true

            // Always return a value
            analyzer.memory.addToStackAsLast(LxmNil)
            return@LxmInternalFunction true
        }

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, functionName, function)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertTrue(executed, "The function has not been executed")
        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The returned value must be nil")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(functionName))
    }

    @Test
    fun `test not immediately finalizing`() {
        val functionName = "fn"
        val grammar = "$functionName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AccessExpressionNode.Companion::parse)

        // Prepare context.
        var executed = -1
        val function = LxmInternalFunction { analyzer, _, signal ->
            when (signal) {
                AnalyzerNodesCommons.signalCallFunction -> {
                    executed = signal

                    // Prepare stack to call toString over an integer.
                    val value = LxmInteger.Num10
                    val prototype = value.getPrototypeAsObject(analyzer.memory)
                    val function = prototype.getDereferencedProperty<LxmInternalFunction>(analyzer.memory,
                            AnalyzerCommons.Identifiers.ToString)!!

                    val arguments = LxmArguments(analyzer.memory)
                    arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, value)
                    val argumentsRef = analyzer.memory.add(arguments)

                    AnalyzerNodesCommons.callFunction(analyzer, function, argumentsRef, InternalFunctionCallNode,
                            LxmCodePoint(InternalFunctionCallNode, 1))

                    return@LxmInternalFunction false
                }
                else -> {
                    executed = signal
                }
            }

            return@LxmInternalFunction true
        }

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, functionName, function)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(1, executed, "The function has not been correctly executed")

        val value =
                analyzer.memory.getLastFromStack() as? LxmString ?: throw Error("The stack value must be a LxmString")
        Assertions.assertEquals("10", value.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(functionName))
    }
}
