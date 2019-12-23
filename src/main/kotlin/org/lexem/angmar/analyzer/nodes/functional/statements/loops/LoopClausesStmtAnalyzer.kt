package org.lexem.angmar.analyzer.nodes.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.functional.statements.loops.*


/**
 * Analyzer for last clauses of loop statements.
 */
internal object LoopClausesStmtAnalyzer {
    const val signalEndClause = AnalyzerNodesCommons.signalStart + 1
    val optionForLast = LxmLogic.False
    val optionForElse = LxmLogic.True

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LoopClausesStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                val option = analyzer.memory.getLastFromStack()

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (option == optionForLast) {
                    if (node.lastBlock != null) {
                        return analyzer.nextNode(node.lastBlock)
                    }
                } else {
                    if (node.elseBlock != null) {
                        return analyzer.nextNode(node.elseBlock)
                    }
                }
            }
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
