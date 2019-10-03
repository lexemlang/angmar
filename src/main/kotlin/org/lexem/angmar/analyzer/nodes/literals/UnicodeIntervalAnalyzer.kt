package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for unicode interval literals.
 */
internal object UnicodeIntervalAnalyzer {
    const val signalEndNode = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: UnicodeIntervalNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.node)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
