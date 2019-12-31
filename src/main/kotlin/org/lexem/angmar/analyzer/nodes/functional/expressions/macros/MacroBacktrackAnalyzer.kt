package org.lexem.angmar.analyzer.nodes.functional.expressions.macros

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.functional.expressions.macros.*


/**
 * Analyzer for macro 'backtrack'.
 */
internal object MacroBacktrackAnalyzer {
    const val signalEndArguments = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: MacroBacktrackCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Make this way because the behaviour of the arguments analyzer.
                val fn = LxmFunction(analyzer.memory, this::executeBacktrackingWithArguments)
                analyzer.memory.addToStackAsLast(fn)

                return analyzer.nextNode(node.arguments)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    private fun executeBacktrackingWithArguments(analyzer: LexemAnalyzer, arguments: LxmArguments,
            function: LxmFunction, signal: Int): Boolean {
        analyzer.backtrackingData = arguments.mapToBacktrackingData()
        analyzer.initBacktracking()

        return true
    }
}
