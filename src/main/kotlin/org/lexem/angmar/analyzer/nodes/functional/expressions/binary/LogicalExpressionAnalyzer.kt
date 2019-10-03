package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.BinaryAnalyzerCommons.createArguments
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Analyzer for logical expressions.
 */
internal object LogicalExpressionAnalyzer {
    const val signalEndFirstExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LogicalExpressionNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expressions[0])
            }
            in signalEndFirstExpression until signalEndFirstExpression + node.expressions.size -> {
                val position = (signal - signalEndFirstExpression) + 1

                // Even values must compute the addition.
                if (position % 2 == 0) {
                    return operate(analyzer, position, signal, node)
                }
                // Evaluate the next operand.
                else {
                    return analyzer.nextNode(node.expressions[position])
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Operates two values depending on the specified operator.
     */
    private fun operate(analyzer: LexemAnalyzer, signal: Int, position: Int, node: LogicalExpressionNode) {
        val operator = node.operators[position / 2 - 1]

        // Get operand values.
        val right = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.popStack())
        val left = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.popStack())

        // Get operand function.
        val operatorFunctionName = when (operator) {
            LogicalExpressionNode.andOperator -> AnalyzerCommons.Operators.LogicalAnd
            LogicalExpressionNode.orOperator -> AnalyzerCommons.Operators.LogicalOr
            LogicalExpressionNode.xorOperator -> AnalyzerCommons.Operators.LogicalXor
            else -> throw AngmarUnreachableException()
        }

        val operatorFunction =
                BinaryAnalyzerCommons.getOperatorFunction(analyzer, left, node, node.expressions[position - 2],
                        operator, operatorFunctionName)

        // Create argument values.
        val arguments = createArguments(analyzer, left, right)

        return AnalyzerNodesCommons.callFunction(analyzer, operatorFunction, arguments, node, signal + 1)
    }
}
