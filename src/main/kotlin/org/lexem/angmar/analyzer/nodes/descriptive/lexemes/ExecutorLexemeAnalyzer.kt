package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.descriptive.lexemes.*


/**
 * Analyzer for executor lexemes.
 */
internal object ExecutorLexemeAnalyzer {
    const val signalEndFirstExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ExecutorLexemeCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expressions.first())
            }
            in signalEndFirstExpression..signalEndFirstExpression + node.expressions.size -> {
                val position = (signal - signalEndFirstExpression) + 1

                // Get value.
                val value = analyzer.memory.getLastFromStack()

                // Evaluate the next operand.
                if (position < node.expressions.size) {
                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    return analyzer.nextNode(node.expressions[position])
                }

                // Evaluate condition.
                if (node.isConditional && !RelationalFunctions.isTruthy(value)) {
                    return analyzer.initBacktracking()
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
