package org.lexem.angmar.analyzer.nodes.descriptive.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.descriptive.statements.*


/**
 * Analyzer for onBack block statements.
 */
internal object OnBackBlockStmtAnalyzer {
    const val signalEndParameters = AnalyzerNodesCommons.signalStart + 1
    const val signalOnBacktrack = signalEndParameters + 1
    const val signalEndBlock = signalOnBacktrack + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: OnBackBlockStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // On backwards execute the block.
                analyzer.freezeMemoryCopy(node, signalOnBacktrack)
            }
            signalOnBacktrack -> {
                // Generate an intermediate context.
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                if (node.parameters != null) {
                    // Add arguments
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments,
                            analyzer.backtrackingData ?: LxmBacktrackingData(emptyList(), emptyMap()))

                    // Clear the backtracking data.
                    analyzer.backtrackingData = null

                    return analyzer.nextNode(node.parameters)
                }

                return analyzer.nextNode(node.block)
            }
            signalEndParameters -> {
                return analyzer.nextNode(node.block)
            }
            signalEndBlock -> {
                // On normal exit, inits backtracking.
                return analyzer.initBacktracking()
            }
            // Process the control signals.
            AnalyzerNodesCommons.signalExitControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                finish(analyzer, node)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)

                // On backwards execute the block.
                analyzer.freezeMemoryCopy(node, signalOnBacktrack)
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl -> {
                finish(analyzer, node)

                return analyzer.nextNode(node.parent, signal)
            }
            AnalyzerNodesCommons.signalRestartControl, AnalyzerNodesCommons.signalReturnControl -> {
                finish(analyzer, node)

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Process the finalization of the node.
     */
    private fun finish(analyzer: LexemAnalyzer, node: OnBackBlockStmtNode) {
        // Remove the intermediate context.
        AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)
    }
}
