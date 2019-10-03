package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for destructuring.
 */
internal object DestructuringStmtAnalyzer {
    const val signalEndAlias = AnalyzerNodesCommons.signalStart + 1
    const val signalEndSpread = signalEndAlias + 1
    const val signalEndFirstElement = signalEndSpread + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: DestructuringStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Adds the destructuring object to the stack.
                analyzer.memory.pushStack(LxmDestructuring())

                if (node.alias != null) {
                    return analyzer.nextNode(node.alias)
                }

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }

                if (node.spread != null) {
                    return analyzer.nextNode(node.spread)
                }
            }
            signalEndAlias -> {
                // Add alias.
                val alias = analyzer.memory.popStack() as LxmString
                val destructuring = analyzer.memory.popStack() as LxmDestructuring

                destructuring.setAlias(alias.primitive)

                analyzer.memory.pushStack(destructuring)

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }

                if (node.spread != null) {
                    return analyzer.nextNode(node.spread)
                }
            }
            in signalEndFirstElement until signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                if (node.spread != null) {
                    return analyzer.nextNode(node.spread)
                }
            }
            signalEndSpread -> {
                // Return the destructuring object.
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
