package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [FunctionStmtNode].
 */
internal class FunctionalStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: FunctionStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var name: CompiledNode
    lateinit var block: CompiledNode
    var parameterList: FunctionParameterListCompiled? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: FunctionStmtNode): FunctionalStmtCompiled {
            val result = FunctionalStmtCompiled(parent, parentSignal, node)
            result.name = node.name.compile(result, FunctionStmtAnalyzer.signalEndName)
            result.block = node.block.compile(result, FunctionStmtAnalyzer.signalEndBlock)
            result.parameterList = node.parameterList?.compile(result, FunctionStmtAnalyzer.signalEndParameterList)

            return result
        }
    }
}
