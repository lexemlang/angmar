package org.lexem.angmar.analyzer.nodes.functional.expressions.macros

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Analyzer for macro 'backtrack'.
 */
internal object MacroBacktrackAnalyzer {
    const val signalEndValue = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: MacroBacktrackNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.arguments != null) {
                    // Add the function.
                    val fn = LxmFunction(analyzer.memory, this::executeBacktrackingWithArguments)
                    analyzer.memory.addToStackAsLast(fn)

                    return analyzer.nextNode(node.arguments)
                }

                return executeBacktrackingWithoutArguments(analyzer, node)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    private fun executeBacktrackingWithoutArguments(analyzer: LexemAnalyzer, node: MacroBacktrackNode) {
        analyzer.backtrackingData = null
        analyzer.initBacktracking()
    }

    private fun executeBacktrackingWithArguments(analyzer: LexemAnalyzer, arguments: LxmArguments,
            function: LxmFunction, signal: Int): Boolean {
        analyzer.backtrackingData = arguments.mapToBacktrackingData(analyzer.memory)
        analyzer.initBacktracking()

        return true
    }
}
