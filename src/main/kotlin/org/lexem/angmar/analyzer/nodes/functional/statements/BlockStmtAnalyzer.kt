package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for block statements.
 */
internal object BlockStmtAnalyzer {
    const val signalEndTag = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFirstStatement = signalEndTag + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: BlockStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.tag != null) {
                    // Generate a new context.
                    AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                    return analyzer.nextNode(node.tag)
                }

                if (node.statements.isNotEmpty()) {
                    // Generate a new context.
                    AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                    return analyzer.nextNode(node.statements[0])
                }
            }
            signalEndTag -> {
                // Sets a tag to this block.
                val name = analyzer.memory.popStack() as LxmString
                AnalyzerCommons.namePreviousContextIfItHasNoName(analyzer.memory, name)

                if (node.statements.isNotEmpty()) {
                    return analyzer.nextNode(node.statements[0])
                }

                // Remove the context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)
            }
            in signalEndFirstStatement until signalEndFirstStatement + node.statements.size -> {
                val position = (signal - signalEndFirstStatement) + 1

                // Process the next node.
                if (position < node.statements.size) {
                    return analyzer.nextNode(node.statements[position])
                }

                // Remove the context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalExitControl, AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl -> {
                // Remove the context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                return analyzer.nextNode(node.parent, signal)
            }
            AnalyzerNodesCommons.signalRestartControl, AnalyzerNodesCommons.signalReturnControl -> {
                // Remove the context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
