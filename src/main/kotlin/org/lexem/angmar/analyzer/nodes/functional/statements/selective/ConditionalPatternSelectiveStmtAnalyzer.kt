package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.parser.functional.statements.selective.*


/**
 * Analyzer for conditional patterns of the selective statements.
 */
internal object ConditionalPatternSelectiveStmtAnalyzer {
    const val signalEndCondition = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ConditionalPatternSelectiveStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Remove the value.
                analyzer.memory.popStack()

                return analyzer.nextNode(node.condition)
            }
            signalEndCondition -> {
                // Evaluate the condition.
                val condition = analyzer.memory.popStack()
                var conditionTruthy = RelationalFunctions.isTruthy(condition)

                if (node.isUnless) {
                    conditionTruthy = !conditionTruthy
                }

                analyzer.memory.pushStack(LxmLogic.from(conditionTruthy))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
