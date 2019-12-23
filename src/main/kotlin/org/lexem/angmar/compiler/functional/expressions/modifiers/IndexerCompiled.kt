package org.lexem.angmar.compiler.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Compiler for [IndexerNode].
 */
internal class IndexerCompiled private constructor(parent: CompiledNode?, parentSignal: Int, parserNode: IndexerNode) :
        CompiledNode(parent, parentSignal, parserNode) {
    lateinit var expression: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = IndexerAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: IndexerNode): IndexerCompiled {
            val result = IndexerCompiled(parent, parentSignal, node)
            result.expression = node.expression.compile(result, IndexerAnalyzer.signalEndExpression)

            return result
        }
    }
}
