package org.lexem.angmar.compiler.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.expressions.binary.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*

/**
 * Compiler for [PrefixExpressionNode].
 */
internal class PrefixExpressionCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: PrefixExpressionNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var expression: CompiledNode
    var operator = ""

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PrefixExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: PrefixExpressionNode): CompiledNode {
            val result = PrefixExpressionCompiled(parent, parentSignal, node)
            result.expression = node.expression.compile(result, PrefixExpressionAnalyzer.signalEndExpression)
            result.operator = node.prefix.operator

            if (result.expression is ConstantCompiled) {
                val resultValue = operate(result.operator, (result.expression as ConstantCompiled).value, node)
                return ConstantCompiled(parent, parentSignal, node, resultValue)
            }

            return result
        }

        /**
         * Operates the prefix value.
         */
        private fun operate(operator: String, value: LexemPrimitive, node: ParserNode): LexemPrimitive {
            when (operator) {
                PrefixOperatorNode.bitwiseNegationOperator -> {
                    return when (value) {
                        is LxmBitList -> LxmBitList(!value.primitive)
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.Add, value,
                                node)
                    }
                }
                PrefixOperatorNode.notOperator -> {
                    return when (value) {
                        is LxmLogic -> LxmLogic.from(!value.primitive)
                        is LxmInterval -> LxmInterval.from(value.primitive.not())
                        else -> LxmLogic.from(!RelationalFunctions.isTruthy(value))
                    }
                }
                PrefixOperatorNode.affirmationOperator -> {
                    return when (value) {
                        is LxmFloat -> value
                        is LxmInteger -> value
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.Add, value,
                                node)
                    }
                }
                PrefixOperatorNode.negationOperator -> {
                    return when (value) {
                        is LxmFloat -> LxmFloat.from(-value.primitive)
                        is LxmInteger -> LxmInteger.from(-value.primitive)
                        else -> throw AdditiveExpressionCompiled.unitaryError(AnalyzerCommons.Operators.Add, value,
                                node)
                    }
                }
                else -> throw AngmarUnreachableException()
            }
        }
    }
}
