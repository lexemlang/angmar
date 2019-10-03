package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for map literals.
 */
internal object MapAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: MapNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add the new map.
                val map = LxmMap(null)
                val mapRef = analyzer.memory.add(map)
                analyzer.memory.pushStack(mapRef)

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }

                if (node.isConstant) {
                    map.makeConstant()
                }
            }
            in signalEndFirstElement until signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Process the next node.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                if (node.isConstant) {
                    val mapRef = analyzer.memory.popStack()
                    val map = mapRef.dereference(analyzer.memory) as LxmMap

                    map.makeConstant()

                    analyzer.memory.pushStackIgnoringReferenceCount(mapRef)
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
