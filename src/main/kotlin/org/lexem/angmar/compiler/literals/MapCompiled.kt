package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [MapNode].
 */
internal class MapCompiled private constructor(parent: CompiledNode?, parentSignal: Int, parserNode: MapNode) :
        CompiledNode(parent, parentSignal, parserNode) {
    val elements = mutableListOf<CompiledNode>()
    var isConstant = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = MapAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: MapNode): MapCompiled {
            val result = MapCompiled(parent, parentSignal, node)
            result.isConstant = node.isConstant

            for (element in node.elements) {
                val compiledElement = element.compile(result, result.elements.size + MapAnalyzer.signalEndFirstElement)
                result.elements.add(compiledElement)
            }

            return result
        }
    }
}
