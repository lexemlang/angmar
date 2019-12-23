package org.lexem.angmar.compiler.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.selectors.*

/**
 * Compiler for [NameSelectorNode].
 */
internal class NameSelectorCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: NameSelectorNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isNegated = false
    var isAddition = false
    val names = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            NameSelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: NameSelectorNode): CompiledNode {
            val result = NameSelectorCompiled(parent, parentSignal, node)
            result.isNegated = node.isNegated
            result.isAddition = node.isAddition

            for (name in node.names) {
                val compiledName = name.compile(result, result.names.size + NameSelectorAnalyzer.signalEndFirstName)

                result.names.add(compiledName)
            }

            return result
        }
    }
}
