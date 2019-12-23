package org.lexem.angmar.compiler.descriptive.lexemes.anchors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*

/**
 * Compiler for [AbsoluteAnchorLexemeNode].
 */
internal class AbsoluteAnchorLexemeCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: AbsoluteAnchorLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isNegated = false
    var elements = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AbsoluteAnchorLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: AbsoluteAnchorLexemeNode): CompiledNode {
            val result = AbsoluteAnchorLexemeCompiled(parent, parentSignal, node)
            result.isNegated = node.isNegated

            for (element in node.elements) {
                val compiledElement = element.compile(result,
                        result.elements.size + AbsoluteAnchorLexemeAnalyzer.signalEndFirstElement)

                result.elements.add(compiledElement)
            }

            return result
        }
    }
}
