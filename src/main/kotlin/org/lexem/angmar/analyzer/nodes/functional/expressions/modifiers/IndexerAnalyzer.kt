package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.setters.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.functional.expressions.modifiers.*


/**
 * Analyzer for indexers.
 */
internal object IndexerAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: IndexerCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Move Last to Accumulator in stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Accumulator)

                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                val index = analyzer.memory.getLastFromStack()
                val setter = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as LexemSetter
                val element = setter.dereference(analyzer.memory, toWrite = false)

                analyzer.memory.replaceLastStackCell(LxmIndexerSetter(analyzer.memory, element, index, node))

                // Remove Accumulator from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
