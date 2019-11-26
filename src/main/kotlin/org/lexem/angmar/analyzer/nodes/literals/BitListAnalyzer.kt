package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
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
                if (node.elements.isNotEmpty()) {
                    // Empty bitlist.
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmBitList.Empty)

                    return analyzer.nextNode(node.elements[0])
                }

                // Empty bitlist.
                analyzer.memory.addToStackAsLast(LxmBitList.Empty)
            }
            in signalEndFirstElement until signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Evaluate the next operand if it exist.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
