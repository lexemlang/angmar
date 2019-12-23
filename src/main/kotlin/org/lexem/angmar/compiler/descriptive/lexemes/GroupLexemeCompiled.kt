package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [GroupLexemeNode].
 */
internal class GroupLexemeCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: GroupLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isNegated = false
    var isFilterCode = false
    var header: CompiledNode? = null
    val patterns = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = GroupLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: GroupLexemeNode): CompiledNode {
            val result = GroupLexemeCompiled(parent, parentSignal, node)
            result.isNegated = node.isNegated
            result.isFilterCode = node.isFilterCode
            result.header = node.header?.compile(result, GroupLexemAnalyzer.signalEndHeader)

            for (pattern in node.patterns) {
                val compiledPattern =
                        pattern.compile(result, result.patterns.size + GroupLexemAnalyzer.signalEndFirstPattern)

                result.patterns.add(compiledPattern)
            }

            if (result.header == null) {
                if (result.patterns.all { it is NoOperationCompiled }) {
                    return if (result.isNegated) {
                        InitBacktrackingCompiled(parent, parentSignal, node)
                    } else {
                        NoOperationCompiled(parent, parentSignal, node)
                    }
                } else if (result.patterns.all { it is InitBacktrackingCompiled }) {
                    return if (result.isNegated) {
                        NoOperationCompiled(parent, parentSignal, node)
                    } else {
                        InitBacktrackingCompiled(parent, parentSignal, node)
                    }
                }
            }

            return result
        }
    }
}
