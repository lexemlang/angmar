package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for elements of destructuring.
 */
internal object DestructuringElementStmtAnalyzer {
    const val signalEndOriginal = AnalyzerNodesCommons.signalStart + 1
    const val signalEndAlias = signalEndOriginal + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: DestructuringElementStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.original != null) {
                    return analyzer.nextNode(node.original)
                }

                return analyzer.nextNode(node.alias)
            }
            signalEndOriginal -> {
                // Move Last to Key in the stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                return analyzer.nextNode(node.alias)
            }
            signalEndAlias -> {
                if (node.original != null) {
                    val alias = analyzer.memory.getLastFromStack() as LxmString
                    val original = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Key) as LxmString
                    val destructuring =
                            analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Destructuring) as LxmDestructuring

                    destructuring.addElement(original.primitive, alias.primitive, node.isConstant)

                    // Remove Last and Key from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                    analyzer.memory.removeLastFromStack()
                } else {
                    val alias = analyzer.memory.getLastFromStack() as LxmString
                    val destructuring =
                            analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Destructuring) as LxmDestructuring

                    destructuring.addElement(alias.primitive, node.isConstant)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
