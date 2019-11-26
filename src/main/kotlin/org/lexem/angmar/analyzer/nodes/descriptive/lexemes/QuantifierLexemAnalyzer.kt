package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*


/**
 * Analyzer for quantifier lexemes.
 */
internal object QuantifierLexemAnalyzer {
    const val endQuantifierSignal = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: QuantifierLexemeNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.quantifier != null) {
                    return analyzer.nextNode(node.quantifier)
                }

                // Create final quantifier
                val quantifier = when (node.abbreviation) {
                    QuantifierLexemeNode.lazyAbbreviation[0] -> when (node.modifier) {
                        QuantifierLexemeNode.lazyAbbreviation[0] -> LxmQuantifier.LazyZeroOrOne
                        QuantifierLexemeNode.atomicGreedyAbbreviations[0] -> LxmQuantifier.AtomicGreedyZeroOrOne
                        QuantifierLexemeNode.atomicLazyAbbreviations[0] -> LxmQuantifier.AtomicLazyZeroOrOne
                        else -> LxmQuantifier.GreedyZeroOrOne
                    }
                    QuantifierLexemeNode.atomicGreedyAbbreviations[0] -> when (node.modifier) {
                        QuantifierLexemeNode.lazyAbbreviation[0] -> LxmQuantifier.LazyOneOrMore
                        QuantifierLexemeNode.atomicGreedyAbbreviations[0] -> LxmQuantifier.AtomicGreedyOneOrMore
                        QuantifierLexemeNode.atomicLazyAbbreviations[0] -> LxmQuantifier.AtomicLazyOneOrMore
                        else -> LxmQuantifier.GreedyOneOrMore
                    }
                    QuantifierLexemeNode.atomicLazyAbbreviations[0] -> when (node.modifier) {
                        QuantifierLexemeNode.lazyAbbreviation[0] -> LxmQuantifier.LazyZeroOrMore
                        QuantifierLexemeNode.atomicGreedyAbbreviations[0] -> LxmQuantifier.AtomicGreedyZeroOrMore
                        QuantifierLexemeNode.atomicLazyAbbreviations[0] -> LxmQuantifier.AtomicLazyZeroOrMore
                        else -> LxmQuantifier.GreedyZeroOrMore
                    }
                    else -> throw AngmarUnreachableException()
                }

                analyzer.memory.addToStackAsLast(quantifier)
            }
            endQuantifierSignal -> {
                // Get quantifier value.
                var quantifier = analyzer.memory.getLastFromStack() as LxmQuantifier

                // Create final quantifier
                quantifier = when (node.modifier) {
                    QuantifierLexemeNode.lazyAbbreviation[0] -> LxmQuantifier(quantifier, isLazy = true)
                    QuantifierLexemeNode.atomicGreedyAbbreviations[0] -> LxmQuantifier(quantifier, isAtomic = true)
                    QuantifierLexemeNode.atomicLazyAbbreviations[0] -> LxmQuantifier(quantifier, isLazy = true,
                            isAtomic = true)
                    else -> quantifier
                }

                analyzer.memory.replaceLastStackCell(quantifier)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
