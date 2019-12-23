package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [MapElementNode].
 */
internal class MapElementCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: MapElementNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var key: CompiledNode
    lateinit var value: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = MapElementAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: MapElementNode): MapElementCompiled {
            val result = MapElementCompiled(parent, parentSignal, node)
            result.key = node.key.compile(result, MapElementAnalyzer.signalEndKey)
            result.value = node.value.compile(result, MapElementAnalyzer.signalEndValue)

            return result
        }
    }
}
