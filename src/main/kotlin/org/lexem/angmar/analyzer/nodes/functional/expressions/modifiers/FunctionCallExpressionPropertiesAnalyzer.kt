package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Analyzer for expression properties of function calls.
 */
internal object FunctionCallExpressionPropertiesAnalyzer {
    const val signalEndValue = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: FunctionCallExpressionPropertiesNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.value)
            }
            signalEndValue -> {
                val value = analyzer.memory.getLastFromStack()
                val arguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(
                        analyzer.memory) as LxmArguments

                arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.ArgumentsProperties, value)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
