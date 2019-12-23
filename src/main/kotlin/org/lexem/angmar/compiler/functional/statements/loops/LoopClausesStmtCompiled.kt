package org.lexem.angmar.compiler.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.loops.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.loops.*

/**
 * Compiler for [LoopClausesStmtNode].
 */
internal class LoopClausesStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: LoopClausesStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    var lastBlock: CompiledNode? = null
    var elseBlock: CompiledNode? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            LoopClausesStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: LoopClausesStmtNode): LoopClausesStmtCompiled {
            val result = LoopClausesStmtCompiled(parent, parentSignal, node)
            result.lastBlock = node.lastBlock?.compile(result, LoopClausesStmtAnalyzer.signalEndClause)
            result.elseBlock = node.elseBlock?.compile(result, LoopClausesStmtAnalyzer.signalEndClause)

            return result
        }
    }
}
