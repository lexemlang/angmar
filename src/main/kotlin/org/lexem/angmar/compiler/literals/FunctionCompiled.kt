package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [FunctionNode].
 */
internal class FunctionCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: FunctionNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var block: CompiledNode
    var parameterList: FunctionParameterListCompiled? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = FunctionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: FunctionNode): FunctionCompiled {
            val result = FunctionCompiled(parent, parentSignal, node)
            result.block = node.block.compile(result, FunctionAnalyzer.signalEndBlock)
            result.parameterList = node.parameterList?.compile(result, FunctionAnalyzer.signalEndParameterList)

            return result
        }
    }
}
