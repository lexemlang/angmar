package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.descriptive.lexemes.*


/**
 * Analyzer for quantifier lexemes.
 */
internal object QuantifierLexemAnalyzer {
    const val signalEndQuantifier = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: QuantifierLexemeCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.quantifier)
            }
            signalEndQuantifier -> {
                // Get quantifier value.
                var quantifier = analyzer.memory.getLastFromStack() as LxmQuantifier

                // Create final quantifier.
                quantifier = LxmQuantifier(quantifier, isLazy = node.isLazy, isAtomic = node.isAtomic)

                analyzer.memory.replaceLastStackCell(quantifier)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
