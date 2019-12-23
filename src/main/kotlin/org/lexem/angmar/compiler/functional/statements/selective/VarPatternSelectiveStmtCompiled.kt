package org.lexem.angmar.compiler.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.selective.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.selective.*

/**
 * Compiler for [VarPatternSelectiveStmtNode].
 */
internal class VarPatternSelectiveStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: VarPatternSelectiveStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var identifier: CompiledNode
    var conditional: CompiledNode? = null
    var isConstant = false
    var mustBeIdentifier = true

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            VarPatternSelectiveStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: VarPatternSelectiveStmtNode): CompiledNode {
            val result = VarPatternSelectiveStmtCompiled(parent, parentSignal, node)
            result.identifier = node.identifier.compile(result, VarPatternSelectiveStmtAnalyzer.signalEndIdentifier)
            result.conditional = node.conditional?.compile(result, VarPatternSelectiveStmtAnalyzer.signalEndConditional)
            result.isConstant = node.isConstant
            result.mustBeIdentifier = node.identifier !is DestructuringStmtNode

            return result
        }
    }
}
