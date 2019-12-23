package org.lexem.angmar.compiler.descriptive.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.statements.loops.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.loops.*
import org.lexem.angmar.parser.descriptive.statements.loops.*

/**
 * Compiler for [QuantifiedLoopStmtNode].
 */
internal class QuantifiedLoopStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: QuantifiedLoopStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var quantifier: CompiledNode
    lateinit var thenBlock: CompiledNode
    var index: CompiledNode? = null
    var lastClauses: LoopClausesStmtCompiled? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            QuantifiedLoopStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: QuantifiedLoopStmtNode): CompiledNode {
            val result = QuantifiedLoopStmtCompiled(parent, parentSignal, node)
            result.quantifier = node.quantifier.compile(result, QuantifiedLoopStmtAnalyzer.signalEndQuantifier)
            result.thenBlock = node.thenBlock.compile(result, QuantifiedLoopStmtAnalyzer.signalEndThenBlock)
            result.index = node.index?.compile(result, QuantifiedLoopStmtAnalyzer.signalEndIndex)
            result.lastClauses = node.lastClauses?.compile(result, QuantifiedLoopStmtAnalyzer.signalEndLastClause)

            return result
        }
    }
}
