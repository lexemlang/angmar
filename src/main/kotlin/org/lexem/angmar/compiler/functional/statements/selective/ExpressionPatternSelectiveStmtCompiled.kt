package org.lexem.angmar.compiler.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.selective.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.selective.*

/**
 * Compiler for [ExpressionPatternSelectiveStmtNode].
 */
internal class ExpressionPatternSelectiveStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: ExpressionPatternSelectiveStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var expression: CompiledNode
    var conditional: CompiledNode? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ExpressionPatternSelectiveStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ExpressionPatternSelectiveStmtNode): CompiledNode {
            val result = ExpressionPatternSelectiveStmtCompiled(parent, parentSignal, node)
            result.expression =
                    node.expression.compile(result, ExpressionPatternSelectiveStmtAnalyzer.signalEndExpression)
            result.conditional =
                    node.conditional?.compile(result, ExpressionPatternSelectiveStmtAnalyzer.signalEndConditional)

            return result
        }
    }
}
