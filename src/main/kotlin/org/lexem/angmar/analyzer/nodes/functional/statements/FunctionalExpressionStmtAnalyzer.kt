package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.functional.statements.*


/**
 * Analyzer for variable declaration.
 */
internal object FunctionalExpressionStmtAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: FunctionalExpressionStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
