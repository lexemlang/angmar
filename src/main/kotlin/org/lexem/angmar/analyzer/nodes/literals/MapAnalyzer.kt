package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.literals.*


/**
 * Analyzer for map literals.
 */
internal object MapAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: MapCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add the new map.
                val map = LxmMap(analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, map)

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }

                if (node.isConstant) {
                    map.makeConstant(analyzer.memory)
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
            in signalEndFirstElement..signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Process the next node.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                if (node.isConstant) {
                    val map = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                            analyzer.memory, toWrite = true) as LxmMap

                    map.makeConstant(analyzer.memory)
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
