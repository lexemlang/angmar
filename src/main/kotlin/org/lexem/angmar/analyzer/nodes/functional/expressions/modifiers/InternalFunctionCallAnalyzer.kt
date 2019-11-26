package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*


/**
 * Analyzer to call internal functions.
 */
internal object InternalFunctionCallAnalyzer {

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int) {
        when (signal) {
            AnalyzerNodesCommons.signalStart, AnalyzerNodesCommons.signalCallFunction -> {
                val function = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Function) as LxmInternalFunction
                val arguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(
                        analyzer.memory) as LxmArguments

                // Call the function
                val hasEnded = function.function(analyzer, arguments, signal)

                if (hasEnded) {
                    val lastCodePoint =
                            analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint) as LxmCodePoint

                    // Remove the intermediate context.
                    AnalyzerCommons.removeCurrentFunctionContextAndAssignPrevious(analyzer.memory)

                    // Remove Function, Arguments and ReturnCodePoint from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Function)
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint)

                    // Restore the last position.
                    return analyzer.nextNode(lastCodePoint)
                }
            }
            else -> {
                // Call the function
                val function = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Function) as LxmInternalFunction
                val arguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(
                        analyzer.memory) as LxmArguments
                val hasEnded = function.function(analyzer, arguments, signal)

                if (hasEnded) {
                    // Gets the last position.
                    val lastCodePoint =
                            analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint) as LxmCodePoint

                    // Remove the intermediate context.
                    AnalyzerCommons.removeCurrentFunctionContextAndAssignPrevious(analyzer.memory)

                    // Remove Function, Arguments and ReturnCodePoint from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Function)
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint)

                    return analyzer.nextNode(lastCodePoint)
                }
            }
        }
    }
}
