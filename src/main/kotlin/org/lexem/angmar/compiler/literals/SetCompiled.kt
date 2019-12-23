package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [SetNode].
 */
internal class SetCompiled private constructor(parent: CompiledNode?, parentSignal: Int, parserNode: SetNode) :
        CompiledNode(parent, parentSignal, parserNode) {
    lateinit var list: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = SetAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: SetNode): SetCompiled {
            val result = SetCompiled(parent, parentSignal, node)
            result.list = node.list.compile(result, SetAnalyzer.signalEndList)

            return result
        }
    }
}
