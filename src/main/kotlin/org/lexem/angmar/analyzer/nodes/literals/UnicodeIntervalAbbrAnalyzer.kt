package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for abbreviated unicode interval literals.
 */
internal object UnicodeIntervalAbbrAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: UnicodeIntervalAbbrNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                analyzer.memory.pushStack(LxmInterval.Empty)

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }
            }
            in signalEndFirstElement until signalEndFirstElement + node.elements.size -> {
                val position = (signal - ListAnalyzer.signalEndFirstElement) + 1

                // Process the next node.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }
            }
        }

        if (node.reversed) {
            val interval = analyzer.memory.popStack() as LxmInterval
            val result = !interval.primitive
            analyzer.memory.pushStack(LxmInterval.from(result))
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
