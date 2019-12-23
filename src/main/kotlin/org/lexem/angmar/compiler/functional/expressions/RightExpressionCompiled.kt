package org.lexem.angmar.compiler.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.functional.expressions.*

/**
 * Compiler for [RightExpressionNode].
 */
internal class RightExpressionCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: RightExpressionNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var expression: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            RightExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: RightExpressionNode): CompiledNode {
            val result = RightExpressionCompiled(parent, parentSignal, node)
            result.expression = node.expression.compile(result, RightExpressionAnalyzer.signalEndExpression)

            if (result.expression is ConstantCompiled) {
                return result.expression.linkTo(parent, parentSignal, node)
            }

            return result
        }
    }
}
