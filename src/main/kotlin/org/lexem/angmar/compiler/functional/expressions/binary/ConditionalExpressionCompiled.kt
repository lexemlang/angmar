package org.lexem.angmar.compiler.functional.expressions.binary

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.binary.*

/**
 * Compiler for [ConditionalExpressionNode].
 */
internal class ConditionalExpressionCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: ConditionalExpressionNode) : CompiledNode(parent, parentSignal, parserNode) {
    val expressions = mutableListOf<CompiledNode>()
    lateinit var operator: String

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ConditionalExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ConditionalExpressionNode): CompiledNode {
            // Create the operator hierarchy.
            var currentResult = ConditionalExpressionCompiled(parent, parentSignal, node)
            currentResult.operator = node.operators.first()

            for ((index, operator) in node.operators.withIndex()) {
                val compiledExpression = node.expressions[index].compile(currentResult,
                        ConditionalExpressionAnalyzer.signalEndFirstExpression + currentResult.expressions.size)
                currentResult.expressions.add(compiledExpression)

                if (currentResult.operator != operator) {
                    val newResult = ConditionalExpressionCompiled(parent, parentSignal, node)
                    newResult.operator = operator

                    currentResult.linkTo(newResult, ConditionalExpressionAnalyzer.signalEndFirstExpression, node)

                    newResult.expressions.add(simplify(currentResult))
                    currentResult = newResult
                }
            }

            // Add last operand.
            val compiledExpression = node.expressions.last().compile(currentResult,
                    ConditionalExpressionAnalyzer.signalEndFirstExpression + currentResult.expressions.size)
            currentResult.expressions.add(compiledExpression)

            return simplify(currentResult)
        }

        /**
         * Simplifies a list of operands associated with one operator.
         */
        private fun simplify(node: ConditionalExpressionCompiled): CompiledNode {
            when (node.operator) {
                ConditionalExpressionNode.andOperator -> {
                    var index = 0
                    while (index < node.expressions.size) {
                        val expression = node.expressions[index]
                        if (expression is ConstantCompiled) {
                            if (!RelationalFunctions.isTruthy(expression.value)) {
                                index += 1
                                break
                            }

                            // Remove all truthy constants.
                            if (index != node.expressions.lastIndex) {
                                node.expressions.removeAt(index)
                                continue
                            }
                        }

                        index += 1
                    }

                    // Remove rest.
                    for (i in node.expressions.lastIndex downTo index) {
                        node.expressions.removeAt(i)
                    }
                }
                ConditionalExpressionNode.orOperator -> {
                    var index = 0
                    while (index < node.expressions.size) {
                        val expression = node.expressions[index]
                        if (expression is ConstantCompiled) {
                            if (RelationalFunctions.isTruthy(expression.value)) {
                                index += 1
                                break
                            }

                            // Remove all falsy constants.
                            if (index != node.expressions.lastIndex) {
                                node.expressions.removeAt(index)
                                continue
                            }
                        }

                        index += 1
                    }

                    // Remove rest.
                    for (i in node.expressions.lastIndex downTo index) {
                        node.expressions.removeAt(i)
                    }
                }
                ConditionalExpressionNode.xorOperator -> {
                    var index = 0
                    var count = 0
                    while (index < node.expressions.size) {
                        val expression = node.expressions[index]
                        if (expression is ConstantCompiled) {
                            if (RelationalFunctions.isTruthy(expression.value)) {
                                count += 1

                                if (count == 2) {
                                    index += 1
                                    break
                                }
                            } else {
                                // Remove all falsy constants.
                                node.expressions.removeAt(index)
                                continue

                            }
                        }

                        index += 1
                    }

                    // Remove rest.
                    for (i in node.expressions.lastIndex downTo index) {
                        node.expressions.removeAt(i)
                    }

                    if (node.expressions.size == 1) {
                        return node.expressions.first().linkTo(node.parent, node.parentSignal, node.parserNode)
                    }

                    if (node.expressions.size == count || node.expressions.isEmpty()) {
                        return ConstantCompiled(node.parent, node.parentSignal, node.parserNode, LxmNil)
                    }
                }
                else -> throw AngmarUnreachableException()
            }

            if (node.expressions.size == 1) {
                return node.expressions.first().linkTo(node.parent, node.parentSignal, node.parserNode)
            }

            // Re-link the reminders.
            for ((index, expression) in node.expressions.withIndex()) {
                expression.changeParentSignal(index + ConditionalExpressionAnalyzer.signalEndFirstExpression)
            }

            return node
        }
    }
}
