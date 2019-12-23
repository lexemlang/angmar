package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*


/**
 * Analyzer for lambda statements.
 */
internal object LambdaStmtAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LambdaStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate a new context.
                val type = if (!node.isDescriptiveCode) {
                    LxmContext.LxmContextType.Function
                } else if (node.isFilterCode) {
                    LxmContext.LxmContextType.Filter
                } else {
                    LxmContext.LxmContextType.Expression
                }

                AnalyzerCommons.createAndAssignNewContext(analyzer.memory, type)

                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                // If it is functional code rise a return.
                if (!node.isDescriptiveCode) {
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
