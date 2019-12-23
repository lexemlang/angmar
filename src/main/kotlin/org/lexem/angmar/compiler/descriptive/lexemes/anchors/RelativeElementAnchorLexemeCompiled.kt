package org.lexem.angmar.compiler.descriptive.lexemes.anchors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*

/**
 * Compiler for [RelativeElementAnchorLexemeNode].
 */
internal class RelativeElementAnchorLexemeCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: RelativeElementAnchorLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var type: RelativeElementAnchorLexemeNode.RelativeAnchorType
    var value: CompiledNode? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            RelativeElementAnchorLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: RelativeElementAnchorLexemeNode): CompiledNode {
            val result = RelativeElementAnchorLexemeCompiled(parent, parentSignal, node)
            result.type = node.type
            result.value = node.value?.compile(result, RelativeElementAnchorLexemeAnalyzer.signalEndValue)

            return result
        }
    }
}
