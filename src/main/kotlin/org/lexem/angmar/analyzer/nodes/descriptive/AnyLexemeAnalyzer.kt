package org.lexem.angmar.analyzer.nodes.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.descriptive.*


/**
 * Analyzer for lexemes.
 */
internal object AnyLexemeAnalyzer {
    const val signalEndDataCapturing = AnalyzerNodesCommons.signalStart + 1
    const val signalEndLexem = signalEndDataCapturing + 1
    const val signalEndQuantifier = signalEndLexem + 1
    const val signalStartLexemeForLazy = signalEndQuantifier + 1
    const val signalExitForGreedy = signalStartLexemeForLazy + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AnyLexemeCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Save the memory big node for atomic quantifiers
                val atomicFirstIndex = analyzer.memory.lastNode
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.AtomicFirstIndex, LxmBigNode(atomicFirstIndex))

                if (node.dataCapturing != null) {
                    return analyzer.nextNode(node.dataCapturing)
                }

                if (node.quantifier != null) {
                    return analyzer.nextNode(node.quantifier)
                }

                return analyzer.nextNode(node.lexeme)
            }
            signalEndDataCapturing -> {
                // Set the data capturing name.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.LexemeDataCapturingName)

                val list = LxmList(analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeDataCapturingList, list)

                if (node.quantifier != null) {
                    return analyzer.nextNode(node.quantifier)
                }

                return analyzer.nextNode(node.lexeme)
            }
            signalEndQuantifier -> {
                // Set the quantifier and the index.
                val quantifier = analyzer.memory.getLastFromStack() as LxmQuantifier
                val union = LxmPatternUnion(analyzer.memory, quantifier, LxmInteger.Num0)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeUnion, union)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                return evaluateCondition(analyzer, node)
            }
            signalEndLexem -> {
                val result = if (node.isBlock) {
                    LxmNil
                } else {
                    analyzer.memory.getLastFromStack()
                }

                // Add result to the list.
                if (node.dataCapturing != null) {
                    val list = analyzer.memory.getFromStack(
                            AnalyzerCommons.Identifiers.LexemeDataCapturingList).dereference(analyzer.memory,
                            toWrite = true) as LxmList
                    list.addCell(result)
                }

                if (!node.isBlock) {
                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()
                }

                if (node.quantifier != null) {
                    // Increase the index.
                    incrementIterationIndex(analyzer)

                    // Evaluate condition.
                    return evaluateCondition(analyzer, node)
                } else {
                    return finalization(analyzer, node, isAtomic = false)
                }
            }
            signalStartLexemeForLazy -> {
                // Evaluate the condition.
                val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = false) as LxmPatternUnion

                if (union.canHaveANextPattern(analyzer.memory)) {
                    // Execute the then block.
                    return analyzer.nextNode(node.lexeme)
                } else {
                    return analyzer.initBacktracking()
                }
            }
            signalExitForGreedy -> {
                val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = false) as LxmPatternUnion

                return finalization(analyzer, node, union.quantifier.isAtomic)
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalExitControl, AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl -> {
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
     * Performs the next iteration of a loop.
     */
    private fun evaluateCondition(analyzer: LexemAnalyzer, node: AnyLexemeCompiled) {
        // Evaluate the condition.
        val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(analyzer.memory,
                toWrite = false) as LxmPatternUnion
        val quantifier = union.quantifier
        val indexValue = union.getIndex(analyzer.memory)

        if (quantifier.isLazy) {
            when {
                quantifier.isFinished(indexValue.primitive) -> {
                    // Freezes a copy setting the next node to start the then block of the loop.
                    analyzer.freezeMemoryCopy(node, signalStartLexemeForLazy)

                    // Exit from the loop.
                    return finalization(analyzer, node, quantifier.isAtomic)
                }
                quantifier.canHaveANextIteration(indexValue.primitive) -> {
                    // Execute the lexeme.
                    return analyzer.nextNode(node.lexeme)
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

                    // Execute the lexeme.
                    return analyzer.nextNode(node.lexeme)
                }
                quantifier.isFinished(indexValue.primitive) -> {
                    // Exit from the loop.
                    return finalization(analyzer, node, quantifier.isAtomic)
                }
                else -> {
                    return analyzer.initBacktracking()
                }
            }
        }
    }

    /**
     * Evaluates the end of the lexeme.
     */
    private fun finalization(analyzer: LexemAnalyzer, node: AnyLexemeCompiled, isAtomic: Boolean) {
        if (isAtomic) {
            val atomicFirstIndex =
                    analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.AtomicFirstIndex) as LxmBigNode

            analyzer.memory.collapseTo(atomicFirstIndex.node)
        }

        // Sets the data capturing.
        setDataCapturing(analyzer, node)

        finish(analyzer, node)

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Sets the data captured in the specified variable.
     */
    private fun setDataCapturing(analyzer: LexemAnalyzer, node: AnyLexemeCompiled) {
        if (node.dataCapturing == null) {
            return
        }

        val setter = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeDataCapturingName) as LexemSetter
        val listRef = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeDataCapturingList)
        val list = listRef.dereference(analyzer.memory, toWrite = false) as LxmList

        if (node.quantifier == null) {
            setter.setSetterValue(analyzer.memory, list.getCell(0)!!)
        } else {
            setter.setSetterValue(analyzer.memory, listRef)
        }
    }

    /**
     * Increment the iteration index.
     */
    private fun incrementIterationIndex(analyzer: LexemAnalyzer) {
        val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(analyzer.memory,
                toWrite = true) as LxmPatternUnion

        union.increaseIndex(analyzer.memory)
    }

    /**
     * Process the finalization of the node.
     */
    private fun finish(analyzer: LexemAnalyzer, node: AnyLexemeCompiled) {
        // Remove AtomicFirstIndex, LexemeDataCapturingName, LexemeDataCapturingList, LexemeAlias and LexemeQuantifier from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.AtomicFirstIndex)
        if (node.dataCapturing != null) {
            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeDataCapturingName)
            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeDataCapturingList)
        }

        if (node.quantifier != null) {
            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeUnion)
        }
    }
}
