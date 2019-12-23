package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.utils.*


/**
 * Analyzer for text lexemes.
 */
internal object TextLexemAnalyzer {
    const val signalEndText = AnalyzerNodesCommons.signalStart + 1
    private const val signalBadEnd = signalEndText + 1
    const val signalEndPropertyPostfix = signalBadEnd + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: TextLexemeCompiled) {
        val reader = analyzer.text as? ITextReader ?: throw AngmarException(
                "The text lexemes require the analyzed content is a text reader.")

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

                return analyzer.nextNode(node.text)
            }
            signalEndText -> {
                val (reverse, insensible) = AnalyzerNodesCommons.resolveInlineProperties(analyzer, node.propertyPostfix)
                val isForward = analyzer.isForward() xor reverse

                val textPrimitive = analyzer.memory.getLastFromStack() as LxmString
                var text = textPrimitive.primitive

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (!isForward) {
                    analyzer.text.back()
                }

                if (insensible) {
                    text = text.toUnicodeLowercase()
                }

                val finalText = StringBuilder()
                for (ch in text) {
                    var chText = reader.currentChar()
                    finalText.append(chText)

                    if (insensible) {
                        chText = chText?.toUnicodeLowercase()
                    }

                    if (chText == null || chText != ch) {
                        if (!node.isNegated) {
                            return analyzer.initBacktracking()
                        }

                        // Restore the memory index of the start and continue.
                        val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                        analyzer.restoreMemoryCopy(index.node)

                        return analyzer.nextNode(node.parent, node.parentSignal)
                    }

                    if (isForward) {
                        analyzer.text.advance()
                    } else {
                        analyzer.text.back()
                    }
                }

                if (node.isNegated) {
                    // Restore the memory index of the start and continue.
                    val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                    analyzer.restoreMemoryCopy(index.node)

                    return analyzer.initBacktracking()
                }

                if (isForward) {
                    analyzer.memory.addToStackAsLast(LxmString.from(finalText.toString()))
                } else {
                    analyzer.memory.addToStackAsLast(LxmString.from(finalText.reverse().toString()))
                }
            }
            signalBadEnd -> {
                // Skip
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
