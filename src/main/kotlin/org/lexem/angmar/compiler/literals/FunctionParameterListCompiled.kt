package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [FunctionParameterListNode].
 */
internal class FunctionParameterListCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: FunctionParameterListNode) : CompiledNode(parent, parentSignal, parserNode) {
    val parameters = mutableListOf<CompiledNode>()
    var positionalSpread: CompiledNode? = null
    var namedSpread: CompiledNode? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionParameterListAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: FunctionParameterListNode): FunctionParameterListCompiled {
            val result = FunctionParameterListCompiled(parent, parentSignal, node)
            result.positionalSpread =
                    node.positionalSpread?.compile(result, FunctionParameterListAnalyzer.signalEndPositionalSpread)
            result.namedSpread = node.namedSpread?.compile(result, FunctionParameterListAnalyzer.signalEndNamedSpread)

            for (parameter in node.parameters) {
                val compiledParameter = parameter.compile(result,
                        result.parameters.size + FunctionParameterListAnalyzer.signalEndFirstParameter)
                result.parameters.add(compiledParameter)
            }

            return result
        }
    }
}
