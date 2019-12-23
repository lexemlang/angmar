package org.lexem.angmar.compiler.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*

/**
 * Compiler for [AccessExpressionNode].
 */
internal class AccessExpressionCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: AccessExpressionNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var element: CompiledNode
    var isIdentifier = false
    var modifiers = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AccessExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: AccessExpressionNode): CompiledNode {
            val result = AccessExpressionCompiled(parent, parentSignal, node)
            result.element = node.element.compile(result, AccessExpressionAnalyzer.signalEndElement)
            result.isIdentifier = node.element is IdentifierNode

            for (modifier in node.modifiers) {
                val compiledModifier = modifier.compile(result,
                        result.modifiers.size + AccessExpressionAnalyzer.signalEndFirstModifier)
                result.modifiers.add(compiledModifier)
            }

            return result
        }
    }
}
