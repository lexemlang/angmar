package org.lexem.angmar.compiler.descriptive.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.statements.*

/**
 * Compiler for [OnBackBlockStmtNode].
 */
internal class OnBackBlockStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: OnBackBlockStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    var parameters: CompiledNode? = null
    lateinit var block: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            OnBackBlockStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: OnBackBlockStmtNode): OnBackBlockStmtCompiled {
            val result = OnBackBlockStmtCompiled(parent, parentSignal, node)
            result.parameters = node.parameters?.compile(result, OnBackBlockStmtAnalyzer.signalEndParameters)
            result.block = node.block.compile(result, OnBackBlockStmtAnalyzer.signalEndBlock)

            return result
        }
    }
}
