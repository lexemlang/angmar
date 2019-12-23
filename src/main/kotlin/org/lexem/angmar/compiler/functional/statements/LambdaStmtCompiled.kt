package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [LambdaStmtNode].
 */
internal class LambdaStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: LambdaStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var expression: CompiledNode
    var isDescriptiveCode = false
    var isFilterCode = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = LambdaStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: LambdaStmtNode): LambdaStmtCompiled {
            val result = LambdaStmtCompiled(parent, parentSignal, node)
            result.expression = node.expression.compile(result, LambdaStmtAnalyzer.signalEndExpression)
            result.isDescriptiveCode = node.isFilterCode || node.expression is LexemePatternContentNode
            result.isFilterCode = node.isFilterCode

            return result
        }
    }
}
