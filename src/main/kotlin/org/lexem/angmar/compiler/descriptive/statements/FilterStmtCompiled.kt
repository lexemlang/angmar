package org.lexem.angmar.compiler.descriptive.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.parser.descriptive.statements.*

/**
 * Compiler for [FilterStmtNode].
 */
internal class FilterStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: FilterStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var name: CompiledNode
    lateinit var block: CompiledNode
    var properties: CompiledNode? = null
    var parameterList: FunctionParameterListCompiled? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = FilterStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: FilterStmtNode): FilterStmtCompiled {
            val result = FilterStmtCompiled(parent, parentSignal, node)
            result.name = node.name.compile(result, FilterStmtAnalyzer.signalEndName)
            result.block = node.block.compile(result, FilterStmtAnalyzer.signalEndBlock)
            result.properties = node.properties?.compile(result, FilterStmtAnalyzer.signalEndProperties)
            result.parameterList = node.parameterList?.compile(result, FilterStmtAnalyzer.signalEndParameterList)

            return result
        }
    }
}
