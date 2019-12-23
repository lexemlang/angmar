package org.lexem.angmar.compiler.functional.expressions.binary

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import kotlin.math.*

/**
 * Compiler for [MultiplicativeExpressionNode].
 */
internal class MultiplicativeExpressionCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: MultiplicativeExpressionNode) : CompiledNode(parent, parentSignal, parserNode) {
    val expressions = mutableListOf<CompiledNode>()
    var operators = mutableListOf<String>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            MultiplicativeExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: MultiplicativeExpressionNode): CompiledNode {
            val result = MultiplicativeExpressionCompiled(parent, parentSignal, node)

            var last: LexemPrimitive? = null

            // Process the first.
            val compiledExpression =
                    node.expressions.first().compile(result, MultiplicativeExpressionAnalyzer.signalEndFirstExpression)

            if (compiledExpression is ConstantCompiled) {
                last = compiledExpression.value
            } else {
                result.expressions.add(compiledExpression)
            }

            // Process the rest.
            for ((index, operator) in node.operators.withIndex()) {
                val compiledExpression = node.expressions[index + 1].compile(result,
                        result.expressions.size + MultiplicativeExpressionAnalyzer.signalEndFirstExpression)

                if (compiledExpression is ConstantCompiled) {
                    if (last == null) {
                        last = compiledExpression.value
                        result.operators.add(operator)
                    } else {
                        last = operate(operator, last, compiledExpression.value, node)
                    }
                } else {
                    if (last != null) {
                        result.expressions.add(ConstantCompiled(result,
                                result.expressions.size + MultiplicativeExpressionAnalyzer.signalEndFirstExpression,
                                node, last))
                        result.expressions.add(compiledExpression.changeParentSignal(
                                result.expressions.size + MultiplicativeExpressionAnalyzer.signalEndFirstExpression))
                    } else {
                        result.expressions.add(compiledExpression)
                    }

                    result.operators.add(operator)

                    last = null
                }
            }

            // Constant.
            if (result.expressions.isEmpty()) {
                return ConstantCompiled(parent, parentSignal, node, last!!)
            }

            if (last != null) {
                result.expressions.add(ConstantCompiled(result,
                        result.expressions.size + MultiplicativeExpressionAnalyzer.signalEndFirstExpression, node,
                        last))
            }

            return result
        }

        /**
         * Operates two values.
         */
        private fun operate(operator: String, left: LexemPrimitive, right: LexemPrimitive,
                node: ParserNode): LexemPrimitive {
            when (operator) {
                MultiplicativeExpressionNode.multiplicationOperator -> {
                    return when (left) {
                        is LxmInteger -> when (right) {
                            is LxmFloat -> LxmFloat.from(left.primitive * right.primitive)
                            is LxmInteger -> LxmInteger.from(left.primitive * right.primitive)
                            else -> throw AdditiveExpressionCompiled.binaryError(
                                    AnalyzerCommons.Operators.Multiplication, left, right, node)
                        }
                        is LxmFloat -> when (right) {
                            is LxmFloat -> LxmFloat.from(left.primitive * right.primitive)
                            is LxmInteger -> LxmFloat.from(left.primitive * right.primitive)
                            else -> throw AdditiveExpressionCompiled.binaryError(
                                    AnalyzerCommons.Operators.Multiplication, left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.Multiplication,
                                left, node)
                    }
                }
                MultiplicativeExpressionNode.divisionOperator -> {
                    return when (left) {
                        is LxmInteger -> when (right) {
                            is LxmFloat -> LxmFloat.from(left.primitive / right.primitive)
                            is LxmInteger -> LxmInteger.from(left.primitive / right.primitive)
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.Division,
                                    left, right, node)
                        }
                        is LxmFloat -> when (right) {
                            is LxmFloat -> LxmFloat.from(left.primitive / right.primitive)
                            is LxmInteger -> LxmFloat.from(left.primitive / right.primitive)
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.Division,
                                    left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.Division, left,
                                node)
                    }
                }
                MultiplicativeExpressionNode.integerDivisionOperator -> {
                    return when (left) {
                        is LxmInteger -> when (right) {
                            is LxmFloat -> LxmInteger.from(truncate(left.primitive / right.primitive).toInt())
                            is LxmInteger -> LxmInteger.from(left.primitive / right.primitive)
                            else -> throw AdditiveExpressionCompiled.binaryError(
                                    AnalyzerCommons.Operators.IntegerDivision, left, right, node)
                        }
                        is LxmFloat -> when (right) {
                            is LxmFloat -> LxmInteger.from(truncate(left.primitive / right.primitive).toInt())
                            is LxmInteger -> LxmInteger.from(truncate(left.primitive / right.primitive).toInt())
                            else -> throw AdditiveExpressionCompiled.binaryError(
                                    AnalyzerCommons.Operators.IntegerDivision, left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.IntegerDivision,
                                left, node)
                    }
                }
                MultiplicativeExpressionNode.reminderOperator -> {
                    return when (left) {
                        is LxmInteger -> when (right) {
                            is LxmFloat -> LxmFloat.from(left.primitive % right.primitive)
                            is LxmInteger -> LxmInteger.from(left.primitive % right.primitive)
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.Reminder,
                                    left, right, node)
                        }
                        is LxmFloat -> when (right) {
                            is LxmFloat -> LxmFloat.from(left.primitive % right.primitive)
                            is LxmInteger -> LxmFloat.from(left.primitive % right.primitive)
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.Reminder,
                                    left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.Reminder, left,
                                node)
                    }
                }
                else -> throw AngmarUnreachableException()
            }
        }
    }
}
