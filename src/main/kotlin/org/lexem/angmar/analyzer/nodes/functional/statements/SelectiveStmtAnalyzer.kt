package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for selective statements.
 */
internal object SelectiveStmtAnalyzer {
    const val signalEndCondition = AnalyzerNodesCommons.signalStart + 1
    const val signalEndTag = signalEndCondition + 1
    const val signalEndFirstCase = signalEndTag + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: SelectiveStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate an intermediate context.
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                if (node.condition != null) {
                    return analyzer.nextNode(node.condition)
                }

                // Add null as value.
                analyzer.memory.pushStack(LxmNil)

                if (node.tag != null) {
                    return analyzer.nextNode(node.tag)
                }

                return analyzer.nextNode(node.cases[0])
            }
            signalEndCondition -> {
                if (node.tag != null) {
                    return analyzer.nextNode(node.tag)
                }

                return analyzer.nextNode(node.cases[0])
            }
            signalEndTag -> {
                // Set the name of the context.
                val identifier = analyzer.memory.popStack() as LxmString
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenContextTag, identifier)

                return analyzer.nextNode(node.cases[0])
            }
            in signalEndFirstCase until signalEndFirstCase + node.cases.size -> {
                // Check the result.
                val result = analyzer.memory.popStack() as LxmLogic
                if (!result.primitive) {
                    val position = (signal - signalEndFirstCase) + 1
                    if (position < node.cases.size) {
                        return analyzer.nextNode(node.cases[position])
                    }
                }

                // Remove the condition value.
                analyzer.memory.popStack()

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)
            }
            // Process the control signal.
            AnalyzerNodesCommons.signalExitControl -> {
                val control = analyzer.memory.popStack() as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                // Remove the condition value.
                analyzer.memory.popStack()

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    analyzer.memory.pushStack(control)
                    return analyzer.nextNode(node.parent, signal)
                }
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                // Remove the condition value.
                val control = analyzer.memory.popStack() as LxmControl
                analyzer.memory.popStack()
                analyzer.memory.pushStack(control)

                return analyzer.nextNode(node.parent, signal)
            }
            AnalyzerNodesCommons.signalRestartControl, AnalyzerNodesCommons.signalReturnControl -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                // Remove the condition value.
                val control = analyzer.memory.popStack() as LxmControl
                analyzer.memory.popStack()
                analyzer.memory.pushStack(control)

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
