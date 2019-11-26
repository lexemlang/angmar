package org.lexem.angmar.analyzer.nodes.commons

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.commons.*


/**
 * Analyzer for escaped characters.
 */
internal object EscapeAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: EscapeNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                val prevValue = analyzer.memory.getLastFromStack() as LxmString

                val char = when (node.value) {
                    "t" -> "\t"
                    "n" -> "\n"
                    "r" -> "\r"
                    else -> node.value
                }

                analyzer.memory.replaceLastStackCell(LxmString.from(prevValue.primitive + char))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
