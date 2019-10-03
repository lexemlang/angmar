package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for key-value pairs of map literals.
 */
internal object MapElementAnalyzer {
    const val signalEndKey = AnalyzerNodesCommons.signalStart + 1
    const val signalEndValue = signalEndKey + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: MapElementNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.key)
            }
            signalEndKey -> {
                return analyzer.nextNode(node.value)
            }
            signalEndValue -> {
                // Add the value to the object.
                val value = analyzer.memory.popStack()
                val key = analyzer.memory.popStack()
                val mapRef = analyzer.memory.popStack()
                val map = mapRef.dereference(analyzer.memory) as LxmMap

                map.setProperty(analyzer.memory, key, value)

                analyzer.memory.pushStackIgnoringReferenceCount(mapRef)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
