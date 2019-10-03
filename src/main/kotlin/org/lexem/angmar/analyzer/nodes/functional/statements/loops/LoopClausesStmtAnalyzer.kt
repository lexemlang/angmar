package org.lexem.angmar.analyzer.nodes.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.loops.*


/**
 * Analyzer for last clauses of loop statements.
 */
internal object LoopClausesStmtAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LoopClausesStmtNode) {
        when (signal) {
            // Propagate the control signal.
            AnalyzerNodesCommons.signalExitControl, AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl -> {
                return analyzer.nextNode(node.parent, signal)
            }
            AnalyzerNodesCommons.signalRestartControl, AnalyzerNodesCommons.signalReturnControl -> {
                return analyzer.nextNode(node.parent, signal)
            }
        }
        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
