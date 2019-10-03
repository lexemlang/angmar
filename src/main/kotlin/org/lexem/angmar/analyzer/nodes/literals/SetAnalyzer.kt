package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for set literals.
 */
internal object SetAnalyzer {
    const val signalEndList = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: SetNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.list)
            }
            signalEndList -> {
                val listRef = analyzer.memory.popStack() as LxmReference
                val list = listRef.dereference(analyzer.memory) as LxmList
                val set = LxmSet(null)
                val setRef = analyzer.memory.add(set)

                for (i in list.getAllCells()) {
                    set.addValue(analyzer.memory, i)
                }

                analyzer.memory.pushStack(setRef)

                if (node.list.isConstant) {
                    set.makeConstant(analyzer.memory)
                }

                // Remove the list.
                listRef.decreaseReferenceCount(analyzer.memory)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
