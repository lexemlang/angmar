package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.setters.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Analyzer for indexers.
 */
internal object IndexerAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: IndexerNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                val index = analyzer.memory.popStack()
                val element = (analyzer.memory.popStack() as LexemSetter).resolve(analyzer.memory)

                analyzer.memory.pushStack(LxmIndexerSetter(element, index, node, analyzer.memory))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
