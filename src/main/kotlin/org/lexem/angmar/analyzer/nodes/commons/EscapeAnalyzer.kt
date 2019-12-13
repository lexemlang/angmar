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
                val char = when (node.value) {
                    "t" -> '\t'.toInt()
                    "n" -> '\n'.toInt()
                    "r" -> '\r'.toInt()
                    else -> node.value[0].toInt()
                }

                analyzer.memory.addToStackAsLast(LxmInteger.from(char))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
