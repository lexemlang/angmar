package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Analyzer for assign expressions.
 */
internal object AssignExpressionAnalyzer {
    const val signalEndLeft = AnalyzerNodesCommons.signalStart + 1
    const val signalEndOperator = signalEndLeft + 1
    const val signalEndRight = signalEndOperator + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AssignExpressionNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.left)
            }
            signalEndLeft -> {
                // Check left.
                val left = analyzer.memory.getLastFromStack()

                if (left !is LexemSetter) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.AssignToConstant,
                            "The returned value by the left expression is a value not a reference, therefore it cannot be assigned. Actual value: $left") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.left.from.position(), node.left.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                // Move Last to Left in the stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Left)

                return analyzer.nextNode(node.right)
            }
            signalEndRight -> {
                return analyzer.nextNode(node.operator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
