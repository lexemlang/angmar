package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Analyzer for relational expressions.
 */
internal object RelationalExpressionAnalyzer {
    const val signalEndFirstExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: RelationalExpressionNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expressions[0])
            }
            signalEndFirstExpression -> {
                return analyzer.nextNode(node.expressions[1])
            }
            in signalEndFirstExpression + 1 until signalEndFirstExpression + node.expressions.size -> {
                val position = (signal - signalEndFirstExpression) + 1

                // Finalize if the comparision fails.
                if (operate(analyzer, position, signal, node)) {
                    return analyzer.nextNode(node.parent, node.parentSignal)
                }

                // Evaluate the next operand.
                if (position < node.expressions.size) {
                    return analyzer.nextNode(node.expressions[position])
                } else {
                    // Replace the last operand by a true value into the stack.
                    analyzer.memory.popStack()
                    analyzer.memory.pushStack(LxmLogic.True)
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Operates two values depending on the specified operator.
     * Returns a value indicating whether the expression must end.
     */
    private fun operate(analyzer: LexemAnalyzer, signal: Int, position: Int, node: RelationalExpressionNode): Boolean {
        val operator = node.operators[position - (signalEndFirstExpression + 1)]

        // Get operand values.
        val right = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.popStack())
        val left = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.popStack())

        // Get operand function.
        val isOk = when (operator) {
            RelationalExpressionNode.equalityOperator -> RelationalFunctions.lxmEquals(analyzer.memory, left, right)
            RelationalExpressionNode.inequalityOperator -> RelationalFunctions.lxmNotEquals(analyzer.memory, left,
                    right)
            else -> when (operator) {
                RelationalExpressionNode.identityOperator -> RelationalFunctions::identityEquals
                RelationalExpressionNode.notIdentityOperator -> RelationalFunctions::identityNotEquals
                RelationalExpressionNode.lowerThanOperator -> RelationalFunctions::lowerThan
                RelationalExpressionNode.greaterThanOperator -> RelationalFunctions::greaterThan
                RelationalExpressionNode.lowerOrEqualThanOperator -> RelationalFunctions::lowerOrEqualThan
                RelationalExpressionNode.greaterOrEqualThanOperator -> RelationalFunctions::greaterOrEqualThan
                else -> throw AngmarUnreachableException()
            }(left, right)
        }

        // If fails adds a false to the stack and returns.
        if (!isOk) {
            analyzer.memory.pushStack(LxmLogic.False)
            return true
        }

        analyzer.memory.pushStack(right)
        return false
    }
}
