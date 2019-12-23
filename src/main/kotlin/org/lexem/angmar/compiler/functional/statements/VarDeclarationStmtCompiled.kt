package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [VarDeclarationStmtNode].
 */
internal class VarDeclarationStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: VarDeclarationStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var identifier: CompiledNode
    lateinit var value: CompiledNode
    var isConstant = false
    var mustBeIdentifier = true

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            VarDeclarationStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: VarDeclarationStmtNode): CompiledNode {
            val result = VarDeclarationStmtCompiled(parent, parentSignal, node)
            result.identifier = node.identifier.compile(result, VarDeclarationStmtAnalyzer.signalEndIdentifier)
            result.value = node.value.compile(result, VarDeclarationStmtAnalyzer.signalEndValue)
            result.isConstant = node.isConstant
            result.mustBeIdentifier = node.identifier !is DestructuringStmtNode

            return result
        }
    }
}
