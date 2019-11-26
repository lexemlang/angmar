package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Analyzer for conditional expressions.
 */
internal object ConditionalExpressionAnalyzer {
    const val signalEndFirstExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ConditionalExpressionNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add initial previous value.
                when (node.operators[0]) {
                    ConditionalExpressionNode.andOperator -> {
                        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, LxmLogic.True)
                    }
                    ConditionalExpressionNode.orOperator -> {
                        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, LxmLogic.False)
                    }
                    ConditionalExpressionNode.xorOperator -> {
                        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, LxmLogic.False)
                    }
                    else -> throw AngmarUnreachableException()
                }

                return analyzer.nextNode(node.expressions[0])
            }
            in signalEndFirstExpression until signalEndFirstExpression + node.expressions.size -> {
                val position = (signal - signalEndFirstExpression) + 1

                // Finalize the section if the comparision fails.
                if (operate(analyzer, position, signal, node)) {
                    // Find a different operator.
                    val operator = node.operators[(position + 1) / 2 - 1]
                    for (i in (position + 1) / 2 until node.operators.size) {
                        if (operator != node.operators[i]) {
                            // Move Last to Left in the stack.
                            analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Left)

                            return analyzer.nextNode(node.expressions[i + 1])
                        }
                    }

                    // Finalize.
                    return analyzer.nextNode(node.parent, node.parentSignal)
                }

                // Evaluate the next operand.
                if (position < node.expressions.size) {
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
     * Returns a value indicating whether the expression must end.
     */
    private fun operate(analyzer: LexemAnalyzer, signal: Int, position: Int, node: ConditionalExpressionNode): Boolean {
        val operator = node.operators[(position + 1) / 2 - 1]

        // Get operand values.
        val right = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.getLastFromStack())
        val left = AnalyzerNodesCommons.resolveSetter(analyzer.memory,
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left))
        val rightBool = RelationalFunctions.isTruthy(right)
        val leftBool = RelationalFunctions.isTruthy(left)

        // Remove the Left from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Left)

        // Get operand function.
        var result = false
        when (operator) {
            ConditionalExpressionNode.andOperator -> {
                if (!rightBool) {
                    result = true
                }
            }
            ConditionalExpressionNode.orOperator -> {
                if (rightBool) {
                    result = true
                }
            }
            ConditionalExpressionNode.xorOperator -> {
                when {
                    leftBool -> {
                        if (rightBool) {
                            // true ^^ true
                            analyzer.memory.replaceLastStackCell(LxmNil)
                            return true
                        } else {
                            // true ^^ false
                            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, left)

                            // Remove Last from the stack.
                            analyzer.memory.removeLastFromStack()
                            return false
                        }
                    }
                    rightBool -> {
                        // true ^^ false
                    }
                    else -> {
                        // false ^^ false
                        analyzer.memory.replaceLastStackCell(LxmNil)
                        return true
                    }
                }
            }
            else -> throw AngmarUnreachableException()
        }

        return result
    }
}
