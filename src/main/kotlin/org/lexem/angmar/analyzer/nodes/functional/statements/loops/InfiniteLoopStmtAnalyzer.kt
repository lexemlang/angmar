package org.lexem.angmar.analyzer.nodes.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.functional.statements.loops.*


/**
 * Analyzer for infinite loop statements.
 */
internal object InfiniteLoopStmtAnalyzer {
    const val signalEndIndex = AnalyzerNodesCommons.signalStart + 1
    const val signalEndThenBlock = signalEndIndex + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: InfiniteLoopStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate an intermediate context.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory, context.type)

                // Save the index.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LoopIndexValue, LxmInteger.Num0)

                if (node.index != null) {
                    return analyzer.nextNode(node.index)
                }

                return analyzer.nextNode(node.thenBlock)
            }
            signalEndIndex -> {
                // Save the index.
                val indexName = analyzer.memory.getLastFromStack() as LxmString
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.LoopIndexName)

                // Set the index in the context.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                context.setProperty( indexName.primitive, LxmInteger.Num0)

                return analyzer.nextNode(node.thenBlock)
            }
            signalEndThenBlock -> {
                // Increase the current index.
                incrementIterationIndex(analyzer, node)

                return analyzer.nextNode(node.thenBlock)
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
            }
            AnalyzerNodesCommons.signalNextControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    finish(analyzer, node)

                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)

                // Increase the current index.
                incrementIterationIndex(analyzer, node)

                return analyzer.nextNode(node.thenBlock)
            }
            AnalyzerNodesCommons.signalRedoControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    finish(analyzer, node)

                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)

                // Execute the block again.
                return analyzer.nextNode(node.thenBlock)
            }
            AnalyzerNodesCommons.signalRestartControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    finish(analyzer, node)

                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)

                // Start again the loop.
                analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.LoopIndexValue, LxmInteger.Num0)

                // Execute the condition.
                return analyzer.nextNode(node.thenBlock)
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalReturnControl -> {
                finish(analyzer, node)

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Increment the iteration index.
     */
    private fun incrementIterationIndex(analyzer: LexemAnalyzer, node: InfiniteLoopStmtCompiled, count: Int = 1) {
        val lastIndex = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIndexValue) as LxmInteger
        val newIndex = LxmInteger.from(lastIndex.primitive + count)

        analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.LoopIndexValue, newIndex)

        // Set the index if there is an index expression.
        if (node.index != null) {
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            val indexName = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIndexName) as LxmString

            context.setProperty( indexName.primitive, newIndex)
        }
    }

    /**
     * Process the finalization of the loop.
     */
    private fun finish(analyzer: LexemAnalyzer, node: InfiniteLoopStmtCompiled) {
        // Remove the intermediate context.
        AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

        // Remove LoopIndexName and LoopIndexValue from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LoopIndexValue)
        if (node.index != null) {
            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LoopIndexName)
        }
    }
}
