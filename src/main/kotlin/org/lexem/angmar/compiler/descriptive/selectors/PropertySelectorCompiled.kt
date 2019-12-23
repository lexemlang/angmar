package org.lexem.angmar.compiler.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.selectors.*

/**
 * Compiler for [PropertySelectorNode].
 */
internal class PropertySelectorCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: PropertySelectorNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isAddition = false
    val properties = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PropertySelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: PropertySelectorNode): CompiledNode {
            val result = PropertySelectorCompiled(parent, parentSignal, node)
            result.isAddition = node.isAddition

            for (property in node.properties) {
                val compiledProperty = property.compile(result,
                        result.properties.size + PropertySelectorAnalyzer.signalEndFirstProperty)

                result.properties.add(compiledProperty)
            }

            return result
        }
    }
}
