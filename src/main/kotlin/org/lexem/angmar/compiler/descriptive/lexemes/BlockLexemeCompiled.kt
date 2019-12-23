package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [BlockLexemeNode].
 */
internal class BlockLexemeCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: BlockLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isNegated = false
    var propertyPostfix: LexemePropertyPostfixCompiled? = null
    lateinit var interval: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = BlockLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: BlockLexemeNode): CompiledNode {
            val result = BlockLexemeCompiled(parent, parentSignal, node)
            result.isNegated = node.isNegated
            result.propertyPostfix = node.propertyPostfix?.compile(result, BlockLexemAnalyzer.signalEndPropertyPostfix)
            result.interval = node.interval.compile(result, BlockLexemAnalyzer.signalEndInterval)

            // Constant text.
            val interval = result.interval
            if (interval is ConstantCompiled) {
                val itv = (interval.value as LxmInterval).primitive

                // Empty text lexeme.
                if (itv.isEmpty) {
                    return if (result.isNegated) {
                        ConstantCompiled(parent, parentSignal, node, LxmString.Empty)
                    } else {
                        InitBacktrackingCompiled(parent, parentSignal, node)
                    }
                }
            }

            return result
        }
    }
}
