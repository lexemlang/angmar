package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
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
        val function = LxmFunction(analyzer.memory) { _, _, _, _ ->
            executed = true

            // Always return a value
            analyzer.memory.addToStackAsLast(LxmNil)
            return@LxmFunction true
        }

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(functionName, function)
        context.setProperty(AnalyzerCommons.Identifiers.HiddenCurrentContextName, LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertTrue(executed, "The function has not been executed")
        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The returned value must be nil")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(functionName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test not immediately finalizing`() {
        val functionName = "fn"
        val grammar = "$functionName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AccessExpressionNode.Companion::parse)

        // Prepare context.
        var executed = -1
        val function = LxmFunction(analyzer.memory) { analyzer, _, _, signal ->
            when (signal) {
                AnalyzerNodesCommons.signalCallFunction -> {
                    executed = signal

                    // Prepare stack to call toString over an integer.
                    val value = LxmInteger.Num10
                    val prototype = value.getPrototypeAsObject(analyzer.memory, toWrite = false)
                    val function = prototype.getPropertyValue(AnalyzerCommons.Identifiers.ToString)!!.dereference(
                            analyzer.memory, toWrite = false) as LxmFunction

                    val arguments = LxmArguments(analyzer.memory)
                    arguments.addNamedArgument(AnalyzerCommons.Identifiers.This, value)

                    AnalyzerNodesCommons.callFunction(analyzer, function, arguments,
                            LxmCodePoint(InternalFunctionCallCompiled, 1, CompiledNode.Companion.EmptyCompiledNode, ""))

                    return@LxmFunction false
                }
                else -> {
                    executed = signal
                }
            }

            return@LxmFunction true
        }

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(functionName, function)
        context.setProperty(AnalyzerCommons.Identifiers.HiddenCurrentContextName, LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(1, executed, "The function has not been correctly executed")

        val value =
                analyzer.memory.getLastFromStack() as? LxmString ?: throw Error("The stack value must be a LxmString")
        Assertions.assertEquals("10", value.primitive, "The primitive property is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(functionName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }
}
