package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.selective.*


/**
 * Analyzer for cases of the selective statements.
 */
internal object SelectiveCaseStmtAnalyzer {
    const val signalEndBlock = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFirstPattern = signalEndBlock + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: SelectiveCaseStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Duplicate the main value.
                val mainValue = analyzer.memory.popStack()
                analyzer.memory.pushStack(mainValue)
                analyzer.memory.pushStack(mainValue)

                // Generate an intermediate context.
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                return analyzer.nextNode(node.patterns[0])
            }
            in signalEndFirstPattern until signalEndFirstPattern + node.patterns.size -> {
                // Check the result.
                val result = analyzer.memory.popStack() as LxmLogic
                if (result.primitive) {
                    return analyzer.nextNode(node.block)
                }

                val position = (signal - signalEndFirstPattern) + 1
                if (position < node.patterns.size) {
                    // Duplicate the main value.
                    val mainValue = analyzer.memory.popStack()
                    analyzer.memory.pushStack(mainValue)
                    analyzer.memory.pushStack(mainValue)

                    return analyzer.nextNode(node.patterns[position])
                }

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                // Set the nok flag.
                analyzer.memory.pushStack(LxmLogic.False)
            }
            signalEndBlock -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                // Set the ok flag.
                analyzer.memory.pushStack(LxmLogic.True)
            }
            // Process the control signal.
            AnalyzerNodesCommons.signalExitControl -> {
                val control = analyzer.memory.popStack() as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                // Propagate the control signal.
                if (contextTag == null || control.tag != contextTag) {
                    analyzer.memory.pushStack(control)
                    return analyzer.nextNode(node.parent, signal)
                }

                // Set the ok flag.
                analyzer.memory.pushStack(LxmLogic.True)
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                return analyzer.nextNode(node.parent, signal)
            }
            AnalyzerNodesCommons.signalRestartControl, AnalyzerNodesCommons.signalReturnControl -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
