package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [TextLexemeNode].
 */
internal class TextLexemeCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: TextLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isNegated = false
    var propertyPostfix: LexemePropertyPostfixCompiled? = null
    lateinit var text: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = TextLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: TextLexemeNode): CompiledNode {
            val result = TextLexemeCompiled(parent, parentSignal, node)
            result.isNegated = node.isNegated
            result.propertyPostfix = node.propertyPostfix?.compile(result, TextLexemAnalyzer.signalEndPropertyPostfix)
            result.text = node.text.compile(result, TextLexemAnalyzer.signalEndText)

            // Constant text.
            val text = result.text
            if (text is ConstantCompiled) {
                val textStr = (text.value as LxmString).primitive

                // Empty text lexeme.
                if (textStr.isEmpty()) {
                    return if (result.isNegated) {
                        InitBacktrackingCompiled(parent, parentSignal, node)
                    } else {
                        ConstantCompiled(parent, parentSignal, node, LxmString.Empty)
                    }
                }
            }

            return result
        }
    }
}
