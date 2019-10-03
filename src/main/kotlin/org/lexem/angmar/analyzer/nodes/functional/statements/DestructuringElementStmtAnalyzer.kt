package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
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
                return analyzer.nextNode(node.alias)
            }
            signalEndAlias -> {
                if (node.original != null) {
                    val alias = analyzer.memory.popStack() as LxmString
                    val original = analyzer.memory.popStack() as LxmString
                    val destructuring = analyzer.memory.popStack() as LxmDestructuring

                    destructuring.addElement(original.primitive, alias.primitive, node.isConstant)

                    analyzer.memory.pushStack(destructuring)
                } else {
                    val alias = analyzer.memory.popStack() as LxmString
                    val destructuring = analyzer.memory.popStack() as LxmDestructuring

                    destructuring.addElement(alias.primitive, node.isConstant)

                    analyzer.memory.pushStack(destructuring)
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
