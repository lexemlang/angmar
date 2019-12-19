package org.lexem.angmar.analyzer.nodes.descriptive.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.descriptive.statements.loops.*


/**
 * Analyzer for quantified loop statements.
 */
internal object QuantifiedLoopStmtAnalyzer {
    const val signalEndIndex = AnalyzerNodesCommons.signalStart + 1
    const val signalEndQuantifier = signalEndIndex + 1
    const val signalEndThenBlock = signalEndQuantifier + 1
    const val signalEndLastClause = signalEndThenBlock + 1
    const val signalStartThenBlockForLazy = signalEndLastClause + 1
    const val signalExitForGreedy = signalStartThenBlockForLazy + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: QuantifiedLoopStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate an intermediate context.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory, context.type)

                // Save the index.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LoopIndexValue, LxmInteger.Num0)

                // Save the memory big node for atomic quantifiers.
                val atomicFirstIndex = analyzer.memory.lastNode
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.AtomicFirstIndex, LxmBigNode(atomicFirstIndex))

                if (node.index != null) {
                    return analyzer.nextNode(node.index)
                }

                return analyzer.nextNode(node.quantifier)
            }
            signalEndIndex -> {
                // Save the index.
                val indexName = analyzer.memory.getLastFromStack() as LxmString
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.LoopIndexName)

                // Set the index in the context.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                context.setProperty(analyzer.memory, indexName.primitive, LxmInteger.Num0)

                return analyzer.nextNode(node.quantifier)
            }
            signalEndQuantifier -> {
                // Set the quantifier.
                val quantifier = analyzer.memory.getLastFromStack() as LxmQuantifier
                val union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LoopUnion, union)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                return evaluateCondition(analyzer, node)
            }
            signalEndThenBlock -> {
                // Increase the current index.
                incrementIterationIndex(analyzer, node)

                // Evaluate the condition.
                return evaluateCondition(analyzer, node)
            }
            signalEndLastClause -> {
                finish(analyzer, node)
            }
            signalStartThenBlockForLazy -> {
                // Evaluate the condition.
                val union =
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopUnion).dereference(analyzer.memory,
                                toWrite = false) as LxmPatternUnion
                val quantifier = union.quantifier
                val indexValue = union.getIndex(analyzer.memory)

                if (quantifier.canHaveANextIteration(indexValue.primitive)) {
                    // Execute the then block.
                    return analyzer.nextNode(node.thenBlock)
                } else {
                    return analyzer.initBacktracking()
                }
            }
            signalExitForGreedy -> {
                val union =
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopUnion).dereference(analyzer.memory,
                                toWrite = false) as LxmPatternUnion
                val indexValue = union.getIndex(analyzer.memory)

                return evaluateLoopEnd(analyzer, node, indexValue.primitive)
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

                // Evaluate the condition.
                return evaluateCondition(analyzer, node)
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

                // Restart the loop.
                val union =
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopUnion).dereference(analyzer.memory,
                                toWrite = true) as LxmPatternUnion
                union.setIndex(analyzer.memory, LxmInteger.Num0)
                analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.LoopIndexValue, LxmInteger.Num0)

                // Evaluate the condition.
                return evaluateCondition(analyzer, node)
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
     * Performs the next iteration of a loop.
     */
    private fun evaluateCondition(analyzer: LexemAnalyzer, node: QuantifiedLoopStmtNode) {
        // Evaluate the condition.
        val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopUnion).dereference(analyzer.memory,
                toWrite = false) as LxmPatternUnion
        val quantifier = union.quantifier
        val indexValue = union.getIndex(analyzer.memory)

        if (quantifier.isLazy) {
            when {
                quantifier.isFinished(indexValue.primitive) -> {
                    // Freezes a copy setting the next node to start the then block of the loop.
                    analyzer.freezeMemoryCopy(node, signalStartThenBlockForLazy)

                    // Exit from the loop.
                    return evaluateLoopEnd(analyzer, node, indexValue.primitive)
                }
                quantifier.canHaveANextIteration(indexValue.primitive) -> {
                    // Execute the then block.
                    return analyzer.nextNode(node.thenBlock)
                }
                else -> {
                    return analyzer.initBacktracking()
                }
            }
        } else {
            when {
                quantifier.canHaveANextIteration(indexValue.primitive) -> {
                    // Freezes a copy setting the next node to start the then block of the loop
                    // only if the quantifier is satisfied.
                    if (quantifier.isFinished(indexValue.primitive)) {
                        analyzer.freezeMemoryCopy(node, signalExitForGreedy)
                    }

                    // Execute the then block.
                    return analyzer.nextNode(node.thenBlock)
                }
                quantifier.isFinished(indexValue.primitive) -> {
                    // Exit from the loop.
                    return evaluateLoopEnd(analyzer, node, indexValue.primitive)
                }
                else -> {
                    return analyzer.initBacktracking()
                }
            }
        }
    }

    /**
     * Evaluates the end of the end of the loop, including the end statements.
     */
    private fun evaluateLoopEnd(analyzer: LexemAnalyzer, node: QuantifiedLoopStmtNode, indexValue: Int = 1) {
        if (indexValue == 0 && node.lastClauses?.elseBlock != null) {
            return analyzer.nextNode(node.lastClauses!!.elseBlock)
        }

        if (indexValue != 0 && node.lastClauses?.lastBlock != null) {
            // Remove the name of the intermediate statement.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            context.removeProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenContextTag)

            return analyzer.nextNode(node.lastClauses!!.lastBlock)
        }

        finish(analyzer, node)

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Increment the iteration index.
     */
    private fun incrementIterationIndex(analyzer: LexemAnalyzer, node: QuantifiedLoopStmtNode) {
        val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopUnion).dereference(analyzer.memory,
                toWrite = true) as LxmPatternUnion

        union.increaseIndex(analyzer.memory)

        // Set the index if there is an index expression.
        if (node.index != null) {
            val newIndex = union.getIndex(analyzer.memory)
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            val indexName = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIndexName) as LxmString

            context.setProperty(analyzer.memory, indexName.primitive, newIndex)
        }
    }

    /**
     * Collapses the intermediate [BigNode]s.
     */
    private fun collapseBigNodes(analyzer: LexemAnalyzer, node: QuantifiedLoopStmtNode) {
        val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopUnion).dereference(analyzer.memory,
                toWrite = false) as LxmPatternUnion
        val quantifier = union.quantifier

        if (quantifier.isAtomic) {
            val atomicFirstIndex =
                    analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.AtomicFirstIndex) as LxmBigNode

            analyzer.memory.collapseTo(atomicFirstIndex.node)
        }
    }

    /**
     * Process the finalization of the loop.
     */
    private fun finish(analyzer: LexemAnalyzer, node: QuantifiedLoopStmtNode) {
        collapseBigNodes(analyzer, node)

        // Remove the intermediate context.
        AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

        // Remove LoopIndexValue, LoopUnion, AtomicFirstIndex and LoopIndexName from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LoopIndexValue)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LoopUnion)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.AtomicFirstIndex)
        if (node.index != null) {
            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LoopIndexName)
        }
    }
}
