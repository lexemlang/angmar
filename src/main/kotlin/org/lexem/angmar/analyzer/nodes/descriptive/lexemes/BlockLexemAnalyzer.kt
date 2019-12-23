package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.utils.*


/**
 * Analyzer for block lexemes.
 */
internal object BlockLexemAnalyzer {
    const val signalEndInterval = AnalyzerNodesCommons.signalStart + 1
    const val signalEndPropertyPostfix = signalEndInterval + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: BlockLexemeCompiled) {
        val reader = analyzer.text as? ITextReader ?: throw AngmarException(
                "The block lexeme require that the analyzed content is a text reader.")

        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.isNegated) {
                    // Save the first index for atomic.
                    val memoryIndex = LxmBigNode(analyzer.memory.lastNode)

                    // On backwards skip.
                    analyzer.freezeMemoryCopy(node, AbsoluteAnchorLexemeAnalyzer.signalBadEnd)

                    // Put the index in the stack.
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LastNode, memoryIndex)
                }

                return analyzer.nextNode(node.interval)
            }
            signalEndInterval -> {
                val (reverse, insensible) = AnalyzerNodesCommons.resolveInlineProperties(analyzer, node.propertyPostfix)
                val isForward = analyzer.isForward() xor reverse

                val textPrimitive = analyzer.memory.getLastFromStack() as LxmInterval
                val interval = textPrimitive.primitive

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (!isForward) {
                    analyzer.text.back()
                }

                val chText = if (insensible) {
                    val chText = reader.currentChar()

                    if (chText == null || (!interval.contains(
                                    chText.toUnicodeLowercase().toInt()) && !interval.contains(
                                    chText.toUnicodeUppercase().toInt()))) {
                        if (!node.isNegated) {
                            return analyzer.initBacktracking()
                        }

                        // Restore the memory index of the start and continue.
                        val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                        analyzer.restoreMemoryCopy(index.node)

                        return analyzer.nextNode(node.parent, node.parentSignal)
                    }

                    chText
                } else {
                    val chText = reader.currentChar()

                    if (chText == null || !interval.contains(chText.toInt())) {
                        if (!node.isNegated) {
                            return analyzer.initBacktracking()
                        }

                        // Restore the memory index of the start and continue.
                        val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                        analyzer.restoreMemoryCopy(index.node)

                        return analyzer.nextNode(node.parent, node.parentSignal)
                    }

                    chText
                }

                if (node.isNegated) {
                    // Restore the memory index of the start and continue.
                    val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                    analyzer.restoreMemoryCopy(index.node)

                    return analyzer.initBacktracking()
                }

                if (isForward) {
                    analyzer.text.advance()
                    analyzer.memory.addToStackAsLast(LxmString.from("$chText"))
                } else {
                    analyzer.text.back()
                    analyzer.memory.addToStackAsLast(LxmString.from("$chText"))
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
