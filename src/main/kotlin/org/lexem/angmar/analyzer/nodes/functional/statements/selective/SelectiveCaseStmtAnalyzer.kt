package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.selective.*


/**
 * Analyzer for cases of the selective statements.
 */
internal object SelectiveCaseStmtAnalyzer {
    const val signalEndBlock = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFirstPattern = signalEndBlock + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: SelectiveCaseStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate an intermediate context.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory, context.type)

                return analyzer.nextNode(node.patterns[0])
            }
            in signalEndFirstPattern until signalEndFirstPattern + node.patterns.size -> {
                // Check the result.
                val result = analyzer.memory.getLastFromStack() as LxmLogic
                if (result.primitive) {
                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    return analyzer.nextNode(node.block)
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                val position = (signal - signalEndFirstPattern) + 1
                if (position < node.patterns.size) {
                    return analyzer.nextNode(node.patterns[position])
                }

                finish(analyzer, node)

                // Set the nok flag.
                analyzer.memory.addToStackAsLast(LxmLogic.False)
            }
            signalEndBlock -> {
                finish(analyzer, node)

                // Set the ok flag.
                analyzer.memory.addToStackAsLast(LxmLogic.True)
            }
            // Process the control signal.
            AnalyzerNodesCommons.signalExitControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                finish(analyzer, node)

                // Propagate the control signal.
                if (contextTag == null || control.tag != contextTag) {
                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)

                // Set the ok flag.
                analyzer.memory.addToStackAsLast(LxmLogic.True)
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
     * Process the finalization of the loop.
     */
    private fun finish(analyzer: LexemAnalyzer, node: SelectiveCaseStmtNode) {
        // Remove the intermediate context.
        AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)
    }
}
