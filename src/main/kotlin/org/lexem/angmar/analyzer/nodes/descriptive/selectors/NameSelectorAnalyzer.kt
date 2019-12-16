package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.descriptive.selectors.*


/**
 * Analyzer for names of selectors.
 */
internal object NameSelectorAnalyzer {
    const val signalEndFirstName = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: NameSelectorNode) {
        if (node.isAddition) {
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    return analyzer.nextNode(node.names.first())
                }
                signalEndFirstName -> {
                    val name = analyzer.memory.getLastFromStack() as LxmString
                    val cursor = analyzer.text.saveCursor()
                    val lxmNode = LxmNode(name.primitive, cursor, null, analyzer.memory)
                    val lxmNodeRef = analyzer.memory.add(lxmNode)
                    lxmNode.setTo(analyzer.memory, cursor)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                }
            }
        } else {
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    return analyzer.nextNode(node.names.first())
                }
                in signalEndFirstName until signalEndFirstName + node.names.size -> {
                    val position = (signal - signalEndFirstName) + 1

                    val name = analyzer.memory.getLastFromStack() as LxmString
                    val lxmNode =
                            analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node).dereference(analyzer.memory,
                                    toWrite = false) as LxmNode

                    // Check the name.
                    if (lxmNode.name != name.primitive) {
                        if (position < node.names.size) {
                            // Remove Last from the stack.
                            analyzer.memory.removeLastFromStack()

                            return analyzer.nextNode(node.names[position])
                        }

                        analyzer.memory.replaceLastStackCell(LxmLogic.from(node.isNegated))
                    } else {
                        analyzer.memory.replaceLastStackCell(LxmLogic.from(!node.isNegated))
                    }
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
