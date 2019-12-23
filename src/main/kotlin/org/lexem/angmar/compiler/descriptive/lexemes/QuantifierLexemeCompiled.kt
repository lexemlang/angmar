package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [QuantifierLexemeNode].
 */
internal class QuantifierLexemeCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: QuantifierLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var quantifier: CompiledNode
    var isLazy = false
    var isAtomic = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            QuantifierLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: QuantifierLexemeNode): CompiledNode {
            val result = QuantifierLexemeCompiled(parent, parentSignal, node)
            val quantifier = node.quantifier?.compile(result, QuantifierLexemAnalyzer.signalEndQuantifier)

            if (quantifier == null) {
                val quantifierValue = when (node.abbreviation) {
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

                return ConstantCompiled(parent, parentSignal, node, quantifierValue)
            }

            result.quantifier = quantifier

            when (node.modifier) {
                QuantifierLexemeNode.lazyAbbreviation[0] -> {
                    result.isLazy = true
                }
                QuantifierLexemeNode.atomicGreedyAbbreviations[0] -> {
                    result.isAtomic = true
                }
                QuantifierLexemeNode.atomicLazyAbbreviations[0] -> {
                    result.isLazy = true
                    result.isAtomic = true
                }
            }

            return result
        }
    }
}
