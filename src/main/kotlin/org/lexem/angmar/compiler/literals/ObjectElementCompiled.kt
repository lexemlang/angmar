package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [ObjectElementNode].
 */
internal class ObjectElementCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: ObjectElementNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var key: CompiledNode
    lateinit var value: CompiledNode
    var isConstant = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ObjectElementAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ObjectElementNode): ObjectElementCompiled {
            val result = ObjectElementCompiled(parent, parentSignal, node)
            result.key = node.key.compile(result, ObjectElementAnalyzer.signalEndKey)
            result.value = node.value.compile(result, ObjectElementAnalyzer.signalEndValue)
            result.isConstant = node.isConstant

            return result
        }
    }
}
