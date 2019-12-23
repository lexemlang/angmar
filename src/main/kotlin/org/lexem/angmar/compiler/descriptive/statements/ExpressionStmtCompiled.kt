package org.lexem.angmar.compiler.descriptive.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.parser.descriptive.statements.*

/**
 * Compiler for [ExpressionStmtNode].
 */
internal class ExpressionStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: ExpressionStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var name: CompiledNode
    lateinit var block: CompiledNode
    var properties: CompiledNode? = null
    var parameterList: FunctionParameterListCompiled? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ExpressionStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ExpressionStmtNode): ExpressionStmtCompiled {
            val result = ExpressionStmtCompiled(parent, parentSignal, node)
            result.name = node.name.compile(result, ExpressionStmtAnalyzer.signalEndName)
            result.block = node.block.compile(result, ExpressionStmtAnalyzer.signalEndBlock)
            result.properties = node.properties?.compile(result, ExpressionStmtAnalyzer.signalEndProperties)
            result.parameterList = node.parameterList?.compile(result, ExpressionStmtAnalyzer.signalEndParameterList)

            return result
        }
    }
}
