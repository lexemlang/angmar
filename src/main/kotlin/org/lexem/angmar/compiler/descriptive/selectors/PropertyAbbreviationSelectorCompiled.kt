package org.lexem.angmar.compiler.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.selectors.*

/**
 * Compiler for [PropertyAbbreviationSelectorNode].
 */
internal class PropertyAbbreviationSelectorCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: PropertyAbbreviationSelectorNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isNegated = false
    var isAddition = false
    var isAtIdentifier = false
    var propertyBlock: CompiledNode? = null
    lateinit var name: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PropertyAbbreviationSelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: PropertyAbbreviationSelectorNode): CompiledNode {
            val result = PropertyAbbreviationSelectorCompiled(parent, parentSignal, node)
            result.isNegated = node.isNegated
            result.isAddition = node.isAddition
            result.isAtIdentifier = node.isAtIdentifier
            result.propertyBlock =
                    node.propertyBlock?.compile(result, PropertyAbbreviationSelectorAnalyzer.signalEndPropertyBlock)
            result.name = node.name.compile(result, PropertyAbbreviationSelectorAnalyzer.signalEndName)

            return result
        }
    }
}
