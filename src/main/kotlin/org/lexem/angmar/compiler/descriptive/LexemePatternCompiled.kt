package org.lexem.angmar.compiler.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.descriptive.*

/**
 * Compiler for [LexemePatternNode].
 */
internal class LexemePatternCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: LexemePatternNode) : CompiledNode(parent, parentSignal, parserNode) {
    var type = LexemePatternNode.Companion.PatternType.Alternative
    var quantifier: CompiledNode? = null
    var unionName: CompiledNode? = null
    var patternContent: CompiledNode? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            LexemePatternAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: LexemePatternNode): CompiledNode {
            val result = LexemePatternCompiled(parent, parentSignal, node)
            result.type = node.type
            result.quantifier = node.quantifier?.compile(result, LexemePatternAnalyzer.signalEndQuantifier)
            result.unionName = node.unionName?.compile(result, LexemePatternAnalyzer.signalEndUnionName)
            result.patternContent = node.patternContent?.compile(result, LexemePatternAnalyzer.signalEndPatternContent)

            val pattern = result.patternContent
            when (result.type) {
                LexemePatternNode.Companion.PatternType.Static -> {
                    return pattern?.linkTo(parent, parentSignal, node) ?: NoOperationCompiled(parent, parentSignal,
                            node)
                }
                LexemePatternNode.Companion.PatternType.Optional -> {
                    if (pattern is NoOperationCompiled || pattern is InitBacktrackingCompiled) {
                        return pattern.linkTo(parent, parentSignal, node)
                    }
                }
                LexemePatternNode.Companion.PatternType.Negative -> {
                    if (pattern is NoOperationCompiled) {
                        return InitBacktrackingCompiled(parent, parentSignal, node)
                    } else if (pattern is InitBacktrackingCompiled) {
                        return NoOperationCompiled(parent, parentSignal, node)
                    }
                }
            }

            return result
        }
    }
}
