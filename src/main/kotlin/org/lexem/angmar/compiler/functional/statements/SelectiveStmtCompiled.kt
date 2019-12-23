package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [SelectiveStmtNode].
 */
internal class SelectiveStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: SelectiveStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    var condition: CompiledNode? = null
    var tag: CompiledNode? = null
    val cases = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            SelectiveStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: SelectiveStmtNode): CompiledNode {
            val result = SelectiveStmtCompiled(parent, parentSignal, node)
            result.condition = node.condition?.compile(result, SelectiveStmtAnalyzer.signalEndCondition)
            result.tag = node.tag?.compile(result, SelectiveStmtAnalyzer.signalEndTag)

            for (case in node.cases) {
                val compiledCase = case.compile(result, result.cases.size + SelectiveStmtAnalyzer.signalEndFirstCase)

                // Save only if it is not constant.
                if (compiledCase !is ConstantCompiled) {
                    result.cases.add(compiledCase)
                }
            }

            // No cases nor condition nor tag -> remove the statement.
            if (result.cases.isEmpty() && (result.tag == null || result.tag is ConstantCompiled) && (result.condition == null || result.condition is ConstantCompiled)) {
                return NoOperationCompiled(parent, parentSignal, node)
            }

            return result
        }
    }
}
