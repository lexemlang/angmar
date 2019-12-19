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
 * Analyzer for lexeme pattern.
 */
internal object LexemePatternAnalyzer {
    const val signalEndType = AnalyzerNodesCommons.signalStart + 1
    const val signalEndUnionName = signalEndType + 1
    const val signalEndPatternContent = signalEndUnionName + 1
    const val signalEndBadPatternContent = signalEndPatternContent + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LexemePatternNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                when (node.type) {
                    LexemePatternNode.Companion.PatternType.Static -> {
                        return analyzer.nextNode(node.patternContent)
                    }
                    LexemePatternNode.Companion.PatternType.Optional -> {
                        // On backwards skip.
                        analyzer.freezeMemoryCopy(node, signalEndBadPatternContent)

                        return analyzer.nextNode(node.patternContent)
                    }
                    LexemePatternNode.Companion.PatternType.Negative -> {
                        // Save the first index.
                        val memoryIndex = LxmBigNode(analyzer.memory.lastNode)

                        // On backwards skip.
                        analyzer.freezeMemoryCopy(node, signalEndBadPatternContent)

                        // Put the index in the stack.
                        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LastNode, memoryIndex)

                        return analyzer.nextNode(node.patternContent)
                    }
                    else -> {
                        if (node.quantifier != null) {
                            return analyzer.nextNode(node.quantifier)
                        }

                        return analyzer.nextNode(node.unionName)
                    }
                }
            }
            signalEndType -> {
                // Move Last to Union in stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.LexemeUnion)

                return analyzer.nextNode(node.unionName)
            }
            signalEndUnionName -> {
                val unionName = analyzer.memory.getLastFromStack() as LxmString
                val quantifier = if (node.quantifier != null) {
                    val res = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion) as LxmQuantifier

                    // Remove LexemeUnion from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeUnion)

                    res
                } else {
                    when (node.type) {
                        LexemePatternNode.Companion.PatternType.Additive -> LxmQuantifier.AdditivePattern
                        LexemePatternNode.Companion.PatternType.Selective -> LxmQuantifier.SelectivePattern
                        LexemePatternNode.Companion.PatternType.Quantified -> null
                        LexemePatternNode.Companion.PatternType.Alternative -> LxmQuantifier.AlternativePattern
                        else -> throw AngmarUnreachableException()
                    }
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                return executePattern(analyzer, node, unionName = unionName, quantifier = quantifier)
            }
            signalEndPatternContent -> {
                when (node.type) {
                    LexemePatternNode.Companion.PatternType.Static -> {
                        // Continue
                    }
                    LexemePatternNode.Companion.PatternType.Optional -> {
                        // Continue
                    }
                    LexemePatternNode.Companion.PatternType.Negative -> {
                        // Restore the memory copy.
                        val memoryIndex =
                                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode
                        analyzer.memory.restoreCopy(memoryIndex.node)

                        val lastCodePoint = analyzer.getLastRollbackCodePoint()
                        lastCodePoint.restore(analyzer)

                        // Init backtracking.
                        return analyzer.initBacktracking()
                    }
                    else -> {
                        // Get the union name.
                        val unionName =
                                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion) as LxmString

                        // Remove LexemeUnion from the stack.
                        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeUnion)

                        // Increase the index.
                        val union = getUnion(analyzer, node, unionName.primitive).dereference(analyzer.memory,
                                toWrite = true) as LxmPatternUnion
                        union.increaseIndex(analyzer.memory)
                    }
                }
            }
            signalEndBadPatternContent -> {
                when (node.type) {
                    LexemePatternNode.Companion.PatternType.Static -> throw AngmarUnreachableException()
                    LexemePatternNode.Companion.PatternType.Optional -> {
                        // Skip
                    }
                    LexemePatternNode.Companion.PatternType.Negative -> {
                        // Skip
                    }
                    else -> {
                        // Skip
                    }
                }
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalExitControl, AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl, AnalyzerNodesCommons.signalRestartControl, AnalyzerNodesCommons.signalReturnControl -> {
                when (node.type) {
                    LexemePatternNode.Companion.PatternType.Static -> {
                    }
                    LexemePatternNode.Companion.PatternType.Optional -> {
                    }
                    LexemePatternNode.Companion.PatternType.Negative -> {
                        // Remove LastNode from the stack.
                        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LastNode)
                    }
                    else -> {
                        // Get the union name.
                        val unionName =
                                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion) as LxmString

                        // Increase the index.
                        val union = getUnion(analyzer, node, unionName.primitive).dereference(analyzer.memory,
                                toWrite = true) as LxmPatternUnion
                        union.increaseIndex(analyzer.memory)

                        // Remove LexemeUnion from the stack.
                        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeUnion)
                    }
                }

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Executes the pattern of the union.
     */
    private fun executePattern(analyzer: LexemAnalyzer, node: LexemePatternNode, unionName: LxmString,
            quantifier: LxmQuantifier? = null) {
        val union = getOrInitUnion(analyzer, node, unionName.primitive, quantifier)

        if (union.canHaveANextPattern(analyzer.memory)) {
            // On backwards skip.
            analyzer.freezeMemoryCopy(node, signalEndBadPatternContent)

            // Add union name to the stack.
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeUnion, unionName)

            return analyzer.nextNode(node.patternContent)
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Gets the union.
     */
    private fun getUnion(analyzer: LexemAnalyzer, node: LexemePatternNode, unionName: String): LxmReference {
        val unions = AnalyzerCommons.getCurrentContextElement<LxmObject>(analyzer.memory,
                AnalyzerCommons.Identifiers.HiddenPatternUnions, toWrite = false)
        return unions.getPropertyValue(analyzer.memory, unionName) as LxmReference
    }

    /**
     * Gets or initializes a union from a quantifier.
     */
    private fun getOrInitUnion(analyzer: LexemAnalyzer, node: LexemePatternNode, unionName: String,
            quantifier: LxmQuantifier?): LxmPatternUnion {
        val unions = AnalyzerCommons.getCurrentContextElement<LxmObject>(analyzer.memory,
                AnalyzerCommons.Identifiers.HiddenPatternUnions, toWrite = true)
        var union = unions.getPropertyValue(analyzer.memory, unionName)?.dereference(analyzer.memory,
                toWrite = false) as? LxmPatternUnion

        if (union == null) {
            if (quantifier != null) {
                // Create the union.
                union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)

                unions.setProperty(analyzer.memory, unionName, union)
            } else {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.PatternUnionWithoutQuantifier,
                        "The union called '$unionName' cannot be initialized if there is no quantifier") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                    }
                    addNote(Consts.Logger.hintTitle, "Add a quantifier to this pattern")
                }
            }
        } else if (quantifier != null) {
            // Check the quantifier of the union match with the current one.
            if (!union.quantifierIsEqualsTo(quantifier)) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.PatternUnionAlreadyExists,
                        "The union called '$unionName' already exist with other bounds. Previous: ${union.quantifier}, Current: $quantifier") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                    }
                    addSourceCode(fullText) {
                        title = Consts.Logger.hintTitle
                        highlightSection(node.unionName!!.from.position(), node.unionName!!.to.position() - 1)
                        message = "Change the union name for another one."
                    }
                }
            }
        }

        return union
    }
}
