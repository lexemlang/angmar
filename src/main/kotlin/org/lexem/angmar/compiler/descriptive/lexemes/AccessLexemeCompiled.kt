package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [AccessLexemeNode].
 */
internal class AccessLexemeCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: AccessLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isNegated = false
    var nextAccess: CompiledNode? = null
    lateinit var expression: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AccessLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: AccessLexemeNode): AccessLexemeCompiled {
            val result = AccessLexemeCompiled(parent, parentSignal, node)
            result.isNegated = node.isNegated
            result.nextAccess = node.nextAccess?.compile(result, AccessLexemAnalyzer.signalEndNextAccess)
            result.expression = node.expression.compile(result, AccessLexemAnalyzer.signalEndExpression)

            return result
        }
    }
}
