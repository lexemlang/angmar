package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*


/**
 * Analyzer for group lexemes.
 */
internal object QuantifiedGroupLexemAnalyzer {
    const val signalEndMainModifier = AnalyzerNodesCommons.signalStart + 1
    private const val signalBadEnd = signalEndMainModifier + 1
    const val signalEndFirstPattern = signalBadEnd + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: QuantifiedGroupLexemeNode) {
        val signalEndFirstModifier = signalEndFirstPattern + node.patterns.size
        val signalBadEndFirstPattern = signalEndFirstModifier + node.patterns.size

        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.isNegated) {
                    // Save the first index for atomic.
                    val memoryIndex = LxmBigNode(analyzer.memory.lastNode)

                    // On backwards skip.
                    analyzer.freezeMemoryCopy(node, signalBadEnd)

                    // Put the index in the stack.
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LastNode, memoryIndex)
                }

                if (node.mainModifier != null) {
                    return analyzer.nextNode(node.mainModifier)
                }

                val union = LxmPatternUnion(LxmQuantifier(-1, -1), LxmInteger.Num0, analyzer.memory)
                val quantifiedGroup = LxmQuantifiedGroup(analyzer.memory, union)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeUnion, quantifiedGroup)

                return callNextModifier(analyzer, quantifiedGroup, node, 0)
            }
            signalEndMainModifier -> {
                val quantifier = analyzer.memory.getLastFromStack() as LxmQuantifier
                val union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)
                val quantifiedGroup = LxmQuantifiedGroup(analyzer.memory, union)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeUnion, quantifiedGroup)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                return callNextModifier(analyzer, quantifiedGroup, node, 0)
            }
            in signalEndFirstModifier until signalEndFirstModifier + node.modifiers.size -> {
                val position = (signal - signalEndFirstModifier) + 1

                // Adds the union.
                val quantifier = analyzer.memory.getLastFromStack() as LxmQuantifier
                val union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)
                val quantifiedGroup = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = true) as LxmQuantifiedGroup
                quantifiedGroup.addUnion(analyzer.memory, union)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                return callNextModifier(analyzer, quantifiedGroup, node, position)
            }
            in signalEndFirstPattern until signalEndFirstPattern + node.patterns.size -> {
                val position = (signal - signalEndFirstPattern) + 1

                // Update the index.
                val quantifiedGroup = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = true) as LxmQuantifiedGroup
                quantifiedGroup.increaseIndex(analyzer.memory, position - 1)

                return callNextPattern(analyzer, quantifiedGroup, node, 0)
            }
            in signalBadEndFirstPattern until signalBadEndFirstPattern + node.patterns.size -> {
                val position = (signal - signalBadEndFirstPattern) + 1

                // Skip the pattern.

                val quantifiedGroup = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = false) as LxmQuantifiedGroup
                return callNextPattern(analyzer, quantifiedGroup, node, position)
            }
            signalBadEnd -> {
                // Skip
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Calls the next available modifier.
     */
    private fun callNextModifier(analyzer: LexemAnalyzer, quantifiedGroup: LxmQuantifiedGroup,
            node: QuantifiedGroupLexemeNode, from: Int) {
        for (i in node.modifiers.drop(from)) {
            if (i == null) {
                val union = LxmPatternUnion(LxmQuantifier.AlternativePattern, LxmInteger.Num0, analyzer.memory)
                quantifiedGroup.addUnion(analyzer.memory, union)
            } else {
                return analyzer.nextNode(i)
            }
        }

        // Calculate the main union.
        val oldMainQuantifier = quantifiedGroup.getMainUnion(analyzer.memory, toWrite = false).quantifier
        quantifiedGroup.calculateMainUnion(analyzer.memory)

        // Check bounds.
        val mainQuantifier = quantifiedGroup.getMainUnion(analyzer.memory, toWrite = false).quantifier
        if (mainQuantifier.max < mainQuantifier.min) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectQuantifierBounds,
                    "The maximum value cannot be lower than the minimum. Actual: {min: ${mainQuantifier.min}, max: ${mainQuantifier.max}}") {
                val fullText = node.parser.reader.readAllText()
                addSourceCode(fullText, node.parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(node.mainModifier!!.from.position(), node.mainModifier!!.to.position() - 1)
                }

                when {
                    oldMainQuantifier.min < 0 -> {
                        addSourceCode(fullText, "") {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.mainModifier!!.maximum!!.from.position(),
                                    node.mainModifier!!.maximum!!.to.position() - 1)
                            message =
                                    "Review the returned value of the maximum expression is greater or equal than the calculated minimum (${mainQuantifier.min})"
                        }
                    }
                    oldMainQuantifier.max < 0 -> {
                        addSourceCode(fullText, "") {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.mainModifier!!.minimum!!.from.position(),
                                    node.mainModifier!!.minimum!!.to.position() - 1)
                            message =
                                    "Review the returned value of the minimum expression is lower or equal than the calculated maximum (${mainQuantifier.max})"
                        }
                    }
                }
            }
        }

        return callNextPattern(analyzer, quantifiedGroup, node, 0)
    }

    /**
     * Calls the next pattern that can be repeated.
     */
    private fun callNextPattern(analyzer: LexemAnalyzer, quantifiedGroup: LxmQuantifiedGroup,
            node: QuantifiedGroupLexemeNode, from: Int) {
        val signalBadEndFirstPattern = signalEndFirstPattern + 2 * node.patterns.size

        for ((index, pattern) in node.patterns.withIndex().drop(from)) {
            if (quantifiedGroup.canHaveANextPattern(analyzer.memory, index)) {
                // On backwards try next.
                analyzer.freezeMemoryCopy(node, signalBadEndFirstPattern + index)

                return analyzer.nextNode(pattern)
            }
        }

        // Check whether the group is finished.
        if (quantifiedGroup.isGroupFinished(analyzer.memory)) {
            if (node.isNegated) {
                // Restore the memory index of the start and continue.
                val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                analyzer.restoreMemoryCopy(index.node)

                return analyzer.initBacktracking()
            }

            // Remove LexemUnion from the stack.
            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeUnion)
        } else {
            if (!node.isNegated) {
                return analyzer.initBacktracking()
            }

            // Restore the memory index of the start and continue.
            val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

            analyzer.restoreMemoryCopy(index.node)
        }

        analyzer.memory.addToStackAsLast(LxmNil)

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
