package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for sub-intervals of unicode interval literals.
 */
internal object UnicodeIntervalSubIntervalAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: UnicodeIntervalSubIntervalNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Copy accumulator to parent.
                analyzer.memory.renameStackCell(AnalyzerCommons.Identifiers.Accumulator,
                        AnalyzerCommons.Identifiers.Parent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

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

        operate(analyzer, node)

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Operates the sub-interval.
     */
    private fun operate(analyzer: LexemAnalyzer, node: UnicodeIntervalSubIntervalNode) {
        val subInterval = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as LxmInterval
        val interval = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Parent) as LxmInterval

        var finalValue = if (node.reversed) {
            subInterval.primitive.unicodeNot()
        } else {
            subInterval.primitive
        }

        finalValue = when (node.operator) {
            IntervalSubIntervalNode.Operator.Add -> interval.primitive.plus(finalValue)
            IntervalSubIntervalNode.Operator.Sub -> interval.primitive.minus(finalValue)
            IntervalSubIntervalNode.Operator.Common -> interval.primitive.common(finalValue)
            IntervalSubIntervalNode.Operator.NotCommon -> interval.primitive.notCommon(finalValue)
        }

        // Remove Parent and set the Accumulator.
        analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(finalValue))
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Parent)
    }
}
