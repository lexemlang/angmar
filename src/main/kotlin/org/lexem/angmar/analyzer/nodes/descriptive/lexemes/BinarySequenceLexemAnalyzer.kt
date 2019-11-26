package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.parser.descriptive.lexemes.*


/**
 * Analyzer for binary sequence lexemes.
 */
internal object BinarySequenceLexemAnalyzer {
    const val signalEndBitlist = AnalyzerNodesCommons.signalStart + 1
    private const val signalBadEnd = signalEndBitlist + 1
    const val signalEndPropertyPostfix = signalBadEnd + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: BinarySequenceLexemeNode) {
        val reader = analyzer.text as? IBinaryReader ?: throw AngmarException(
                "The binary sequence lexemes require the analyzed content is a binary reader.")

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

                return analyzer.nextNode(node.bitlist)
            }
            signalEndBitlist -> {
                val (reverse, _) = AnalyzerNodesCommons.resolveInlineProperties(analyzer, node.propertyPostfix)
                val isForward = analyzer.isForward() xor reverse

                val bitSetPrimitive = analyzer.memory.getLastFromStack() as LxmBitList
                val bitSet = bitSetPrimitive.primitive

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (!isForward) {
                    analyzer.text.back()
                }

                for (bit in (0 until bitSetPrimitive.size).map { bitSet[it] }) {
                    val bitText = reader.currentBit()

                    if (bitText == null || bitText != bit) {
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

                analyzer.memory.addToStackAsLast(bitSetPrimitive)
            }
            signalBadEnd -> {
                // Skip
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
