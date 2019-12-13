package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*


/**
 * Analyzer for lambda statements.
 */
internal object LambdaStmtAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LambdaStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate a new context.
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                return analyzer.nextNode(node.statement)
            }
            signalEndExpression -> {
                // If it is functional code rise a return.
                if (node.statement !is LexemePatternContentNode) {
                    val value = analyzer.memory.getLastFromStack()

                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Control,
                            LxmControl.from(ControlWithExpressionStmtNode.returnKeyword, null, value, node))

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    // Remove the context.
                    AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                    return analyzer.nextNode(node.parent, AnalyzerNodesCommons.signalReturnControl)
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
