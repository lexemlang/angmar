package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.functional.statements.*


/**
 * Analyzer for spread element of destructuring.
 */
internal object DestructuringSpreadStmtAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: DestructuringSpreadStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.identifier)
            }
            signalEndIdentifier -> {
                val identifier = analyzer.memory.getLastFromStack() as LxmString
                val destructuring =
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Destructuring) as LxmDestructuring

                destructuring.setSpread(identifier.primitive, node.isConstant)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
