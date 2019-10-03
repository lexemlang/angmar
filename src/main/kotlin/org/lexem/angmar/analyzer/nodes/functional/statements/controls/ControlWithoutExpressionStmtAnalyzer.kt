package org.lexem.angmar.analyzer.nodes.functional.statements.controls

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.statements.controls.*


/**
 * Analyzer for control statements without a expression.
 */
internal object ControlWithoutExpressionStmtAnalyzer {
    const val signalEndTag = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ControlWithoutExpressionStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.tag != null) {
                    return analyzer.nextNode(node.tag)
                }

                return operate(analyzer, node)
            }
            signalEndTag -> {
                return operate(analyzer, node)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Creates the control value and throws the control signal.
     */
    fun operate(analyzer: LexemAnalyzer, node: ControlWithoutExpressionStmtNode) {
        if (node.tag != null) {
            val tag = analyzer.memory.popStack() as LxmString

            analyzer.memory.pushStack(LxmControl.from(node.keyword, tag.primitive, null, node))
        } else {
            analyzer.memory.pushStack(LxmControl.from(node.keyword, null, null, node))
        }

        when (node.keyword) {
            ControlWithoutExpressionStmtNode.exitKeyword -> {
                return analyzer.nextNode(node.parent, AnalyzerNodesCommons.signalExitControl)
            }
            ControlWithoutExpressionStmtNode.nextKeyword -> {
                return analyzer.nextNode(node.parent, AnalyzerNodesCommons.signalNextControl)
            }
            ControlWithoutExpressionStmtNode.redoKeyword -> {
                return analyzer.nextNode(node.parent, AnalyzerNodesCommons.signalRedoControl)
            }
            ControlWithoutExpressionStmtNode.restartKeyword -> {
                return analyzer.nextNode(node.parent, AnalyzerNodesCommons.signalRestartControl)
            }
            else -> throw AngmarUnreachableException()
        }
    }
}
