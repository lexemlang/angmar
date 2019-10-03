package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for list literals.
 */
internal object ListAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ListNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add the new list.
                val list = LxmList()
                val listRef = analyzer.memory.add(list)
                analyzer.memory.pushStack(listRef)

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }

                if (node.isConstant) {
                    list.makeConstant(analyzer.memory)
                }
            }
            in signalEndFirstElement until signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Add the value to the list.
                val value = analyzer.memory.popStack()
                val listRef = analyzer.memory.popStack()
                val list = listRef.dereference(analyzer.memory) as LxmList

                list.addCell(analyzer.memory, value)

                analyzer.memory.pushStackIgnoringReferenceCount(listRef)

                // Process the next node.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                if (node.isConstant) {
                    list.makeConstant(analyzer.memory)
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
