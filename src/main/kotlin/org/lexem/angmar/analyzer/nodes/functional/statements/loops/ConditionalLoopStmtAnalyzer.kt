package org.lexem.angmar.analyzer.nodes.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.parser.functional.statements.loops.*


/**
 * Analyzer for conditional loop statements.
 */
internal object ConditionalLoopStmtAnalyzer {
    const val signalEndIndex = AnalyzerNodesCommons.signalStart + 1
    const val signalEndCondition = signalEndIndex + 1
    const val signalEndThenBlock = signalEndCondition + 1
    const val signalEndLastClause = signalEndThenBlock + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ConditionalLoopStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate an intermediate context.
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                // Save the index
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopIndexValue, LxmInteger.Num0)

                if (node.index != null) {
                    return analyzer.nextNode(node.index)
                }

                return analyzer.nextNode(node.condition)
            }
            signalEndIndex -> {
                // Save the index
                val indexName = analyzer.memory.popStack() as LxmString
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)

                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopIndexName, indexName)
                context.setProperty(analyzer.memory, indexName.primitive, LxmInteger.Num0)

                return analyzer.nextNode(node.condition)
            }
            signalEndCondition -> {
                return evaluateCondition(analyzer, node)
            }
            signalEndThenBlock -> {
                // Increase the current index.
                incrementIterationIndex(analyzer, node)

                // Execute the condition.
                return analyzer.nextNode(node.condition)
            }
            signalEndLastClause -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)
            }
            // Process the control signals.
            AnalyzerNodesCommons.signalExitControl -> {
                val control = analyzer.memory.popStack() as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    analyzer.memory.pushStack(control)
                    return analyzer.nextNode(node.parent, signal)
                }
            }
            AnalyzerNodesCommons.signalNextControl -> {
                val control = analyzer.memory.popStack() as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    // Remove the iteration context.
                    AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                    analyzer.memory.pushStack(control)
                    return analyzer.nextNode(node.parent, signal)
                }

                // Increase the current index.
                incrementIterationIndex(analyzer, node)

                // Execute the condition.
                return analyzer.nextNode(node.condition)
            }
            AnalyzerNodesCommons.signalRedoControl -> {
                val control = analyzer.memory.popStack() as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    // Remove the iteration context.
                    AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                    analyzer.memory.pushStack(control)
                    return analyzer.nextNode(node.parent, signal)
                }

                // Execute the block again.
                return analyzer.nextNode(node.thenBlock)
            }
            AnalyzerNodesCommons.signalRestartControl -> {
                val control = analyzer.memory.popStack() as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    // Remove the iteration context.
                    AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                    analyzer.memory.pushStack(control)
                    return analyzer.nextNode(node.parent, signal)
                }

                // Start again the loop.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopIndexValue, LxmInteger.Num0)

                // Execute the condition.
                return analyzer.nextNode(node.condition)
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalReturnControl -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Performs the next iteration of a loop.
     */
    private fun evaluateCondition(analyzer: LexemAnalyzer, node: ConditionalLoopStmtNode) {
        // Evaluate the condition.
        val condition = analyzer.memory.popStack()
        var conditionTruthy = RelationalFunctions.isTruthy(condition)

        if (node.isUntil) {
            conditionTruthy = !conditionTruthy
        }

        if (conditionTruthy) {
            return analyzer.nextNode(node.thenBlock)
        }

        val indexValue = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory,
                AnalyzerCommons.Identifiers.HiddenLoopIndexValue)

        if (indexValue.primitive == 0 && node.lastClauses?.elseBlock != null) {
            return analyzer.nextNode(node.lastClauses!!.elseBlock)
        }

        if (indexValue.primitive != 0 && node.lastClauses?.lastBlock != null) {
            // Remove the name of the intermediate statement.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            context.removeProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenContextTag)

            return analyzer.nextNode(node.lastClauses!!.lastBlock)
        }

        // Remove the iteration and intermediate contexts.
        AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Increment the iteration index.
     */
    private fun incrementIterationIndex(analyzer: LexemAnalyzer, node: ConditionalLoopStmtNode, count: Int = 1) {
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val lastIndex = context.getDereferencedProperty<LxmInteger>(analyzer.memory,
                AnalyzerCommons.Identifiers.HiddenLoopIndexValue)!!
        val newIndex = LxmInteger.from(lastIndex.primitive + count)

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopIndexValue, newIndex)

        // Set the index if there is an index expression.
        if (node.index != null) {
            val indexName = context.getDereferencedProperty<LxmString>(analyzer.memory,
                    AnalyzerCommons.Identifiers.HiddenLoopIndexName)!!

            context.setProperty(analyzer.memory, indexName.primitive, newIndex)
        }
    }
}
