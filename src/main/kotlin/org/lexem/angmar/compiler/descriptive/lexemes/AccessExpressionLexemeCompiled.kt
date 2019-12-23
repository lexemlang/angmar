package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [AccessExpressionLexemeNode].
 */
internal class AccessExpressionLexemeCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: AccessExpressionLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var element: CompiledNode
    var modifiers = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AccessExpressionLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: AccessExpressionLexemeNode): AccessExpressionLexemeCompiled {
            val result = AccessExpressionLexemeCompiled(parent, parentSignal, node)
            result.element = node.element.compile(result, AccessExpressionLexemeAnalyzer.signalEndElement)

            for (modifier in node.modifiers) {
                val compiledModifier = modifier.compile(result,
                        result.modifiers.size + AccessExpressionLexemeAnalyzer.signalEndFirstModifier)

                result.modifiers.add(compiledModifier)
            }

            return result
        }
    }
}
