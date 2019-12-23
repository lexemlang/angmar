package org.lexem.angmar.compiler.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.expressions.*

/**
 * Compiler for [AssignExpressionNode].
 */
internal class AssignExpressionCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: AssignExpressionNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var left: CompiledNode
    lateinit var right: CompiledNode
    var operator = ""

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AssignExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: AssignExpressionNode): CompiledNode {
            val result = AssignExpressionCompiled(parent, parentSignal, node)
            result.left = node.left.compile(result, AssignExpressionAnalyzer.signalEndLeft)
            result.right = node.right.compile(result, AssignExpressionAnalyzer.signalEndRight)
            result.operator = node.operator.operator

            return result
        }
    }
}
