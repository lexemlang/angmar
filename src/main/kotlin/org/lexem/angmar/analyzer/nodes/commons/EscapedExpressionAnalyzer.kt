package org.lexem.angmar.analyzer.nodes.commons

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.commons.*


/**
 * Analyzer for escaped expressions.
 */
internal object EscapedExpressionAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: EscapedExpressionNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expression)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
