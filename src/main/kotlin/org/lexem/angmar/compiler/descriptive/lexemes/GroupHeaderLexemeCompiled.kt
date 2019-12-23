package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [GroupHeaderLexemeNode].
 */
internal class GroupHeaderLexemeCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: GroupHeaderLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var quantifier: CompiledNode? = null
    var identifier: CompiledNode? = null
    var propertyBlock: CompiledNode? = null
    var isFilterCode = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            GroupHeaderLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: GroupHeaderLexemeNode): GroupHeaderLexemeCompiled {
            val result = GroupHeaderLexemeCompiled(parent, parentSignal, node)
            result.quantifier = node.quantifier?.compile(result, GroupHeaderLexemAnalyzer.signalEndQuantifier)
            result.identifier = node.identifier?.compile(result, GroupHeaderLexemAnalyzer.signalEndIdentifier)
            result.propertyBlock = node.propertyBlock?.compile(result, GroupHeaderLexemAnalyzer.signalEndPropertyBlock)
            result.isFilterCode = node.isFilterCode

            return result
        }
    }
}
