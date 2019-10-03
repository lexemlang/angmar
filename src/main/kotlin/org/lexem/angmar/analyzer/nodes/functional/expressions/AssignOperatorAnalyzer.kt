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
        val right = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.popStack())
        val left = analyzer.memory.popStack() as LexemSetter
        val leftFinal = left.resolve(analyzer.memory)

        left.increaseReferenceCount(analyzer.memory) // Used to allow to use left again.
        analyzer.memory.pushStack(left)

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
                    analyzer.memory.pushStack(leftFinal)
                } else {
                    analyzer.memory.pushStack(right)
                }

                assignFinalValue(analyzer)
                return analyzer.nextNode(node.parent, node.parentSignal)
            }
            ConditionalExpressionNode.orOperator -> {
                if (RelationalFunctions.isTruthy(leftFinal)) {
                    analyzer.memory.pushStack(leftFinal)
                } else {
                    analyzer.memory.pushStack(right)
                }

                assignFinalValue(analyzer)
                return analyzer.nextNode(node.parent, node.parentSignal)
            }
            ConditionalExpressionNode.xorOperator -> {
                if (RelationalFunctions.isTruthy(leftFinal)) {
                    if (RelationalFunctions.isTruthy(right)) {
                        // true ^^ true
                        analyzer.memory.pushStack(LxmNil)
                    } else {
                        // true ^^ false
                        analyzer.memory.pushStack(leftFinal)
                    }
                } else if (RelationalFunctions.isTruthy(right)) {
                    // false ^^ true
                    analyzer.memory.pushStack(right)
                } else {
                    // false ^^ false
                    analyzer.memory.pushStack(LxmNil)
                }

                assignFinalValue(analyzer)
                return analyzer.nextNode(node.parent, node.parentSignal)
            }
            else -> throw AngmarUnreachableException()
        }

        val operatorFunction = BinaryAnalyzerCommons.getOperatorFunction(analyzer, leftFinal, node.parent!!,
                (node.parent as AssignExpressionNode).left, node.operator, operatorFunctionName)

        // Create argument values.
        val arguments = BinaryAnalyzerCommons.createArguments(analyzer, leftFinal, right)

        return AnalyzerNodesCommons.callFunction(analyzer, operatorFunction, arguments, node, signalEndOperator)
    }

    /**
     * Assigns the final value.
     */
    private fun assignFinalValue(analyzer: LexemAnalyzer) {
        val right = analyzer.memory.popStack()
        val left = analyzer.memory.popStack() as LexemSetter

        left.set(analyzer.memory, right)

        analyzer.memory.pushStack(right)
    }
}
