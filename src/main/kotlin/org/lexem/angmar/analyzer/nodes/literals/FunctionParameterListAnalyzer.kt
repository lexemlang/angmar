package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for function parameter list.
 */
internal object FunctionParameterListAnalyzer {
    const val signalEndPositionalSpread = AnalyzerNodesCommons.signalStart + 1
    const val signalEndNamedSpread = signalEndPositionalSpread + 1
    const val signalEndFirstParameter = signalEndNamedSpread + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: FunctionParameterListNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add a parameters object.
                analyzer.memory.pushStack(LxmParameters())

                if (node.parameters.isNotEmpty()) {
                    return analyzer.nextNode(node.parameters[0])
                }

                if (node.positionalSpread != null) {
                    return analyzer.nextNode(node.positionalSpread)
                }

                if (node.namedSpread != null) {
                    return analyzer.nextNode(node.namedSpread)
                }

                processParameters(analyzer, node)
            }
            in signalEndFirstParameter until signalEndFirstParameter + node.parameters.size -> {
                val position = (signal - signalEndFirstParameter) + 1

                if (position < node.parameters.size) {
                    return analyzer.nextNode(node.parameters[position])
                }

                if (node.positionalSpread != null) {
                    return analyzer.nextNode(node.positionalSpread)
                }

                if (node.namedSpread != null) {
                    return analyzer.nextNode(node.namedSpread)
                }

                processParameters(analyzer, node)
            }
            signalEndPositionalSpread -> {
                // Add the identifier.
                val identifier = analyzer.memory.popStack() as LxmString
                val parameters = analyzer.memory.popStack() as LxmParameters
                parameters.setPositionalSpreadArgument(identifier.primitive)

                analyzer.memory.pushStack(parameters)

                if (node.namedSpread != null) {
                    return analyzer.nextNode(node.namedSpread)
                }

                processParameters(analyzer, node)
            }
            signalEndNamedSpread -> {
                // Add the identifier.
                val identifier = analyzer.memory.popStack() as LxmString
                val parameters = analyzer.memory.popStack() as LxmParameters
                parameters.setNamedSpreadArgument(identifier.primitive)

                analyzer.memory.pushStack(parameters)

                processParameters(analyzer, node)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Process the parameters.
     */
    private fun processParameters(analyzer: LexemAnalyzer, node: FunctionParameterListNode) {
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val parameters = analyzer.memory.popStack() as LxmParameters
        val argumentsRef = analyzer.memory.popStack() as LxmReference
        val arguments = argumentsRef.dereference(analyzer.memory) as LxmArguments

        arguments.mapArgumentsToContext(analyzer.memory, parameters, context)

        // Decrease the reference count of the arguments.
        argumentsRef.decreaseReferenceCount(analyzer.memory)
    }
}
