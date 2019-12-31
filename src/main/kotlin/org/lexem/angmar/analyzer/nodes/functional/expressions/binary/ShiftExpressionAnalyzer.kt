package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.BinaryAnalyzerCommons.createArguments
import org.lexem.angmar.compiler.functional.expressions.binary.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Analyzer for shift expressions.
 */
internal object ShiftExpressionAnalyzer {
    const val signalEndFirstExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ShiftExpressionCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expressions.first())
            }
            in signalEndFirstExpression until signalEndFirstExpression + node.expressions.size -> {
                val position = (signal - signalEndFirstExpression) + 1

                // Even values must compute the addition.
                if (position % 2 == 0) {
                    return operate(analyzer, position, signal, node)
                }
                // Evaluate the next operand.
                else {
                    // Move Last to Left in the stack.
                    analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Left)

                    return analyzer.nextNode(node.expressions[position])
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Operates two values depending on the specified operator.
     */
    private fun operate(analyzer: LexemAnalyzer, signal: Int, position: Int, node: ShiftExpressionCompiled) {
        val operator = node.operators[position / 2 - 1]

        // Get operand values.
        val right = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.getLastFromStack())
        val left = AnalyzerNodesCommons.resolveSetter(analyzer.memory,
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left))

        // Get operand function.
        val operatorFunctionName = when (operator) {
            ShiftExpressionNode.leftShiftOperator -> AnalyzerCommons.Operators.LeftShift
            ShiftExpressionNode.rightShiftOperator -> AnalyzerCommons.Operators.RightShift
            ShiftExpressionNode.leftRotationOperator -> AnalyzerCommons.Operators.LeftRotate
            ShiftExpressionNode.rightRotationOperator -> AnalyzerCommons.Operators.RightRotate
            else -> throw AngmarUnreachableException()
        }

        val operatorFunctionRef =
                BinaryAnalyzerCommons.getOperatorFunction(analyzer, left, node, node.expressions[position - 2],
                        operator, operatorFunctionName)

        // Create argument values.
        val arguments = createArguments(analyzer, left, right)

        // Remove operands from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Left)
        analyzer.memory.removeLastFromStack()

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val contextName = AnalyzerCommons.getContextName(context)
        return AnalyzerNodesCommons.callFunction(analyzer, operatorFunctionRef, arguments,
                LxmCodePoint(node, signal + 1, callerNode = node, callerContextName = contextName.primitive))
    }
}
