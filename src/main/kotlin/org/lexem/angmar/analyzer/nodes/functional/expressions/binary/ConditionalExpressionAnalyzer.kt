package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.functional.expressions.binary.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Analyzer for conditional expressions.
 */
internal object ConditionalExpressionAnalyzer {
    const val signalEndFirstExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ConditionalExpressionCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expressions.first())
            }
            in signalEndFirstExpression until signalEndFirstExpression + node.expressions.size -> {
                val position = (signal - signalEndFirstExpression) + 1

                var result = analyzer.memory.getLastFromStack()
                if (result is LexemSetter) {
                    result = AnalyzerNodesCommons.resolveSetter(analyzer.memory, result)

                    analyzer.memory.replaceLastStackCell(result)
                }

                val resultAsTruthy = RelationalFunctions.isTruthy(result)

                when (node.operator) {
                    ConditionalExpressionNode.andOperator -> {
                        // Evaluate the next operand.
                        if (resultAsTruthy && position < node.expressions.size) {
                            // Remove Last from the stack.
                            analyzer.memory.removeLastFromStack()

                            return analyzer.nextNode(node.expressions[position])
                        }
                    }
                    ConditionalExpressionNode.orOperator -> {
                        // Evaluate the next operand.
                        if (!resultAsTruthy && position < node.expressions.size) {
                            // Remove Last from the stack.
                            analyzer.memory.removeLastFromStack()

                            return analyzer.nextNode(node.expressions[position])
                        }
                    }
                    ConditionalExpressionNode.xorOperator -> {
                        if (position == 1) {
                            // Move Last to Left in the stack.
                            analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Left)

                            return analyzer.nextNode(node.expressions[position])
                        }

                        val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left)
                        val leftAsTruthy = RelationalFunctions.isTruthy(left)

                        if (leftAsTruthy) {
                            if (resultAsTruthy) {
                                // Remove Left from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Left)

                                analyzer.memory.replaceLastStackCell(LxmNil)

                                return analyzer.nextNode(node.parent, node.parentSignal)
                            }
                        } else {
                            if (resultAsTruthy) {
                                analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Left, result)
                            } else {
                                analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Left, LxmNil)
                            }
                        }

                        // Remove Last from the stack.
                        analyzer.memory.removeLastFromStack()

                        // Evaluate the next operand.
                        if (position < node.expressions.size) {
                            return analyzer.nextNode(node.expressions[position])
                        }

                        // Move Left to Last in the stack.
                        analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Left)
                    }
                    else -> throw AngmarUnreachableException()
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
