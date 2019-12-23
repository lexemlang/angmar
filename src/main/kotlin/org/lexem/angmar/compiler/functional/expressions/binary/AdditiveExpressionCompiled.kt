package org.lexem.angmar.compiler.functional.expressions.binary

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.binary.*

/**
 * Compiler for [AdditiveExpressionNode].
 */
internal class AdditiveExpressionCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: AdditiveExpressionNode) : CompiledNode(parent, parentSignal, parserNode) {
    val expressions = mutableListOf<CompiledNode>()
    var operators = mutableListOf<String>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AdditiveExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: AdditiveExpressionNode): CompiledNode {
            val result = AdditiveExpressionCompiled(parent, parentSignal, node)

            var last: LexemPrimitive? = null

            // Process the first.
            val compiledExpression =
                    node.expressions.first().compile(result, AdditiveExpressionAnalyzer.signalEndFirstExpression)

            if (compiledExpression is ConstantCompiled) {
                last = compiledExpression.value
            } else {
                result.expressions.add(compiledExpression)
            }

            // Process the rest.
            for ((index, operator) in node.operators.withIndex()) {
                val compiledExpression = node.expressions[index + 1].compile(result,
                        result.expressions.size + AdditiveExpressionAnalyzer.signalEndFirstExpression)

                if (compiledExpression is ConstantCompiled) {
                    if (last == null) {
                        last = compiledExpression.value
                        result.operators.add(operator)
                    } else {
                        last = operate(operator, last, compiledExpression.value, node)
                    }
                } else {
                    if (last != null) {
                        result.expressions.add(ConstantCompiled(result,
                                result.expressions.size + AdditiveExpressionAnalyzer.signalEndFirstExpression, node,
                                last))
                        result.expressions.add(compiledExpression.changeParentSignal(
                                result.expressions.size + AdditiveExpressionAnalyzer.signalEndFirstExpression))
                    } else {
                        result.expressions.add(compiledExpression)
                    }

                    result.operators.add(operator)

                    last = null
                }
            }

            // Constant.
            if (result.expressions.isEmpty()) {
                return ConstantCompiled(parent, parentSignal, node, last!!)
            }

            if (last != null) {
                result.expressions.add(ConstantCompiled(result,
                        result.expressions.size + AdditiveExpressionAnalyzer.signalEndFirstExpression, node, last))
            }

            return result
        }

        /**
         * Operates two values.
         */
        private fun operate(operator: String, left: LexemPrimitive, right: LexemPrimitive,
                node: ParserNode): LexemPrimitive {
            when (operator) {
                AdditiveExpressionNode.additionOperator -> {
                    if (left is LxmString || right is LxmString) {
                        return LxmString.from(left.toString() + right.toString())
                    }

                    return when (left) {
                        is LxmInteger -> when (right) {
                            is LxmFloat -> LxmFloat.from(left.primitive + right.primitive)
                            is LxmInteger -> LxmInteger.from(left.primitive + right.primitive)
                            else -> throw binaryError(AnalyzerCommons.Operators.Add, left, right, node)
                        }
                        is LxmFloat -> when (right) {
                            is LxmFloat -> LxmFloat.from(left.primitive + right.primitive)
                            is LxmInteger -> LxmFloat.from(left.primitive + right.primitive)
                            else -> throw binaryError(AnalyzerCommons.Operators.Add, left, right, node)
                        }
                        is LxmInterval -> when (right) {
                            is LxmInteger -> LxmInterval.from(left.primitive.plus(right.primitive))
                            is LxmInterval -> LxmInterval.from(left.primitive.plus(right.primitive))
                            else -> throw binaryError(AnalyzerCommons.Operators.Add, left, right, node)
                        }
                        else -> throw unitaryError(AnalyzerCommons.Operators.Add, left, node)
                    }
                }
                AdditiveExpressionNode.subtractionOperator -> {
                    return when (left) {
                        is LxmInteger -> when (right) {
                            is LxmFloat -> LxmFloat.from(left.primitive - right.primitive)
                            is LxmInteger -> LxmInteger.from(left.primitive - right.primitive)
                            else -> throw binaryError(AnalyzerCommons.Operators.Sub, left, right, node)
                        }
                        is LxmFloat -> when (right) {
                            is LxmFloat -> LxmFloat.from(left.primitive - right.primitive)
                            is LxmInteger -> LxmFloat.from(left.primitive - right.primitive)
                            else -> throw binaryError(AnalyzerCommons.Operators.Sub, left, right, node)
                        }
                        is LxmInterval -> when (right) {
                            is LxmInteger -> LxmInterval.from(left.primitive.minus(right.primitive))
                            is LxmInterval -> LxmInterval.from(left.primitive.minus(right.primitive))
                            else -> throw binaryError(AnalyzerCommons.Operators.Sub, left, right, node)
                        }
                        else -> throw unitaryError(AnalyzerCommons.Operators.Sub, left, node)
                    }
                }
                else -> throw AngmarUnreachableException()
            }
        }

        /**
         * The error of incorrect type to apply the operator.
         */
        fun unitaryError(operator: String, value: LexemPrimitive, node: ParserNode) =
                AngmarCompilerException(AngmarCompilerExceptionType.IncompatibleType,
                        "Cannot apply the '$operator' operator over the value: (value: $value)") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                        addNote(Consts.Logger.hintTitle, "Review this expression")
                    }
                }

        /**
         * The error of bad combination of types.
         */
        fun binaryError(operator: String, left: LexemPrimitive, right: LexemPrimitive, node: ParserNode) =
                AngmarCompilerException(AngmarCompilerExceptionType.IncompatibleType,
                        "Cannot apply the '$operator' operator over the values: (left: $left, right: $right)") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                        addNote(Consts.Logger.hintTitle, "Review this expression")
                    }
                }
    }
}
