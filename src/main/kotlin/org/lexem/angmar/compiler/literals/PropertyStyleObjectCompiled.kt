package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [PropertyStyleObjectNode].
 */
internal class PropertyStyleObjectCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: PropertyStyleObjectNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var block: CompiledNode
    var isConstant = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PropertyStyleObjectAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: PropertyStyleObjectNode): PropertyStyleObjectCompiled {
            val result = PropertyStyleObjectCompiled(parent, parentSignal, node)
            result.block = node.block.compile(result, PropertyStyleObjectAnalyzer.signalEndBlock)
            result.isConstant = node.isConstant

            return result
        }
    }
}
