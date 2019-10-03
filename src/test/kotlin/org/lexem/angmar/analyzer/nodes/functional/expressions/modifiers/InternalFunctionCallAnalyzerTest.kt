package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class InternalFunctionCallAnalyzerTest {
    @Test
    fun `test finalizing`() {
        val analyzer = TestUtils.createAnalyzerFrom("") { _, _, _ ->
            InternalFunctionCallNode
        }

        // Prepare stack
        var executed = false
        val function = LxmInternalFunction { _, _, _ ->
            executed = true

            // Always return a value
            analyzer.memory.pushStack(LxmNil)
            return@LxmInternalFunction true
        }

        analyzer.memory.pushStack(LxmCodePoint(ParserNode.Companion.EmptyParserNode, 0))
        val arguments = LxmArguments(analyzer.memory)
        arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, LxmNil)
        val argumentsRef = analyzer.memory.add(arguments)
        analyzer.memory.pushStack(argumentsRef)
        analyzer.memory.pushStack(function)

        // Prepare call.
        val context = LxmContext()
        val contextRef = analyzer.memory.add(context)
        AnalyzerCommons.createAndAssignNewFunctionContext(analyzer.memory, contextRef)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertTrue(executed, "The function has not been executed")

        Assertions.assertEquals(LxmNil, analyzer.memory.popStack(), "The returned value must be nil")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test not immediately finalizing`() {
        val analyzer = TestUtils.createAnalyzerFrom("") { _, _, _ ->
            InternalFunctionCallNode
        }

        // Prepare stack
        var executed = -1
        val function = LxmInternalFunction { analyzer, _, signal ->
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    executed = signal

                    // Prepare stack to call toString over an integer.
                    val value = LxmInteger.Num10
                    val prototype = value.getPrototype(analyzer.memory)
                    val function = prototype.getDereferencedProperty<LxmInternalFunction>(analyzer.memory,
                            AnalyzerCommons.Identifiers.ToString)!!

                    val arguments = LxmArguments(analyzer.memory)
                    arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, value)
                    val argumentsRef = analyzer.memory.add(arguments)
                    argumentsRef.increaseReferenceCount(analyzer.memory)
                    AnalyzerNodesCommons.callFunction(analyzer, function, argumentsRef, InternalFunctionCallNode, 1)

                    return@LxmInternalFunction false
                }
                else -> {
                    executed = signal
                }
            }

            return@LxmInternalFunction true
        }

        analyzer.memory.pushStack(LxmCodePoint(ParserNode.Companion.EmptyParserNode, 0))
        val arguments = LxmArguments(analyzer.memory)
        arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, LxmNil)
        val argumentsRef = analyzer.memory.add(arguments)
        analyzer.memory.pushStack(argumentsRef)
        analyzer.memory.pushStack(function)

        // Prepare call.
        AnalyzerCommons.createAndAssignNewFunctionContext(analyzer.memory,
                AnalyzerCommons.getCurrentContextReference(analyzer.memory))

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(1, executed, "The function has not been correctly executed")

        val value = analyzer.memory.popStack() as? LxmString ?: throw Error("The stack value must be a LxmString")
        Assertions.assertEquals("10", value.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
