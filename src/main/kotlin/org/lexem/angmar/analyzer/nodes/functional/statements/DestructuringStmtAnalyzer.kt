package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
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
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Destructuring, LxmDestructuring())

                if (node.alias != null) {
                    return analyzer.nextNode(node.alias)
                }

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }

                if (node.spread != null) {
                    return analyzer.nextNode(node.spread)
                }

                // Move Destructuring to Last in the stack.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Destructuring)
            }
            signalEndAlias -> {
                // Add alias.
                val alias = analyzer.memory.getLastFromStack() as LxmString
                val destructuring =
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Destructuring) as LxmDestructuring

                destructuring.setAlias(alias.primitive)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements[0])
                }

                if (node.spread != null) {
                    return analyzer.nextNode(node.spread)
                }

                // Move Destructuring to Last in the stack.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Destructuring)
            }
            in signalEndFirstElement until signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                if (node.spread != null) {
                    return analyzer.nextNode(node.spread)
                }

                // Move Destructuring to Last in the stack.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Destructuring)
            }
            signalEndSpread -> {
                // Move Destructuring to Last in the stack.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Destructuring)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
