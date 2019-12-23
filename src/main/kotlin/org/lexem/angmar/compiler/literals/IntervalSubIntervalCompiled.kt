package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.data.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [IntervalSubIntervalNode].
 */
internal class IntervalSubIntervalCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: IntervalSubIntervalNode) : CompiledNode(parent, parentSignal, parserNode) {
    var operator = IntervalSubIntervalNode.Operator.Add
    val elements = mutableListOf<CompiledNode>()
    var reversed = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            IntervalSubIntervalAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: IntervalSubIntervalNode): CompiledNode {
            val result = IntervalSubIntervalCompiled(parent, parentSignal, node)
            result.operator = node.operator
            result.reversed = node.reversed

            var last: IntegerInterval? = null
            for (element in node.elements) {
                val compiledElement = element.compile(result,
                        IntervalSubIntervalAnalyzer.signalEndFirstElement + result.elements.size)

                when (compiledElement) {
                    is IntervalConstantCompiled -> {
                        last = operateInterval(compiledElement.operator, last ?: IntegerInterval.Empty,
                                compiledElement.interval)
                    }
                    is IntervalElementCompiled -> {
                        if (compiledElement.constantValue != null) {
                            last = operateInterval(IntervalSubIntervalNode.Operator.Add, last ?: IntegerInterval.Empty,
                                    IntegerInterval.new(compiledElement.constantValue!!))
                        }
                    }
                    else -> {
                        if (last != null) {
                            result.elements.add(ConstantCompiled(result,
                                    IntervalSubIntervalAnalyzer.signalEndFirstElement + result.elements.size, node,
                                    LxmInterval.from(last)))
                            result.elements.add(compiledElement.changeParentSignal(
                                    IntervalSubIntervalAnalyzer.signalEndFirstElement + result.elements.size))
                        } else {
                            result.elements.add(compiledElement)
                        }

                        last = null
                    }
                }
            }

            // Constant.
            if (result.elements.isEmpty()) {
                last = last ?: IntegerInterval.Empty

                if (node.reversed) {
                    last = !last
                }

                return IntervalConstantCompiled(parent, parentSignal, node, node.operator, last)
            }

            if (last != null) {
                result.elements.add(ConstantCompiled(result,
                        IntervalSubIntervalAnalyzer.signalEndFirstElement + result.elements.size, node,
                        LxmInterval.from(last)))
            }

            return result
        }

        /**
         * Operates both intervals depending on the operator.
         */
        fun operateInterval(operator: IntervalSubIntervalNode.Operator, left: IntegerInterval, right: IntegerInterval) =
                when (operator) {
                    IntervalSubIntervalNode.Operator.Add -> left.plus(right)
                    IntervalSubIntervalNode.Operator.Sub -> left.minus(right)
                    IntervalSubIntervalNode.Operator.Common -> left.common(right)
                    IntervalSubIntervalNode.Operator.NotCommon -> left.notCommon(right)
                }
    }
}
