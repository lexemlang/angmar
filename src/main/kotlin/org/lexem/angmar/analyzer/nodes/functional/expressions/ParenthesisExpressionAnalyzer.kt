package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Analyzer for parenthesized expressions.
 */
internal object ParenthesisExpressionAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ParenthesisExpressionNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expression)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
