package org.lexem.angmar.compiler.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Compiler for [FunctionCallExpressionPropertiesNode].
 */
internal class FunctionCallExpressionPropertiesCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: FunctionCallExpressionPropertiesNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var value: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionCallExpressionPropertiesAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: FunctionCallExpressionPropertiesNode): FunctionCallExpressionPropertiesCompiled {
            val result = FunctionCallExpressionPropertiesCompiled(parent, parentSignal, node)
            result.value = node.value.compile(result, FunctionCallExpressionPropertiesAnalyzer.signalEndValue)

            return result
        }
    }
}
