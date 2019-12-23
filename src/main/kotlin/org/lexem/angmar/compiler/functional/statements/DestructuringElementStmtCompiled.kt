package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [DestructuringElementStmtNode].
 */
internal class DestructuringElementStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: DestructuringElementStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var alias: CompiledNode
    var original: CompiledNode? = null
    var isConstant = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            DestructuringElementStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: DestructuringElementStmtNode): DestructuringElementStmtCompiled {
            val result = DestructuringElementStmtCompiled(parent, parentSignal, node)
            result.alias = node.alias.compile(result, DestructuringElementStmtAnalyzer.signalEndAlias)
            result.original = node.original?.compile(result, DestructuringElementStmtAnalyzer.signalEndOriginal)
            result.isConstant = node.isConstant

            return result
        }
    }
}
