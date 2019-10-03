package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for Bitlist literals.
 */
internal object BitListAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: BitlistNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Empty bitlist.
                analyzer.memory.pushStack(LxmBitList.Empty)

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }
            }
            in signalEndFirstElement until signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Evaluate the next operand if it exist.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
