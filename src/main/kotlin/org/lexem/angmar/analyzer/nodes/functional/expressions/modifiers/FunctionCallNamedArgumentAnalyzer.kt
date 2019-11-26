package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Analyzer for named arguments of function calls.
 */
internal object FunctionCallNamedArgumentAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndExpression = signalEndIdentifier + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: FunctionCallNamedArgumentNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.identifier)
            }
            signalEndIdentifier -> {
                // Move Last to Key in stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                // Get the arguments
                val value = analyzer.memory.getLastFromStack()
                val identifier = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Key) as LxmString
                val arguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(
                        analyzer.memory) as LxmArguments

                arguments.addNamedArgument(analyzer.memory, identifier.primitive, value)

                // Remove Last and Key from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
