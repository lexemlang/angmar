package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [StringNode].
 */
internal class StringCompiled private constructor(parent: CompiledNode?, parentSignal: Int, parserNode: StringNode) :
        CompiledNode(parent, parentSignal, parserNode) {
    val elements = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = StringAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: StringNode): CompiledNode {
            val result = StringCompiled(parent, parentSignal, node)

            var last = ""
            for ((i, escape) in node.escapes.withIndex()) {
                // Join to the last.
                last += node.texts[i]

                val compiledEscape = escape.compile(result, result.elements.size + StringAnalyzer.signalEndFirstElement)

                if (compiledEscape is ConstantCompiled) {
                    last += if (escape is EscapedExpressionNode) {
                        compiledEscape.value.toString()
                    } else {
                        val value = compiledEscape.value as LxmInteger
                        Character.toChars(value.primitive).joinToString("")
                    }
                } else {
                    if (last.isNotEmpty()) {
                        result.elements.add(
                                ConstantCompiled(result, result.elements.size + StringAnalyzer.signalEndFirstElement,
                                        node, LxmString.from(last)))
                        result.elements.add(compiledEscape.changeParentSignal(
                                result.elements.size + StringAnalyzer.signalEndFirstElement))
                    } else {
                        result.elements.add(compiledEscape)
                    }

                    last = ""
                }
            }

            last += node.texts.last()

            // Constant string.
            if (result.elements.isEmpty()) {
                return ConstantCompiled(parent, parentSignal, node, LxmString.from(last))
            }

            if (last.isNotEmpty()) {
                result.elements.add(
                        ConstantCompiled(result, result.elements.size + StringAnalyzer.signalEndFirstElement, node,
                                LxmString.from(last)))
            }

            return result
        }
    }
}
