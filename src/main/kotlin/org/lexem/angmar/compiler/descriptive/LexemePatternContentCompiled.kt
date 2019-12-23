package org.lexem.angmar.compiler.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.descriptive.*

/**
 * Compiler for [LexemePatternContentNode].
 */
internal class LexemePatternContentCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: LexemePatternContentNode) : CompiledNode(parent, parentSignal, parserNode) {
    val lexemes = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            LexemePatternContentAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: LexemePatternContentNode): CompiledNode {
            val result = LexemePatternContentCompiled(parent, parentSignal, node)

            loop@ for (lexeme in node.lexemes) {
                val compiledLexeme =
                        lexeme.compile(result, result.lexemes.size + LexemePatternContentAnalyzer.signalEndFirstLexeme)

                when (compiledLexeme) {
                    is NoOperationCompiled -> {
                        // Skip
                    }
                    is InitBacktrackingCompiled -> {
                        // Exit because the rest lexemes will never be executed.
                        result.lexemes.add(compiledLexeme)
                        break@loop
                    }
                    else -> result.lexemes.add(compiledLexeme)
                }
            }

            // All constant.
            if (result.lexemes.isEmpty()) {
                return NoOperationCompiled(parent, parentSignal, node)
            }

            // Only init backtracking.
            if (result.lexemes.size == 1 && result.lexemes.first() is InitBacktrackingCompiled) {
                return result.lexemes.first().linkTo(parent, parentSignal, node)
            }

            return result
        }
    }
}
