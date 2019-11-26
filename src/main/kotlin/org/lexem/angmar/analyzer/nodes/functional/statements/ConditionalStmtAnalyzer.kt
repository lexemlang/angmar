package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for conditional statements.
 */
internal object ConditionalStmtAnalyzer {
    const val signalEndCondition = AnalyzerNodesCommons.signalStart + 1
    const val signalEndThenBlock = signalEndCondition + 1
    const val signalEndElseBlock = signalEndThenBlock + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ConditionalStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate an intermediate context.
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                return analyzer.nextNode(node.condition)
            }
            signalEndCondition -> {
                // Evaluate the condition.
                val condition = analyzer.memory.getLastFromStack()
                val conditionTruthy = RelationalFunctions.isTruthy(condition)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (node.isUnless) {
                    if (!conditionTruthy) {
                        return analyzer.nextNode(node.thenBlock)
                    }

                    // Remove the intermediate context.
                    AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                    if (node.elseBlock != null) {
                        // Generate an intermediate context.
                        AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                        return analyzer.nextNode(node.elseBlock)
                    }
                } else {
                    if (conditionTruthy) {
                        return analyzer.nextNode(node.thenBlock)
                    }

                    // Remove the intermediate context.
                    AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                    if (node.elseBlock != null) {
                        // Generate an intermediate context.
                        AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                        return analyzer.nextNode(node.elseBlock)
                    }
                }
            }
            signalEndThenBlock, signalEndElseBlock -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)
            }
            // Process the control signal.
            AnalyzerNodesCommons.signalExitControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                // Propagate the control signal.
                if (contextTag == null || control.tag != contextTag) {
                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                return analyzer.nextNode(node.parent, signal)
            }
            AnalyzerNodesCommons.signalRestartControl, AnalyzerNodesCommons.signalReturnControl -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
