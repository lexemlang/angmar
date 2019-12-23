package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.descriptive.selectors.*


/**
 * Analyzer for properties of selectors.
 */
internal object PropertySelectorAnalyzer {
    const val signalEndFirstProperty = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PropertySelectorCompiled) {
        if (node.isAddition) {
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    return analyzer.nextNode(node.properties.first())
                }
            }
        } else {
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    return analyzer.nextNode(node.properties.first())
                }
                in signalEndFirstProperty..signalEndFirstProperty + node.properties.size -> {
                    val position = (signal - signalEndFirstProperty) + 1

                    val result = analyzer.memory.getLastFromStack() as LxmLogic

                    if (!result.primitive && position < node.properties.size) {
                        // Remove Last from the stack.
                        analyzer.memory.removeLastFromStack()

                        return analyzer.nextNode(node.properties[position])
                    }
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
