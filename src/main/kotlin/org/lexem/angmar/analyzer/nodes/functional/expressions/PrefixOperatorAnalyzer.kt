package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Analyzer for prefix operators.
 */
internal object PrefixOperatorAnalyzer {
    const val signalEndOperator = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PrefixOperatorNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return operate(analyzer, signal, node)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Operates two values depending on the specified operator.
     */
    private fun operate(analyzer: LexemAnalyzer, signal: Int, node: PrefixOperatorNode) {
        // Get operand values.
        val thisValue = AnalyzerNodesCommons.resolveSetter(analyzer.memory, analyzer.memory.getLastFromStack())

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

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
        val argumentsRef = analyzer.memory.add(arguments)

        return AnalyzerNodesCommons.callFunction(analyzer, operatorFunctionRef, argumentsRef, node,
                LxmCodePoint(node, signalEndOperator))
    }
}
