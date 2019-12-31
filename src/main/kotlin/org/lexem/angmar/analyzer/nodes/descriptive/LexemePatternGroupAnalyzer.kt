package org.lexem.angmar.analyzer.nodes.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.descriptive.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


/**
 * Analyzer for lexeme pattern anonymous groups.
 */
internal object LexemePatternGroupAnalyzer {
    const val signalEndQuantifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFirstPattern = signalEndQuantifier + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LexemePatternGroupCompiled) {
        val signalBadPattern = signalEndFirstPattern + node.patterns.size + 1

        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.initialQuantifier == null) {
                    return analyzer.nextNode(node.quantifier)
                } else {
                    // Add quantifier.
                    analyzer.memory.addToStackAsLast(node.initialQuantifier!!)
                }

                // Jump to signalEndQuantifier.
                return analyzer.nextNode(node, signalEndQuantifier)
            }
            signalEndQuantifier -> {
                val quantifier = analyzer.memory.getLastFromStack() as LxmQuantifier
                val union = LxmPatternUnion(analyzer.memory, quantifier, LxmInteger.Num0)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Check the quantifier is valid for the number of patterns.
                if (!union.canFinish(node.patterns.size)) {
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
                            highlightSection(node.quantifier!!.from.position(), node.quantifier!!.to.position() - 1)
                            message =
                                    "Review that the returned value of the minimum expression is lower or equal than ${node.patterns.size}"
                        }
                    }
                }

                // Add the union to the stack.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeUnion, union)

                if (union.canHaveANextPattern()) {
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

                if (union.isFinished()) {
                    finish(analyzer, node)
                } else {
                    return analyzer.initBacktracking()
                }
            }
            in signalEndFirstPattern..signalEndFirstPattern + node.patterns.size -> {
                val position = (signal - signalEndFirstPattern) + 1

                // Increase index.
                val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = true) as LxmPatternUnion
                union.increaseIndex()

                if (union.canHaveANextPattern()) {
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

                if (union.isFinished()) {
                    finish(analyzer, node)
                } else {
                    return analyzer.initBacktracking()
                }
            }
            in signalBadPattern..signalBadPattern + node.patterns.size -> {
                val position = (signal - signalBadPattern) + 1

                val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = false) as LxmPatternUnion

                // Check the quantifier can be matched with the remaining patterns.
                if (!union.canFinish(node.patterns.size - position)) {
                    return analyzer.initBacktracking()
                }

                if (union.canHaveANextPattern()) {
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

                if (union.isFinished()) {
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
    private fun finish(analyzer: LexemAnalyzer, node: LexemePatternGroupCompiled) {
        // Remove LexemeUnion from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeUnion)
    }
}
