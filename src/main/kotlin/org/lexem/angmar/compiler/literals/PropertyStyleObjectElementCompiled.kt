package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [PropertyStyleObjectElementNode].
 */
internal class PropertyStyleObjectElementCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: PropertyStyleObjectElementNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var key: CompiledNode
    lateinit var value: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PropertyStyleObjectElementAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: PropertyStyleObjectElementNode): PropertyStyleObjectElementCompiled {
            val result = PropertyStyleObjectElementCompiled(parent, parentSignal, node)
            result.key = node.key.compile(result, PropertyStyleObjectElementAnalyzer.signalEndKey)
            result.value = node.value.compile(result, PropertyStyleObjectElementAnalyzer.signalEndValue)

            return result
        }
    }
}
