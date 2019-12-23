package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.literals.*


/**
 * Analyzer for list literals.
 */
internal object ListAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ListCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add the new list.
                val list = LxmList(analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, list)

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }

                if (node.isConstant) {
                    list.makeConstantAndNotWritable(analyzer.memory)
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
            in signalEndFirstElement..signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Add the value to the list.
                val value = analyzer.memory.getLastFromStack()
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory, toWrite = true) as LxmList

                list.addCell(analyzer.memory, value)
                analyzer.memory.removeLastFromStack()

                // Process the next node.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                if (node.isConstant) {
                    list.makeConstantAndNotWritable(analyzer.memory)
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
