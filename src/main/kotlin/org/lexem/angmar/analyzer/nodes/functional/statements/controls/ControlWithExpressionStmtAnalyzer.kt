package org.lexem.angmar.analyzer.nodes.functional.statements.controls

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.functional.statements.controls.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.statements.controls.*


/**
 * Analyzer for control statements with a expression.
 */
internal object ControlWithExpressionStmtAnalyzer {
    const val signalEndTag = AnalyzerNodesCommons.signalStart + 1
    const val signalEndExpression = signalEndTag + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ControlWithExpressionStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.tag != null) {
                    return analyzer.nextNode(node.tag)
                }

                return analyzer.nextNode(node.expression)
            }
            signalEndTag -> {
                // Move Last to Key in stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                // Creates the control value and throws the control signal.
                if (node.tag != null) {
                    val value = analyzer.memory.getLastFromStack()
                    val tag = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Key) as LxmString

                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Control,
                            LxmControl.from(node.keyword, tag.primitive, value, node))

                    // Remove Key from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                } else {
                    val value = analyzer.memory.getLastFromStack()

                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Control,
                            LxmControl.from(node.keyword, null, value, node))
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                when (node.keyword) {
                    ControlWithExpressionStmtNode.returnKeyword -> {
                        return analyzer.nextNode(node.parent, AnalyzerNodesCommons.signalReturnControl)
                    }
                    else -> throw AngmarUnreachableException()
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
