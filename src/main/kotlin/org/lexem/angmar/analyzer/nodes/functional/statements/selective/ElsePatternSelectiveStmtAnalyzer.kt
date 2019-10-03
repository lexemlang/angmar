package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.selective.*


/**
 * Analyzer for else patterns of the selective statements.
 */
internal object ElsePatternSelectiveStmtAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ElsePatternSelectiveStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Remove the value.
                analyzer.memory.popStack()

                analyzer.memory.pushStack(LxmLogic.True)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
