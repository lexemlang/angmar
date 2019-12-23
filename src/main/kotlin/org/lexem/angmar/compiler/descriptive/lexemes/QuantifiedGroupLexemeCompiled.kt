package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [QuantifiedGroupLexemeNode].
 */
internal class QuantifiedGroupLexemeCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: QuantifiedGroupLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var mainModifier: CompiledNode? = null
    val patterns = mutableListOf<CompiledNode>()
    val modifiers = mutableListOf<CompiledNode?>()
    var isNegated = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            QuantifiedGroupLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: QuantifiedGroupLexemeNode): QuantifiedGroupLexemeCompiled {
            val result = QuantifiedGroupLexemeCompiled(parent, parentSignal, node)
            result.mainModifier = node.mainModifier?.compile(result, QuantifiedGroupLexemAnalyzer.signalEndMainModifier)
            result.isNegated = node.isNegated

            for (pattern in node.patterns) {
                val compiledPatterns = pattern.compile(result,
                        result.patterns.size + QuantifiedGroupLexemAnalyzer.signalEndFirstPattern)

                result.patterns.add(compiledPatterns)
            }

            for (modifier in node.modifiers) {
                val compiledModifiers = modifier?.compile(result,
                        result.patterns.size + result.modifiers.size + QuantifiedGroupLexemAnalyzer.signalEndFirstPattern)

                result.modifiers.add(compiledModifiers)
            }

            return result
        }
    }
}
