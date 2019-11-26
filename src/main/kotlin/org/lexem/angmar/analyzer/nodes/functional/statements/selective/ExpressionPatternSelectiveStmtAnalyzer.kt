package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.parser.functional.statements.selective.*


/**
 * Analyzer for expression patterns of the selective statements.
 */
internal object ExpressionPatternSelectiveStmtAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1
    const val signalEndConditional = signalEndExpression + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ExpressionPatternSelectiveStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                // Evaluate the expression.
                val value = analyzer.memory.getLastFromStack()
                val mainValue = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)

                if (RelationalFunctions.lxmEquals(analyzer.memory, value, mainValue)) {
                    if (node.conditional != null) {
                        analyzer.memory.removeLastFromStack()

                        return analyzer.nextNode(node.conditional)
                    }

                    analyzer.memory.replaceLastStackCell(LxmLogic.True)
                } else {
                    analyzer.memory.replaceLastStackCell(LxmLogic.False)
                }
            }
            signalEndConditional -> {
                // Returns the value of the conditional.
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
