package org.lexem.angmar.compiler.functional.statements.controls

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.controls.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.controls.*

/**
 * Compiler for [ControlWithExpressionStmtNode].
 */
internal class ControlWithExpressionStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: ControlWithExpressionStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    var tag: CompiledNode? = null
    lateinit var expression: CompiledNode
    lateinit var keyword: String

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ControlWithExpressionStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ControlWithExpressionStmtNode): CompiledNode {
            val result = ControlWithExpressionStmtCompiled(parent, parentSignal, node)
            result.tag = node.tag?.compile(result, ControlWithExpressionStmtAnalyzer.signalEndTag)
            result.expression = node.expression.compile(result, ControlWithExpressionStmtAnalyzer.signalEndExpression)
            result.keyword = node.keyword

            return result
        }
    }
}
