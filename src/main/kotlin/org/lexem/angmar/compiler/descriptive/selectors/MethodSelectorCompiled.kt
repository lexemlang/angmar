package org.lexem.angmar.compiler.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.selectors.*

/**
 * Compiler for [MethodSelectorNode].
 */
internal class MethodSelectorCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: MethodSelectorNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isNegated = false
    var argument: CompiledNode? = null
    lateinit var name: CompiledNode
    var isArgumentABlock = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            MethodSelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: MethodSelectorNode): CompiledNode {
            val result = MethodSelectorCompiled(parent, parentSignal, node)
            result.isNegated = node.isNegated
            result.argument = node.argument?.compile(result, MethodSelectorAnalyzer.signalEndArgument)
            result.name = node.name.compile(result, MethodSelectorAnalyzer.signalEndName)
            result.isArgumentABlock = node.argument is PropertyBlockSelectorNode

            return result
        }
    }
}
