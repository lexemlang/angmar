package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Analyzer for assign expressions.
 */
internal object AssignOperatorAnalyzer {
    const val signalEndOperator = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AssignOperatorNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.operator.isNotEmpty()) {
                    return operate(analyzer, node)
                }

                assignFinalValue(analyzer)
            }
            signalEndOperator -> {
                assignFinalValue(analyzer)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Operates two values depending on the specified operator.
     */
    private fun operate(analyzer: LexemAnalyzer, node: AssignOperatorNode) {
        // Get operand values.
        val right = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.getLastFromStack())
        val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left) as LexemSetter
        val leftFinal = left.getPrimitive(analyzer.memory)

        // Get operand function.
        val operatorFunctionName = when (node.operator) {
            MultiplicativeExpressionNode.multiplicationOperator -> AnalyzerCommons.Operators.Multiplication
            MultiplicativeExpressionNode.divisionOperator -> AnalyzerCommons.Operators.Division
            MultiplicativeExpressionNode.integerDivisionOperator -> AnalyzerCommons.Operators.IntegerDivision
            MultiplicativeExpressionNode.reminderOperator -> AnalyzerCommons.Operators.Reminder
            AdditiveExpressionNode.additionOperator -> AnalyzerCommons.Operators.Add
            AdditiveExpressionNode.subtractionOperator -> AnalyzerCommons.Operators.Sub
            ShiftExpressionNode.rightShiftOperator -> AnalyzerCommons.Operators.RightShift
            ShiftExpressionNode.leftShiftOperator -> AnalyzerCommons.Operators.LeftShift
            ShiftExpressionNode.rightRotationOperator -> AnalyzerCommons.Operators.RightRotate
            ShiftExpressionNode.leftRotationOperator -> AnalyzerCommons.Operators.LeftRotate
            LogicalExpressionNode.andOperator -> AnalyzerCommons.Operators.LogicalAnd
            LogicalExpressionNode.orOperator -> AnalyzerCommons.Operators.LogicalOr
            LogicalExpressionNode.xorOperator -> AnalyzerCommons.Operators.LogicalXor
            ConditionalExpressionNode.andOperator -> {
                if (!RelationalFunctions.isTruthy(leftFinal)) {
                    analyzer.memory.replaceLastStackCell(leftFinal)
                }

                assignFinalValue(analyzer)
                return analyzer.nextNode(node.parent, node.parentSignal)
            }
            ConditionalExpressionNode.orOperator -> {
                if (RelationalFunctions.isTruthy(leftFinal)) {
                    analyzer.memory.replaceLastStackCell(leftFinal)
                }

                assignFinalValue(analyzer)
                return analyzer.nextNode(node.parent, node.parentSignal)
            }
            ConditionalExpressionNode.xorOperator -> {
                if (RelationalFunctions.isTruthy(leftFinal)) {
                    if (RelationalFunctions.isTruthy(right)) {
                        // true ^^ true
                        analyzer.memory.replaceLastStackCell(LxmNil)
                    } else {
                        // true ^^ false
                        analyzer.memory.replaceLastStackCell(leftFinal)
                    }
                } else if (!RelationalFunctions.isTruthy(right)) {
                    // false ^^ false
                    analyzer.memory.replaceLastStackCell(LxmNil)
                }
                // else false ^^ true

                assignFinalValue(analyzer)
                return analyzer.nextNode(node.parent, node.parentSignal)
            }
            else -> throw AngmarUnreachableException()
        }

        val operatorFunctionRef = BinaryAnalyzerCommons.getOperatorFunction(analyzer, leftFinal, node.parent!!,
                (node.parent as AssignExpressionNode).left, node.operator, operatorFunctionName)

        // Create argument values.
        val arguments = BinaryAnalyzerCommons.createArguments(analyzer, leftFinal, right)

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        return AnalyzerNodesCommons.callFunction(analyzer, operatorFunctionRef, arguments, node,
                LxmCodePoint(node, signalEndOperator))
    }

    /**
     * Assigns the final value.
     */
    private fun assignFinalValue(analyzer: LexemAnalyzer) {
        val right = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.getLastFromStack())
        val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left) as LexemSetter

        left.setPrimitive(analyzer.memory, right)

        // Remove Left from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Left)
    }
}
