package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.primitives.setters.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Analyzer for access expressions.
 */
internal object AccessExpressionAnalyzer {
    const val signalEndElement = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFirstModifier = signalEndElement + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AccessExpressionNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.element)
            }
            signalEndElement -> {
                // Performs the access if the element is an identifier.
                if (node.element is IdentifierNode) {
                    val id = analyzer.memory.getLastFromStack() as LxmString
                    val context = AnalyzerCommons.getCurrentContextReference(analyzer.memory)

                    analyzer.memory.replaceLastStackCell(
                            LxmAccessSetter(analyzer.memory, context, id.primitive, node, node.element))
                }

                // Process the next operand.
                if (node.modifiers.isNotEmpty()) {
                    return analyzer.nextNode(node.modifiers[0])
                }
            }
            in signalEndFirstModifier until signalEndFirstModifier + node.modifiers.size -> {
                val position = (signal - signalEndFirstModifier) + 1

                // Process the next operand
                if (position < node.modifiers.size) {
                    return analyzer.nextNode(node.modifiers[position])
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
