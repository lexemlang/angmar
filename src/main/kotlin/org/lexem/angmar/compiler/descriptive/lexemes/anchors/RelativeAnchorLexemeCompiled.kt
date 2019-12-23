package org.lexem.angmar.compiler.descriptive.lexemes.anchors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*

/**
 * Compiler for [RelativeAnchorLexemeNode].
 */
internal class RelativeAnchorLexemeCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: RelativeAnchorLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isNegated = false
    var elements = mutableListOf<CompiledNode>()
    lateinit var type: RelativeAnchorLexemeNode.RelativeAnchorType

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            RelativeAnchorLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: RelativeAnchorLexemeNode): CompiledNode {
            val result = RelativeAnchorLexemeCompiled(parent, parentSignal, node)
            result.isNegated = node.isNegated
            result.type = node.type

            for (element in node.elements) {
                val compiledElement = element.compile(result,
                        result.elements.size + RelativeAnchorLexemeAnalyzer.signalEndFirstElement)

                result.elements.add(compiledElement)
            }

            return result
        }
    }
}
