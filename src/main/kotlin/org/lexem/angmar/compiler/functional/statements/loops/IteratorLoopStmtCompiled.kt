package org.lexem.angmar.compiler.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.loops.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.loops.*

/**
 * Compiler for [IteratorLoopStmtNode].
 */
internal class IteratorLoopStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: IteratorLoopStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var variable: CompiledNode
    lateinit var condition: CompiledNode
    lateinit var thenBlock: CompiledNode
    var index: CompiledNode? = null
    var lastClauses: LoopClausesStmtCompiled? = null
    var mustBeIdentifier = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            IteratorLoopStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: IteratorLoopStmtNode): CompiledNode {
            val result = IteratorLoopStmtCompiled(parent, parentSignal, node)
            result.variable = node.variable.compile(result, IteratorLoopStmtAnalyzer.signalEndVariable)
            result.condition = node.condition.compile(result, IteratorLoopStmtAnalyzer.signalEndCondition)
            result.thenBlock = node.thenBlock.compile(result, IteratorLoopStmtAnalyzer.signalEndThenBlock)
            result.index = node.index?.compile(result, IteratorLoopStmtAnalyzer.signalEndIndex)
            result.lastClauses = node.lastClauses?.compile(result, IteratorLoopStmtAnalyzer.signalEndLastClause)
            result.mustBeIdentifier = node.variable !is DestructuringStmtNode

            return result
        }
    }
}
