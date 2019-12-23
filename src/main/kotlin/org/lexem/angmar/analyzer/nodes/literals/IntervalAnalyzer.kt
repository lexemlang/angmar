package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.literals.*


/**
 * Analyzer for interval literals.
 */
internal object IntervalAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: IntervalCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.elements.isNotEmpty()) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

                    return analyzer.nextNode(node.elements.first())
                }

                if (node.reversed) {
                    analyzer.memory.addToStackAsLast(LxmInterval.Full)
                } else {
                    analyzer.memory.addToStackAsLast(LxmInterval.Empty)
                }
            }
            in signalEndFirstElement..signalEndFirstElement + node.elements.size -> {
                val position = (signal - ListAnalyzer.signalEndFirstElement) + 1

                // Process the next node.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)

                if (node.reversed) {
                    val interval = analyzer.memory.getLastFromStack() as LxmInterval
                    val result = !interval.primitive
                    analyzer.memory.replaceLastStackCell(LxmInterval.from(result))
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
