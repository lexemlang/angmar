package org.lexem.angmar.compiler.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.selectors.*

/**
 * Compiler for [PropertyBlockSelectorNode].
 */
internal class PropertyBlockSelectorCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: PropertyBlockSelectorNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isAddition = false
    var identifier: CompiledNode? = null
    lateinit var condition: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PropertyBlockSelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: PropertyBlockSelectorNode): CompiledNode {
            val result = PropertyBlockSelectorCompiled(parent, parentSignal, node)
            result.isAddition = node.isAddition
            result.identifier = node.identifier?.compile(result, PropertyBlockSelectorAnalyzer.signalEndIdentifier)
            result.condition = node.condition.compile(result, PropertyBlockSelectorAnalyzer.signalEndCondition)

            return result
        }
    }
}
