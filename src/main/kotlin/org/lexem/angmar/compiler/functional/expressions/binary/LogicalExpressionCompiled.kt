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
 * Compiler for [LogicalExpressionNode].
 */
internal class LogicalExpressionCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: LogicalExpressionNode) : CompiledNode(parent, parentSignal, parserNode) {
    val expressions = mutableListOf<CompiledNode>()
    var operators = mutableListOf<String>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            LogicalExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: LogicalExpressionNode): CompiledNode {
            val result = LogicalExpressionCompiled(parent, parentSignal, node)

            var last: LexemPrimitive? = null

            // Process the first.
            val compiledExpression =
                    node.expressions.first().compile(result, LogicalExpressionAnalyzer.signalEndFirstExpression)

            if (compiledExpression is ConstantCompiled) {
                last = compiledExpression.value
            } else {
                result.expressions.add(compiledExpression)
            }

            // Process the rest.
            for ((index, operator) in node.operators.withIndex()) {
                val compiledExpression = node.expressions[index + 1].compile(result,
                        result.expressions.size + LogicalExpressionAnalyzer.signalEndFirstExpression)

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
                                result.expressions.size + LogicalExpressionAnalyzer.signalEndFirstExpression, node,
                                last))
                        result.expressions.add(compiledExpression.changeParentSignal(
                                result.expressions.size + LogicalExpressionAnalyzer.signalEndFirstExpression))
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
                        result.expressions.size + LogicalExpressionAnalyzer.signalEndFirstExpression, node, last))
            }

            return result
        }

        /**
         * Operates two values.
         */
        private fun operate(operator: String, left: LexemPrimitive, right: LexemPrimitive,
                node: ParserNode): LexemPrimitive {
            when (operator) {
                LogicalExpressionNode.andOperator -> {
                    return when (left) {
                        is LxmLogic -> when (right) {
                            is LxmLogic -> LxmLogic.from(left.primitive.and(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LogicalAnd,
                                    left, right, node)
                        }
                        is LxmBitList -> when (right) {
                            is LxmLogic -> LxmBitList(left.primitive.and(right.primitive))
                            is LxmBitList -> LxmBitList(left.primitive.and(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LogicalAnd,
                                    left, right, node)
                        }
                        is LxmInterval -> when (right) {
                            is LxmInteger -> LxmInterval.from(left.primitive.common(right.primitive))
                            is LxmInterval -> LxmInterval.from(left.primitive.common(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LogicalAnd,
                                    left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.LogicalAnd,
                                left, node)
                    }
                }
                LogicalExpressionNode.orOperator -> {
                    return when (left) {
                        is LxmLogic -> when (right) {
                            is LxmLogic -> LxmLogic.from(left.primitive.or(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LogicalOr,
                                    left, right, node)
                        }
                        is LxmBitList -> when (right) {
                            is LxmLogic -> LxmBitList(left.primitive.or(right.primitive))
                            is LxmBitList -> LxmBitList(left.primitive.or(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LogicalOr,
                                    left, right, node)
                        }
                        is LxmInterval -> when (right) {
                            is LxmInteger -> LxmInterval.from(left.primitive.plus(right.primitive))
                            is LxmInterval -> LxmInterval.from(left.primitive.plus(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LogicalOr,
                                    left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.LogicalOr, left,
                                node)
                    }
                }
                LogicalExpressionNode.xorOperator -> {
                    return when (left) {
                        is LxmLogic -> when (right) {
                            is LxmLogic -> LxmLogic.from(left.primitive.xor(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LogicalXor,
                                    left, right, node)
                        }
                        is LxmBitList -> when (right) {
                            is LxmLogic -> LxmBitList(left.primitive.xor(right.primitive))
                            is LxmBitList -> LxmBitList(left.primitive.xor(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LogicalXor,
                                    left, right, node)
                        }
                        is LxmInterval -> when (right) {
                            is LxmInteger -> LxmInterval.from(left.primitive.notCommon(right.primitive))
                            is LxmInterval -> LxmInterval.from(left.primitive.notCommon(right.primitive))
                            else -> throw AdditiveExpressionCompiled.binaryError(AnalyzerCommons.Operators.LogicalXor,
                                    left, right, node)
                        }
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.LogicalXor,
                                left, node)
                    }
                }
                else -> throw AngmarUnreachableException()
            }
        }
    }
}
