package org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*


/**
 * Analyzer for absolute anchor lexemes.
 */
internal object AbsoluteAnchorLexemeAnalyzer {
    const val signalBadEnd = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFirstElement = signalBadEnd + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AbsoluteAnchorLexemeNode) {
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

                return analyzer.nextNode(node.elements.first())
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

                    if (!node.isNegated) {
                        return analyzer.initBacktracking()
                    }

                    // Restore the memory index of the start and continue.
                    val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                    analyzer.restoreMemoryCopy(index.node)
                } else {
                    if (node.isNegated) {
                        // Restore the memory index of the start and continue.
                        val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                        analyzer.restoreMemoryCopy(index.node)

                        return analyzer.initBacktracking()
                    }
                }

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            signalBadEnd -> {
                // Skip
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
