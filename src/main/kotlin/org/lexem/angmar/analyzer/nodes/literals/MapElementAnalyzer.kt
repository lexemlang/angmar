package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.literals.*


/**
 * Analyzer for key-value pairs of map literals.
 */
internal object MapElementAnalyzer {
    const val signalEndKey = AnalyzerNodesCommons.signalStart + 1
    const val signalEndValue = signalEndKey + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: MapElementCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.key)
            }
            signalEndKey -> {
                // Move last to key.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                return analyzer.nextNode(node.value)
            }
            signalEndValue -> {
                // Add the value to the object.
                val value = analyzer.memory.getLastFromStack()
                val key = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Key)
                val map = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory, toWrite = true) as LxmMap

                map.setProperty(analyzer.memory, key, value)

                // Remove the key and value from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
