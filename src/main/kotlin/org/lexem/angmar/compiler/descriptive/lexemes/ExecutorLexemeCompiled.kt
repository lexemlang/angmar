package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [ExecutorLexemeNode].
 */
internal class ExecutorLexemeCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: ExecutorLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    val expressions = mutableListOf<CompiledNode>()
    var isConditional = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ExecutorLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ExecutorLexemeNode): CompiledNode {
            val result = ExecutorLexemeCompiled(parent, parentSignal, node)
            result.isConditional = node.isConditional

            var lastConstant: LexemPrimitive? = null
            for (expression in node.expressions) {
                val compiledExpression = expression.compile(result,
                        result.expressions.size + ExecutorLexemeAnalyzer.signalEndFirstExpression)

                // Add only not constant expressions.
                if (compiledExpression is ConstantCompiled) {
                    lastConstant = compiledExpression.value
                } else {
                    result.expressions.add(compiledExpression)
                    lastConstant = null
                }
            }

            // All constant.
            if (result.expressions.isEmpty()) {
                return if (result.isConditional) {
                    val isTruthy = RelationalFunctions.isTruthy(lastConstant!!)

                    if (isTruthy) {
                        ConstantCompiled(parent, parentSignal, node, lastConstant)
                    } else {
                        InitBacktrackingCompiled(parent, parentSignal, node)
                    }
                } else {
                    ConstantCompiled(parent, parentSignal, node, lastConstant!!)
                }
            }

            if (result.isConditional && lastConstant != null) {
                result.expressions.add(ConstantCompiled(result,
                        result.expressions.size + ExecutorLexemeAnalyzer.signalEndFirstExpression, node, lastConstant))
            }

            return result
        }
    }
}
