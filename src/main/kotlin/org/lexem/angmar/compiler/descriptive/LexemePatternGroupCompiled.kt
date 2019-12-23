package org.lexem.angmar.compiler.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.*

/**
 * Compiler for [LexemePatternGroupNode].
 */
internal class LexemePatternGroupCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: LexemePatternGroupNode) : CompiledNode(parent, parentSignal, parserNode) {
    var type = LexemePatternNode.Companion.PatternType.Alternative
    var quantifier: CompiledNode? = null
    val patterns = mutableListOf<CompiledNode?>()
    var initialQuantifier: LxmQuantifier? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            LexemePatternGroupAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: LexemePatternGroupNode): CompiledNode {
            val result = LexemePatternGroupCompiled(parent, parentSignal, node)
            result.type = node.type
            result.quantifier = node.quantifier?.compile(result, LexemePatternGroupAnalyzer.signalEndQuantifier)

            var quantifier = when (result.type) {
                LexemePatternNode.Companion.PatternType.Additive -> LxmQuantifier.AdditivePattern
                LexemePatternNode.Companion.PatternType.Selective -> LxmQuantifier.SelectivePattern
                LexemePatternNode.Companion.PatternType.Alternative -> LxmQuantifier.AlternativePattern
                LexemePatternNode.Companion.PatternType.Quantified -> {
                    val qtf = result.quantifier
                    if (qtf is ConstantCompiled) {
                        qtf.value as LxmQuantifier
                    } else {
                        null
                    }
                }
                else -> throw AngmarUnreachableException()
            }

            // Compile
            if (quantifier == null) {
                for (pattern in node.patterns) {
                    val compiledPattern = pattern?.compile(result,
                            result.patterns.size + LexemePatternGroupAnalyzer.signalEndFirstPattern)

                    when (compiledPattern) {
                        is NoOperationCompiled -> {
                            result.patterns.add(null)
                        }
                        else -> result.patterns.add(compiledPattern)
                    }
                }
            } else {
                // Quantifier {0} returns noop.
                if (quantifier.max == 0) {
                    return NoOperationCompiled(parent, parentSignal, node)
                }

                // Return initBack if the minimum is greater than the size.
                if (quantifier.min > node.patterns.size) {
                    return InitBacktrackingCompiled(parent, parentSignal, node)
                }

                // Simplify the quantifier if it is infinite.
                if (quantifier.isInfinite) {
                    quantifier = LxmQuantifier(quantifier.min, node.patterns.size)
                }

                var nullCount = 0
                for (pattern in node.patterns) {
                    val compiledPattern = pattern?.compile(result,
                            result.patterns.size + LexemePatternGroupAnalyzer.signalEndFirstPattern)

                    when (compiledPattern) {
                        null, is NoOperationCompiled -> {
                            // Add nulls until reach the maximum.
                            if (nullCount < quantifier.max) {
                                result.patterns.add(null)
                            }

                            nullCount += 1
                        }
                        else -> result.patterns.add(compiledPattern)
                    }
                }
            }

            result.initialQuantifier = quantifier

            return result
        }
    }
}
