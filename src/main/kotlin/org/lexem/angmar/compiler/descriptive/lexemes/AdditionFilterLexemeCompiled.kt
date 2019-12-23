package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [AdditionFilterLexemeNode].
 */
internal class AdditionFilterLexemeCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: AdditionFilterLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var selector: CompiledNode
    var nextAccess: CompiledNode? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AdditionFilterLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: AdditionFilterLexemeNode): AdditionFilterLexemeCompiled {
            val result = AdditionFilterLexemeCompiled(parent, parentSignal, node)
            result.selector = node.selector.compile(result, AdditionFilterLexemeAnalyzer.signalEndSelector)
            result.nextAccess = node.nextAccess?.compile(result, AdditionFilterLexemeAnalyzer.signalEndNextAccess)

            return result
        }
    }
}
