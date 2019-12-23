package org.lexem.angmar.compiler.functional.statements.controls

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.controls.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.controls.*

/**
 * Compiler for [ControlWithoutExpressionStmtNode].
 */
internal class ControlWithoutExpressionStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: ControlWithoutExpressionStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    var tag: CompiledNode? = null
    lateinit var keyword: String

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ControlWithoutExpressionStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ControlWithoutExpressionStmtNode): CompiledNode {
            val result = ControlWithoutExpressionStmtCompiled(parent, parentSignal, node)
            result.tag = node.tag?.compile(result, ControlWithoutExpressionStmtAnalyzer.signalEndTag)
            result.keyword = node.keyword

            return result
        }
    }
}
