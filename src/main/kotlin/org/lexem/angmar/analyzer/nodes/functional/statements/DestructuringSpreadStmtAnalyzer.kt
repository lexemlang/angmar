package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for spread element of destructuring.
 */
internal object DestructuringSpreadStmtAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: DestructuringSpreadStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.identifier)
            }
            signalEndIdentifier -> {
                val identifier = analyzer.memory.popStack() as LxmString
                val destructuring = analyzer.memory.popStack() as LxmDestructuring

                destructuring.setSpread(identifier.primitive, node.isConstant)

                analyzer.memory.pushStack(destructuring)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
