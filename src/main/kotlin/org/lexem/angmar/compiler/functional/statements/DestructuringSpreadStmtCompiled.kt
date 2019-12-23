package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [DestructuringSpreadStmtNode].
 */
internal class DestructuringSpreadStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: DestructuringSpreadStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var identifier: CompiledNode
    var isConstant = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            DestructuringSpreadStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: DestructuringSpreadStmtNode): DestructuringSpreadStmtCompiled {
            val result = DestructuringSpreadStmtCompiled(parent, parentSignal, node)
            result.identifier = node.identifier.compile(result, DestructuringSpreadStmtAnalyzer.signalEndIdentifier)
            result.isConstant = node.isConstant

            return result
        }
    }
}
