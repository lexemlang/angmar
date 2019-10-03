package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Analyzer for prefixed expressions.
 */
internal object PrefixExpressionAnalyzer {
    const val signalEndPrefix = AnalyzerNodesCommons.signalStart + 1
    const val signalEndElement = signalEndPrefix + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PrefixExpressionNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.element)
            }
            signalEndElement -> {
                return analyzer.nextNode(node.prefix)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
