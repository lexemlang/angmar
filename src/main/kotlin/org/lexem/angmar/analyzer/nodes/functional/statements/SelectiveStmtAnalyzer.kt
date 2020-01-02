package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.functional.statements.*


/**
 * Analyzer for selective statements.
 */
internal object SelectiveStmtAnalyzer {
    const val signalEndCondition = AnalyzerNodesCommons.signalStart + 1
    const val signalEndTag = signalEndCondition + 1
    const val signalEndFirstCase = signalEndTag + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: SelectiveStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate an intermediate context.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory, context.type)

                if (node.condition != null) {
                    return analyzer.nextNode(node.condition)
                }

                // Add null as condition.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectiveCondition, LxmNil)

                if (node.tag != null) {
                    return analyzer.nextNode(node.tag)
                }

                return analyzer.nextNode(node.cases.first())
            }
            signalEndCondition -> {
                // Move Last to SelectiveCondition in the stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.SelectiveCondition)

                if (node.tag != null) {
                    return analyzer.nextNode(node.tag)
                }

                return analyzer.nextNode(node.cases.first())
            }
            signalEndTag -> {
                // Set the name of the context.
                val identifier = analyzer.memory.getLastFromStack() as LxmString
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenContextTag, identifier)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                return analyzer.nextNode(node.cases.first())
            }
            in signalEndFirstCase..signalEndFirstCase + node.cases.size -> {
                // Check the result.
                val result = analyzer.memory.getLastFromStack() as LxmLogic

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (!result.primitive) {
                    val position = (signal - signalEndFirstCase) + 1
                    if (position < node.cases.size) {
                        return analyzer.nextNode(node.cases[position])
                    }
                }

                finish(analyzer)
            }
            // Process the control signal.
            AnalyzerNodesCommons.signalExitControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                finish(analyzer)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl -> {
                finish(analyzer)

                return analyzer.nextNode(node.parent, signal)
            }
            AnalyzerNodesCommons.signalRestartControl, AnalyzerNodesCommons.signalReturnControl -> {
                finish(analyzer)

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Process the finalization of the loop.
     */
    private fun finish(analyzer: LexemAnalyzer) {
        // Remove the intermediate context.
        AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

        // Remove SelectiveCondition from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
    }
}
