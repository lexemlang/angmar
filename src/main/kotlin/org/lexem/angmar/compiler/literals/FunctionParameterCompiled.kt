package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [FunctionParameterNode].
 */
internal class FunctionParameterCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: FunctionParameterNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var identifier: CompiledNode
    var expression: CompiledNode? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionParameterAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: FunctionParameterNode): FunctionParameterCompiled {
            val result = FunctionParameterCompiled(parent, parentSignal, node)
            result.identifier = node.identifier.compile(result, FunctionParameterAnalyzer.signalEndIdentifier)
            result.expression = node.expression?.compile(result, FunctionParameterAnalyzer.signalEndExpression)

            return result
        }
    }
}
