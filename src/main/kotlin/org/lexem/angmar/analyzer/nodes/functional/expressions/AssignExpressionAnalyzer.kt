package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.functional.expressions.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Analyzer for assign expressions.
 */
internal object AssignExpressionAnalyzer {
    const val signalEndLeft = AnalyzerNodesCommons.signalStart + 1
    const val signalEndOperator = signalEndLeft + 1
    const val signalEndRight = signalEndOperator + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AssignExpressionCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.left)
            }
            signalEndLeft -> {
                // Check left.
                val left = analyzer.memory.getLastFromStack()

                if (left !is LexemSetter) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.AssignToConstant,
                            "The returned value by the left expression is a value not a reference, therefore it cannot be assigned.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.left.from.position(), node.left.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                // Move Last to Left in the stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Left)

                return analyzer.nextNode(node.right)
            }
            signalEndRight -> {
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
    private fun operate(analyzer: LexemAnalyzer, node: AssignExpressionCompiled) {
        // Get operand values.
        val right = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.getLastFromStack())
        val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left) as LexemSetter
        val leftFinal = left.getSetterPrimitive(analyzer.memory)

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

        val operatorFunctionRef =
                BinaryAnalyzerCommons.getOperatorFunction(analyzer, leftFinal, node, node.left, node.operator,
                        operatorFunctionName)

        // Create argument values.
        val arguments = BinaryAnalyzerCommons.createArguments(analyzer, leftFinal, right)

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val contextName = AnalyzerCommons.getContextName(analyzer.memory, context)
        return AnalyzerNodesCommons.callFunction(analyzer, operatorFunctionRef, arguments, node,
                LxmCodePoint(node, signalEndOperator, callerNode = node, callerContextName = contextName.primitive))
    }

    /**
     * Assigns the final value.
     */
    private fun assignFinalValue(analyzer: LexemAnalyzer) {
        val right = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.getLastFromStack())
        val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left) as LexemSetter

        left.setSetterValue(analyzer.memory, right)

        // Remove Left from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Left)
    }
}
