package org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*


/**
 * Analyzer for relative anchor lexemes.
 */
internal object RelativeAnchorLexemeAnalyzer {
    const val signalBadEnd = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFirstElement = signalBadEnd + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: RelativeAnchorLexemeNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                when (node.type) {
                    RelativeAnchorLexemeNode.RelativeAnchorType.StartText -> {
                        val isForward = analyzer.isForward()
                        val condition = if (!isForward) {
                            !analyzer.text.isEnd()
                        } else {
                            !analyzer.text.isStart()
                        }

                        if (condition xor node.isNegated) {
                            return analyzer.initBacktracking()
                        }
                    }
                    RelativeAnchorLexemeNode.RelativeAnchorType.EndText -> {
                        val isForward = analyzer.isForward()
                        val condition = if (isForward) {
                            !analyzer.text.isEnd()
                        } else {
                            !analyzer.text.isStart()
                        }

                        if (condition xor node.isNegated) {
                            return analyzer.initBacktracking()
                        }
                    }
                    else -> {
                        if (node.elements.isEmpty()) {
                            val reader = analyzer.text as? ITextReader ?: throw AngmarException(
                                    "The relative anchor line type require that the analyzed content is a text reader.")

                            when (node.type) {
                                RelativeAnchorLexemeNode.RelativeAnchorType.StartLine -> {
                                    val isForward = analyzer.isForward()
                                    val condition = if (!isForward) {
                                        !reader.isEnd() && reader.currentChar() !in WhitespaceNode.endOfLineChars
                                    } else {
                                        !reader.isStart() && reader.prevChar() !in WhitespaceNode.endOfLineChars
                                    }

                                    if (condition xor node.isNegated) {
                                        return analyzer.initBacktracking()
                                    }
                                }
                                RelativeAnchorLexemeNode.RelativeAnchorType.EndLine -> {
                                    val isForward = analyzer.isForward()
                                    val condition = if (isForward) {
                                        !reader.isEnd() && reader.currentChar() !in WhitespaceNode.endOfLineChars
                                    } else {
                                        !reader.isStart() && reader.prevChar() !in WhitespaceNode.endOfLineChars
                                    }

                                    if (condition xor node.isNegated) {
                                        return analyzer.initBacktracking()
                                    }
                                }
                                else -> throw AngmarUnreachableException()
                            }

                            return analyzer.nextNode(node.parent, node.parentSignal)
                        }

                        // With anchors.
                        if (node.isNegated) {
                            // Save the first index for atomic.
                            val memoryIndex = LxmBigNode(analyzer.memory.lastNode)

                            // On backwards skip.
                            analyzer.freezeMemoryCopy(node, signalBadEnd)

                            // Put the index in the stack.
                            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LastNode, memoryIndex)
                        }

                        if (node.type == RelativeAnchorLexemeNode.RelativeAnchorType.StartLine) {
                            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.AnchorIsStart, LxmLogic.True)
                        } else {
                            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.AnchorIsStart, LxmLogic.False)
                        }

                        return analyzer.nextNode(node.elements.first())
                    }
                }
            }
            in signalEndFirstElement until signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                val result = analyzer.memory.getLastFromStack() as LxmLogic

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Continue if the current has not matched.
                if (!result.primitive) {
                    // Process the next element.
                    if (position < node.elements.size) {
                        return analyzer.nextNode(node.elements[position])
                    }

                    // Remove AnchorIsStart from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.AnchorIsStart)

                    if (!node.isNegated) {
                        return analyzer.initBacktracking()
                    }

                    // Restore the memory index of the start and continue.
                    val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                    analyzer.restoreMemoryCopy(index.node)
                } else {
                    // Remove AnchorIsStart from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.AnchorIsStart)

                    if (node.isNegated) {
                        // Restore the memory index of the start and continue.
                        val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                        analyzer.restoreMemoryCopy(index.node)

                        return analyzer.initBacktracking()
                    }
                }
            }
            signalBadEnd -> {
                // Skip
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
