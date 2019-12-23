package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.data.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [UnicodeIntervalSubIntervalNode].
 */
internal class UnicodeIntervalSubIntervalCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: UnicodeIntervalSubIntervalNode) : CompiledNode(parent, parentSignal, parserNode) {
    var operator = IntervalSubIntervalNode.Operator.Add
    val elements = mutableListOf<CompiledNode>()
    var reversed = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            UnicodeIntervalSubIntervalAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: UnicodeIntervalSubIntervalNode): CompiledNode {
            val result = UnicodeIntervalSubIntervalCompiled(parent, parentSignal, node)
            result.operator = node.operator
            result.reversed = node.reversed

            var last: IntegerInterval? = null
            for (element in node.elements) {
                val compiledElement = element.compile(result,
                        IntervalSubIntervalAnalyzer.signalEndFirstElement + result.elements.size)

                when (compiledElement) {
                    is IntervalConstantCompiled -> {
                        last = IntervalSubIntervalCompiled.operateInterval(compiledElement.operator,
                                last ?: IntegerInterval.Empty, compiledElement.interval)
                    }
                    is UnicodeIntervalElementCompiled -> {
                        if (compiledElement.constantValue != null) {
                            last = IntervalSubIntervalCompiled.operateInterval(IntervalSubIntervalNode.Operator.Add,
                                    last ?: IntegerInterval.Empty, IntegerInterval.new(compiledElement.constantValue!!))
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
                    last = last.unicodeNot()
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
    }
}
