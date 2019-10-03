package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for object literals.
 */
internal object ObjectAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ObjectNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add the new object.
                val obj = LxmObject()
                val objRef = analyzer.memory.add(obj)
                analyzer.memory.pushStack(objRef)

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }

                if (node.isConstant) {
                    obj.makeConstant(analyzer.memory)
                }
            }
            in signalEndFirstElement until signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Process the next node.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                if (node.isConstant) {
                    val objRef = analyzer.memory.popStack()
                    val obj = objRef.dereference(analyzer.memory) as LxmObject

                    obj.makeConstant(analyzer.memory)

                    analyzer.memory.pushStackIgnoringReferenceCount(objRef)
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
