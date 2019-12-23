package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.functional.statements.selective.*


/**
 * Analyzer for conditional patterns of the selective statements.
 */
internal object ConditionalPatternSelectiveStmtAnalyzer {
    const val signalEndCondition = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ConditionalPatternSelectiveStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.condition)
            }
            signalEndCondition -> {
                // Evaluate the condition.
                val condition = analyzer.memory.getLastFromStack()
                var conditionTruthy = RelationalFunctions.isTruthy(condition)

                if (node.isUnless) {
                    conditionTruthy = !conditionTruthy
                }

                analyzer.memory.replaceLastStackCell(LxmLogic.from(conditionTruthy))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
