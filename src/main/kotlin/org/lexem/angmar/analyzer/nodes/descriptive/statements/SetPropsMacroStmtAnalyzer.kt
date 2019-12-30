package org.lexem.angmar.analyzer.nodes.descriptive.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.descriptive.statements.*


/**
 * Analyzer for set properties macro statement.
 */
internal object SetPropsMacroStmtAnalyzer {
    const val signalEndProperties = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: SetPropsMacroStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.properties)
            }
            signalEndProperties -> {
                val values =
                        analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = false) as LxmObject
                val properties = AnalyzerCommons.getCurrentNodeProps(analyzer.memory, toWrite = true)

                for ((key, value) in values.getAllIterableProperties()) {
                    properties.setProperty( key, value.value)
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                analyzer.memory.addToStackAsLast(LxmNil)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
