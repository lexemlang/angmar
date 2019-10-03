package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*


/**
 * Analyzer to call internal functions.
 */
internal object InternalFunctionCallAnalyzer {

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int) {
        when (signal) {
            AnalyzerNodesCommons.signalStart, AnalyzerNodesCommons.signalCallFunction -> {
                val function = analyzer.memory.popStack() as? LxmInternalFunction ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.IncompatibleType, "The function must be a built-in") {}
                val arguments = analyzer.memory.popStack()
                val lastCodePoint = analyzer.memory.popStack() as LxmCodePoint

                // Save the returned point and function.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLastCodePoint, lastCodePoint)
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenInternalFunction, function)

                // Call the function
                val derefArguments = arguments.dereference(analyzer.memory) as LxmArguments
                val hasEnded = function.function(analyzer, derefArguments, signal)

                // Decrease the reference count of the arguments.
                (arguments as LxmReference).decreaseReferenceCount(analyzer.memory)

                if (hasEnded) {
                    // Remove the intermediate context.
                    AnalyzerCommons.removeCurrentFunctionContextAndAssignPrevious(analyzer.memory)

                    // Restore the last position
                    return analyzer.nextNode(lastCodePoint)
                }
            }
            else -> {
                // Call the function
                val function = AnalyzerCommons.getCurrentContextElement<LxmInternalFunction>(analyzer.memory,
                        AnalyzerCommons.Identifiers.HiddenInternalFunction)
                val hasEnded = function.function(analyzer, LxmArguments(), signal)
                if (hasEnded) {
                    // Restore the last position
                    val lastCodePoint = AnalyzerCommons.getCurrentContextElement<LxmCodePoint>(analyzer.memory,
                            AnalyzerCommons.Identifiers.HiddenLastCodePoint)

                    // Remove the intermediate context.
                    AnalyzerCommons.removeCurrentFunctionContextAndAssignPrevious(analyzer.memory)

                    return analyzer.nextNode(lastCodePoint)
                }
            }
        }
    }
}
