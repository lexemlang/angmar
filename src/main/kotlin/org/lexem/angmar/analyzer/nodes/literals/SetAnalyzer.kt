package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.literals.*


/**
 * Analyzer for set literals.
 */
internal object SetAnalyzer {
    const val signalEndList = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: SetCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.list)
            }
            signalEndList -> {
                val list = analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = false) as LxmList
                val set = LxmSet(analyzer.memory)

                for (i in list.getAllCells()) {
                    set.addValue(analyzer.memory, i)
                }

                analyzer.memory.replaceLastStackCell(set)

                if (list.isConstant) {
                    set.makeConstant()
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
