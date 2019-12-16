package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.descriptive.selectors.*


/**
 * Analyzer for property blocks of selectors.
 */
internal object PropertyBlockSelectorAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndCondition = signalEndIdentifier + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PropertyBlockSelectorNode) {
        if (node.isAddition) {
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    return analyzer.nextNode(node.condition)
                }
            }
        } else {
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    // Generate an intermediate context.
                    AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                    if (node.identifier != null) {
                        return analyzer.nextNode(node.identifier)
                    }

                    generateAlias(analyzer, AnalyzerCommons.Identifiers.DefaultPropertyName)

                    return analyzer.nextNode(node.condition)
                }
                signalEndIdentifier -> {
                    val identifier = analyzer.memory.getLastFromStack() as LxmString

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    generateAlias(analyzer, identifier.primitive)

                    return analyzer.nextNode(node.condition)
                }
                signalEndCondition -> {
                    // Remove the intermediate context.
                    AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Generates the alias for the property.
     */
    private fun generateAlias(analyzer: LexemAnalyzer, aliasName: String) {
        val property = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Property)
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, aliasName, property)
    }
}
