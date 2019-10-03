package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
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
                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                // Get the arguments
                val value = analyzer.memory.popStack()
                val identifier = analyzer.memory.popStack() as LxmString
                val arguments = analyzer.memory.popStack()
                val argumentsDeref = arguments.dereference(analyzer.memory) as LxmArguments

                argumentsDeref.addNamedArgument(analyzer.memory, identifier.primitive, value)

                analyzer.memory.pushStackIgnoringReferenceCount(arguments)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
