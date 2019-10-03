package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for variable declaration.
 */
internal object ExpressionStmtAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ExpressionStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                // Remove the value of the stack.
                val value = analyzer.memory.popStack()

                if (value is LxmReference) {
                    value.decreaseReferenceCount(analyzer.memory)
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
