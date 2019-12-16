package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*


/**
 * Analyzer to call internal functions.
 */
internal object InternalFunctionCallAnalyzer {

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int) {
        val function = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Function).dereference(analyzer.memory,
                toWrite = false) as LxmFunction
        val argumentsReference = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments) as LxmReference

        // Call the function
        val hasEnded = function.internalFunction!!(analyzer, argumentsReference, function, signal)
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
}
