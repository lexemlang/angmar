package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [FunctionalExpressionStmtNode].
 */
internal class FunctionalExpressionStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: FunctionalExpressionStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var expression: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionalExpressionStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: FunctionalExpressionStmtNode): CompiledNode {
            val result = FunctionalExpressionStmtCompiled(parent, parentSignal, node)
            result.expression = node.expression.compile(result, FunctionalExpressionStmtAnalyzer.signalEndExpression)

            if (result.expression is ConstantCompiled) {
                return NoOperationCompiled(parent, parentSignal, node)
            }

            return result
        }
    }
}
