package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [ConditionalStmtNode].
 */
internal class ConditionalStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: ConditionalStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var condition: CompiledNode
    lateinit var thenBlock: CompiledNode
    var elseBlock: CompiledNode? = null
    var isUnless = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ConditionalStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ConditionalStmtNode): CompiledNode {
            val result = ConditionalStmtCompiled(parent, parentSignal, node)
            result.condition = node.condition.compile(result, ConditionalStmtAnalyzer.signalEndCondition)
            result.thenBlock = node.thenBlock.compile(result, ConditionalStmtAnalyzer.signalEndThenBlock)
            result.elseBlock = node.elseBlock?.compile(result, ConditionalStmtAnalyzer.signalEndElseBlock)
            result.isUnless = node.isUnless

            return result
        }
    }
}
