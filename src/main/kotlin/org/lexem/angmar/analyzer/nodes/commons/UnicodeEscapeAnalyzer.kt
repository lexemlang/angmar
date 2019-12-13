package org.lexem.angmar.analyzer.nodes.commons

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.commons.*


/**
 * Analyzer for unicode escapes.
 */
internal object UnicodeEscapeAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: UnicodeEscapeNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                analyzer.memory.addToStackAsLast(LxmInteger.from(node.value))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
