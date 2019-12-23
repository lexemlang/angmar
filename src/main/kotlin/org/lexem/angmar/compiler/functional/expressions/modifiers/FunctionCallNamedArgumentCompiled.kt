package org.lexem.angmar.compiler.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Compiler for [FunctionCallNamedArgumentNode].
 */
internal class FunctionCallNamedArgumentCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: FunctionCallNamedArgumentNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var identifier: CompiledNode
    lateinit var expression: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionCallNamedArgumentAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: FunctionCallNamedArgumentNode): FunctionCallNamedArgumentCompiled {
            val result = FunctionCallNamedArgumentCompiled(parent, parentSignal, node)
            result.identifier = node.identifier.compile(result, FunctionCallNamedArgumentAnalyzer.signalEndIdentifier)
            result.expression = node.expression.compile(result, FunctionCallNamedArgumentAnalyzer.signalEndExpression)

            return result
        }
    }
}
