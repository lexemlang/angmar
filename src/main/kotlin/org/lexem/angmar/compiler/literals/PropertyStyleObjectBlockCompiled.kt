package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [PropertyStyleObjectBlockNode].
 */
internal class PropertyStyleObjectBlockCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: PropertyStyleObjectBlockNode) : CompiledNode(parent, parentSignal, parserNode) {
    val positiveElements = mutableListOf<CompiledNode>()
    val negativeElements = mutableListOf<CompiledNode>()
    val setElements = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PropertyStyleObjectBlockAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: PropertyStyleObjectBlockNode): PropertyStyleObjectBlockCompiled {
            val result = PropertyStyleObjectBlockCompiled(parent, parentSignal, node)

            for (element in node.positiveElements) {
                val compiledElement = element.compile(result,
                        PropertyStyleObjectBlockAnalyzer.signalEndFirstPositiveElement + result.positiveElements.size)
                result.positiveElements.add(compiledElement)
            }

            for (element in node.negativeElements) {
                val compiledElement = element.compile(result,
                        PropertyStyleObjectBlockAnalyzer.signalEndFirstPositiveElement + result.positiveElements.size + result.negativeElements.size)
                result.negativeElements.add(compiledElement)
            }

            for (element in node.setElements) {
                val compiledElement = element.compile(result,
                        PropertyStyleObjectBlockAnalyzer.signalEndFirstPositiveElement + result.positiveElements.size + result.negativeElements.size + result.setElements.size)
                result.setElements.add(compiledElement)
            }

            return result
        }
    }
}
