package org.lexem.angmar.compiler.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.selectors.*

/**
 * Compiler for [SelectorNode].
 */
internal class SelectorCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: SelectorNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isAddition = false
    var name: CompiledNode? = null
    val properties = mutableListOf<CompiledNode>()
    val methods = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = SelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: SelectorNode): CompiledNode {
            val result = SelectorCompiled(parent, parentSignal, node)
            result.isAddition = node.isAddition
            result.name = node.name?.compile(result, SelectorAnalyzer.signalEndName)

            for (property in node.properties) {
                val compiledProperty =
                        property.compile(result, result.properties.size + SelectorAnalyzer.signalEndFirstProperty)

                result.properties.add(compiledProperty)
            }

            for (method in node.methods) {
                val compiledMethod = method.compile(result,
                        result.properties.size + result.methods.size + SelectorAnalyzer.signalEndFirstProperty)

                result.methods.add(compiledMethod)
            }

            return result
        }
    }
}
