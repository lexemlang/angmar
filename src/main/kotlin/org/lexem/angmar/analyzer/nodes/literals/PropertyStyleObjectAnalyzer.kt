package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.literals.*


/**
 * Analyzer for property-style objects.
 */
internal object PropertyStyleObjectAnalyzer {
    const val signalEndBlock = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PropertyStyleObjectCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.block)
            }
            signalEndBlock -> {
                if (node.isConstant) {
                    val obj =
                            analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = true) as LxmObject

                    obj.makeConstant()
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
