package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for unescaped string literals.
 */
internal object UnescapedStringAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: UnescapedStringNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add the first string.
                analyzer.memory.addToStackAsLast(LxmString.from(node.text))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
