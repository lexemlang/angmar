package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Analyzer for right expressions.
 */
internal object RightExpressionAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: RightExpressionNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                // Check is not a setter.
                var value = analyzer.memory.popStack()

                if (value is LexemSetter) {
                    value = value.resolve(analyzer.memory)
                }

                analyzer.memory.pushStackIgnoringReferenceCount(value)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
