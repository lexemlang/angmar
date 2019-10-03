package org.lexem.angmar.analyzer.nodes.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.loops.*


/**
 * Analyzer for infinite loop statements.
 */
internal object InfiniteLoopStmtAnalyzer {
    const val signalEndIndex = AnalyzerNodesCommons.signalStart + 1
    const val signalEndThenBlock = signalEndIndex + 1
    const val signalEndLastClause = signalEndThenBlock + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: InfiniteLoopStmtNode) {
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

                return analyzer.nextNode(node.thenBlock)
            }
            signalEndIndex -> {
                // Save the index
                val indexName = analyzer.memory.popStack() as LxmString
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)

                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopIndexName, indexName)
                context.setProperty(analyzer.memory, indexName.primitive, LxmInteger.Num0)

                return analyzer.nextNode(node.thenBlock)
            }
            signalEndThenBlock -> {
                // Increase the current index.
                incrementIterationIndex(analyzer, node)

                return analyzer.nextNode(node.thenBlock)
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

                return analyzer.nextNode(node.thenBlock)
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
                return analyzer.nextNode(node.thenBlock)
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
     * Increment the iteration index.
     */
    private fun incrementIterationIndex(analyzer: LexemAnalyzer, node: InfiniteLoopStmtNode, count: Int = 1) {
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
