package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [FilterLexemeNode].
 */
internal class FilterLexemeCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: FilterLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var selector: CompiledNode
    var nextAccess: CompiledNode? = null
    var isNegated = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FilterLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: FilterLexemeNode): FilterLexemeCompiled {
            val result = FilterLexemeCompiled(parent, parentSignal, node)
            result.selector = node.selector.compile(result, FilterLexemeAnalyzer.signalEndSelector)
            result.nextAccess = node.nextAccess?.compile(result, FilterLexemeAnalyzer.signalEndNextAccess)
            result.isNegated = node.isNegated

            return result
        }
    }
}
