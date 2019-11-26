package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.selectors.*


/**
 * Analyzer for selectors.
 */
internal object SelectorAnalyzer {
    const val signalEndName = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFirstProperty = signalEndName + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: SelectorNode) {
        if (node.isAddition) {
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    return analyzer.nextNode(node.name)
                }
                signalEndName -> {
                    if (node.properties.isNotEmpty()) {
                        return analyzer.nextNode(node.properties.first())
                    }

                    // Move Node to Last in the stack.
                    analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Node)
                }
                in signalEndFirstProperty until signalEndFirstProperty + node.properties.size -> {
                    val position = (signal - signalEndFirstProperty) + 1

                    if (position < node.properties.size) {
                        return analyzer.nextNode(node.properties[position])
                    }

                    // Move Node to Last in the stack.
                    analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Node)
                }
            }
        } else {
            val signalEndFirstMethod = signalEndFirstProperty + node.properties.size
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    if (node.name != null) {
                        return analyzer.nextNode(node.name)
                    }

                    if (node.properties.isNotEmpty()) {
                        return analyzer.nextNode(node.properties.first())
                    }

                    if (node.methods.isNotEmpty()) {
                        return analyzer.nextNode(node.methods.first())
                    }

                    throw AngmarUnreachableException()
                }
                signalEndName -> {
                    val result = analyzer.memory.getLastFromStack() as LxmLogic

                    if (result.primitive) {
                        if (node.properties.isNotEmpty()) {
                            // Remove Last from the stack.
                            analyzer.memory.removeLastFromStack()

                            return analyzer.nextNode(node.properties.first())
                        }

                        if (node.methods.isNotEmpty()) {
                            // Remove Last from the stack.
                            analyzer.memory.removeLastFromStack()

                            return analyzer.nextNode(node.methods.first())
                        }
                    }
                }
                in signalEndFirstProperty until signalEndFirstProperty + node.properties.size -> {
                    val position = (signal - signalEndFirstProperty) + 1

                    val result = analyzer.memory.getLastFromStack() as LxmLogic

                    if (result.primitive) {
                        if (position < node.properties.size) {
                            // Remove Last from the stack.
                            analyzer.memory.removeLastFromStack()

                            return analyzer.nextNode(node.properties[position])
                        }

                        if (node.methods.isNotEmpty()) {
                            // Remove Last from the stack.
                            analyzer.memory.removeLastFromStack()

                            return analyzer.nextNode(node.methods.first())
                        }
                    }
                }
                in signalEndFirstMethod until signalEndFirstMethod + node.methods.size -> {
                    val position = (signal - signalEndFirstMethod) + 1

                    val result = analyzer.memory.getLastFromStack() as LxmLogic

                    if (result.primitive) {
                        if (position < node.methods.size) {
                            // Remove Last from the stack.
                            analyzer.memory.removeLastFromStack()

                            return analyzer.nextNode(node.methods[position])
                        }
                    }
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
