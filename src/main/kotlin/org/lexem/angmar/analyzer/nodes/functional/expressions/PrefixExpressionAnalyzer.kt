package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.compiler.functional.expressions.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Analyzer for prefixed expressions.
 */
internal object PrefixExpressionAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1
    const val signalEndOperator = signalEndExpression + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PrefixExpressionCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                return operate(analyzer, signal, node)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Operates two values depending on the specified operator.
     */
    private fun operate(analyzer: LexemAnalyzer, signal: Int, node: PrefixExpressionCompiled) {
        // Get operand values.
        val thisValue = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.getLastFromStack())

        // Get operand function.
        val operatorFunctionName = when (node.operator) {
            PrefixOperatorNode.notOperator -> AnalyzerCommons.Operators.LogicalNot
            PrefixOperatorNode.negationOperator -> AnalyzerCommons.Operators.ArithmeticNegation
            PrefixOperatorNode.affirmationOperator -> AnalyzerCommons.Operators.ArithmeticAffirmation
            PrefixOperatorNode.bitwiseNegationOperator -> AnalyzerCommons.Operators.BitwiseNegation
            else -> throw AngmarUnreachableException()
        }

        val operatorFunctionRef =
                BinaryAnalyzerCommons.getOperatorFunction(analyzer, thisValue, node.parent!!, node, node.operator,
                        operatorFunctionName)

        // Create argument values.
        val arguments = LxmArguments(analyzer.memory)
        arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, thisValue)

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val contextName = AnalyzerCommons.getContextName(analyzer.memory, context)
        return AnalyzerNodesCommons.callFunction(analyzer, operatorFunctionRef, arguments,
                LxmCodePoint(node, signalEndOperator, callerNode = node, callerContextName = contextName.primitive))
    }
}
