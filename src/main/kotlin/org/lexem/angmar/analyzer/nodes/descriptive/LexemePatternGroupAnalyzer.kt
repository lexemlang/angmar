package org.lexem.angmar.analyzer.nodes.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.*


/**
 * Analyzer for lexeme pattern anonymous groups.
 */
internal object LexemePatternGroupAnalyzer {
    const val signalEndQuantifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFirstPattern = signalEndQuantifier + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LexemePatternGroupNode) {
        val signalBadPattern = signalEndFirstPattern + node.patterns.size + 1

        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                when (node.type) {
                    LexemePatternNode.Companion.PatternType.Additive -> {
                        // Add quantifier.
                        analyzer.memory.addToStackAsLast(LxmQuantifier.AdditivePattern)
                    }
                    LexemePatternNode.Companion.PatternType.Selective -> {
                        // Add quantifier.
                        analyzer.memory.addToStackAsLast(LxmQuantifier.SelectivePattern)
                    }
                    LexemePatternNode.Companion.PatternType.Alternative -> {
                        // Add quantifier.
                        analyzer.memory.addToStackAsLast(LxmQuantifier.AlternativePattern)
                    }
                    LexemePatternNode.Companion.PatternType.Quantified -> {
                        return analyzer.nextNode(node.quantifier)
                    }
                    else -> throw AngmarUnreachableException()
                }

                // Jump to signalEndQuantifier.
                return analyzer.nextNode(node, signalEndQuantifier)
            }
            signalEndQuantifier -> {
                val quantifier = analyzer.memory.getLastFromStack() as LxmQuantifier
                val union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Check the quantifier is valid for the number of patterns.
                if (!union.canFinish(analyzer.memory, node.patterns.size)) {
                    throw AngmarAnalyzerException(
                            AngmarAnalyzerExceptionType.QuantifierMinimumIsGreaterThanNumberOfPatterns,
                            "The minimum of the pattern quantifier is greater than the number of patterns in the union. Number of patterns: ${node.patterns.size}, Quantifier: $quantifier") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.quantifier!!.minimum.from.position(),
                                    node.quantifier!!.minimum.to.position() - 1)
                            message =
                                    "Review that the returned value of this expression is lower or equal than ${node.patterns.size}"
                        }
                    }
                }

                // Add the union to the stack.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeUnion, analyzer.memory.add(union))

                if (union.canHaveANextPattern(analyzer.memory)) {
                    val pattern = node.patterns.first()

                    // On backwards try next.
                    analyzer.freezeMemoryCopy(node, signalBadPattern)

                    if (pattern == null) {
                        // Call this node again with the signal for the next pattern.
                        return analyzer.nextNode(node, signalEndFirstPattern)
                    } else {
                        return analyzer.nextNode(pattern)
                    }
                }

                if (union.isFinished(analyzer.memory)) {
                    finish(analyzer, node)
                } else {
                    return analyzer.initBacktracking()
                }
            }
            in signalEndFirstPattern until signalEndFirstPattern + node.patterns.size -> {
                val position = (signal - signalEndFirstPattern) + 1

                // Increase index.
                val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = true) as LxmPatternUnion
                union.increaseIndex(analyzer.memory)

                if (union.canHaveANextPattern(analyzer.memory)) {
                    if (position < node.patterns.size) {
                        val pattern = node.patterns[position]

                        // On backwards try next.
                        analyzer.freezeMemoryCopy(node, signalBadPattern + position)

                        if (pattern == null) {
                            // Call this node again with the signal for the next pattern.
                            return analyzer.nextNode(node, signalEndFirstPattern + position)
                        } else {
                            return analyzer.nextNode(pattern)
                        }
                    }
                }

                if (union.isFinished(analyzer.memory)) {
                    finish(analyzer, node)
                } else {
                    return analyzer.initBacktracking()
                }
            }
            in signalBadPattern until signalBadPattern + node.patterns.size -> {
                val position = (signal - signalBadPattern) + 1

                val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = false) as LxmPatternUnion

                // Check the quantifier can be matched with the remaining patterns.
                if (!union.canFinish(analyzer.memory, node.patterns.size - position)) {
                    return analyzer.initBacktracking()
                }

                if (union.canHaveANextPattern(analyzer.memory)) {
                    if (position < node.patterns.size) {
                        val pattern = node.patterns[position]

                        // On backwards try next.
                        analyzer.freezeMemoryCopy(node, signalBadPattern + position)

                        if (pattern == null) {
                            // Call this node again with the signal for the next pattern.
                            return analyzer.nextNode(node, signalEndFirstPattern + position)
                        } else {
                            return analyzer.nextNode(pattern)
                        }
                    }
                }

                if (union.isFinished(analyzer.memory)) {
                    finish(analyzer, node)
                } else {
                    return analyzer.initBacktracking()
                }
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
     * Process the finalization of the node.
     */
    private fun finish(analyzer: LexemAnalyzer, node: LexemePatternGroupNode) {
        // Remove LexemeUnion from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeUnion)
    }
}
