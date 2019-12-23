package org.lexem.angmar.compiler.functional.expressions.binary

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.binary.*

/**
 * Compiler for [RelationalExpressionNode].
 */
internal class RelationalExpressionCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: RelationalExpressionNode) : CompiledNode(parent, parentSignal, parserNode) {
    val expressions = mutableListOf<CompiledNode>()
    var operators = mutableListOf<String>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            RelationalExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: RelationalExpressionNode): CompiledNode {
            val result = RelationalExpressionCompiled(parent, parentSignal, node)

            var last: LexemPrimitive? = null

            // Process the first.
            val compiledExpression =
                    node.expressions.first().compile(result, RelationalExpressionAnalyzer.signalEndFirstExpression)

            if (compiledExpression is ConstantCompiled) {
                last = compiledExpression.value
            } else {
                result.expressions.add(compiledExpression)
            }

            // Process the rest.
            for ((index, operator) in node.operators.withIndex()) {
                val compiledExpression = node.expressions[index + 1].compile(result,
                        result.expressions.size + RelationalExpressionAnalyzer.signalEndFirstExpression)

                if (compiledExpression is ConstantCompiled) {
                    if (last == null) {
                        last = compiledExpression.value
                        result.expressions.add(ConstantCompiled(result,
                                result.expressions.size + RelationalExpressionAnalyzer.signalEndFirstExpression, node,
                                last))
                        result.operators.add(operator)
                    } else {
                        if (operate(operator, last, compiledExpression.value, node)) {
                            last = compiledExpression.value
                        } else {
                            result.expressions.add(ConstantCompiled(result,
                                    result.expressions.size + RelationalExpressionAnalyzer.signalEndFirstExpression,
                                    node, last))
                            result.expressions.add(compiledExpression.linkTo(result,
                                    result.expressions.size + RelationalExpressionAnalyzer.signalEndFirstExpression,
                                    node))
                            result.operators.add(operator)

                            // Skip the rest values.
                            break
                        }
                    }
                } else {
                    if (last != null) {
                        result.expressions.add(ConstantCompiled(result,
                                result.expressions.size + RelationalExpressionAnalyzer.signalEndFirstExpression, node,
                                last))
                        result.expressions.add(compiledExpression.changeParentSignal(
                                result.expressions.size + ListAnalyzer.signalEndFirstElement))
                    } else {
                        result.expressions.add(compiledExpression)
                    }

                    result.operators.add(operator)

                    last = null
                }
            }

            // Constant.
            if (result.expressions.isEmpty()) {
                return ConstantCompiled(parent, parentSignal, node, LxmLogic.from(RelationalFunctions.isTruthy(last!!)))
            }

            return result
        }

        /**
         * Operates two values.
         */
        private fun operate(operator: String, left: LexemPrimitive, right: LexemPrimitive, node: ParserNode) =
                when (operator) {
                    RelationalExpressionNode.equalityOperator -> RelationalFunctions::identityEquals
                    RelationalExpressionNode.inequalityOperator -> RelationalFunctions::identityNotEquals
                    RelationalExpressionNode.identityOperator -> RelationalFunctions::identityEquals
                    RelationalExpressionNode.notIdentityOperator -> RelationalFunctions::identityNotEquals
                    RelationalExpressionNode.lowerThanOperator -> RelationalFunctions::lowerThan
                    RelationalExpressionNode.greaterThanOperator -> RelationalFunctions::greaterThan
                    RelationalExpressionNode.lowerOrEqualThanOperator -> RelationalFunctions::lowerOrEqualThan
                    RelationalExpressionNode.greaterOrEqualThanOperator -> RelationalFunctions::greaterOrEqualThan
                    else -> throw AngmarUnreachableException()
                }(left, right)
    }
}
