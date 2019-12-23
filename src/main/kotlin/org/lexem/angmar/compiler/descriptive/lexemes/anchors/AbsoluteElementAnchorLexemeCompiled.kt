package org.lexem.angmar.compiler.descriptive.lexemes.anchors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*

/**
 * Compiler for [AbsoluteElementAnchorLexemeNode].
 */
internal class AbsoluteElementAnchorLexemeCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: AbsoluteElementAnchorLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var type: AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType
    lateinit var expression: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AbsoluteElementAnchorLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: AbsoluteElementAnchorLexemeNode): CompiledNode {
            val result = AbsoluteElementAnchorLexemeCompiled(parent, parentSignal, node)
            result.type = node.type
            result.expression = node.expression.compile(result, AbsoluteElementAnchorLexemeAnalyzer.signalEndExpression)

            return result
        }
    }
}
