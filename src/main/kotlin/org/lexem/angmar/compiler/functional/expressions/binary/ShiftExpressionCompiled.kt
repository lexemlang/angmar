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

/**
 * Compiler for [ShiftExpressionNode].
 */
internal class ShiftExpressionCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: ShiftExpressionNode) : CompiledNode(parent, parentSignal, parserNode) {
    val expressions = mutableListOf<CompiledNode>()
    var operators = mutableListOf<String>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ShiftExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ShiftExpressionNode): CompiledNode {
            val result = ShiftExpressionCompiled(parent, parentSignal, node)

            var last: LexemPrimitive? = null

            // Process the first.
            val compiledExpression =
                    node.expressions.first().compile(result, ShiftExpressionAnalyzer.signalEndFirstExpression)

            if (compiledExpression is ConstantCompiled) {
                last = compiledExpression.value
            } else {
                result.expressions.add(compiledExpression)
            }

            // Process the rest.
            for ((index, operator) in node.operators.withIndex()) {
                val compiledExpression = node.expressions[index + 1].compile(result,
                        result.expressions.size + ShiftExpressionAnalyzer.signalEndFirstExpression)

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
                                result.expressions.size + ShiftExpressionAnalyzer.signalEndFirstExpression, node, last))
                        result.expressions.add(compiledExpression.changeParentSignal(
                                result.expressions.size + ShiftExpressionAnalyzer.signalEndFirstExpression))
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
                        result.expressions.size + ShiftExpressionAnalyzer.signalEndFirstExpression, node, last))
            }

            return result
        }

        /**
         * Operates two values.
         */
        private fun operate(operator: String, left: LexemPrimitive, right: LexemPrimitive,
                node: ParserNode): LexemPrimitive {
            when (operator) {
                ShiftExpressionNode.leftShiftOperator -> {
                    return when (left) {
                        is LxmBitList -> when (right) {
                            is LxmInteger -> LxmBitList(left.primitive.leftShift(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LeftShift,
                                    left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.LeftShift, left,
                                node)
                    }
                }
                ShiftExpressionNode.rightShiftOperator -> {
                    return when (left) {
                        is LxmBitList -> when (right) {
                            is LxmInteger -> LxmBitList(left.primitive.rightShift(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.RightShift,
                                    left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.RightShift,
                                left, node)
                    }
                }
                ShiftExpressionNode.leftRotationOperator -> {
                    return when (left) {
                        is LxmBitList -> when (right) {
                            is LxmInteger -> LxmBitList(left.primitive.leftRotate(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LeftRotate,
                                    left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.LeftRotate,
                                left, node)
                    }
                }
                ShiftExpressionNode.rightRotationOperator -> {
                    return when (left) {
                        is LxmBitList -> when (right) {
                            is LxmInteger -> LxmBitList(left.primitive.rightRotate(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.RightRotate,
                                    left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.RightRotate,
                                left, node)
                    }
                }
                else -> throw AngmarUnreachableException()
            }
        }
    }
}
