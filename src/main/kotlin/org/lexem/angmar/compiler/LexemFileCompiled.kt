package org.lexem.angmar.compiler

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.functional.statements.controls.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.*

/**
 * Compiler for [LexemFileNode].
 */
internal class LexemFileCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: LexemFileNode) : CompiledNode(parent, parentSignal, parserNode) {
    val statements = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = LexemFileAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(node: LexemFileNode): LexemFileCompiled {
            val result = LexemFileCompiled(null, 0, node)

            for (statement in node.statements) {
                val compiledStatement =
                        statement.compile(result, result.statements.size + LexemFileAnalyzer.signalEndFirstStatement)

                // Only add not constant statements.
                if (compiledStatement !is NoOperationCompiled) {
                    result.statements.add(compiledStatement)
                }

                // Stop adding the rest statements after the control statements because they will never be executed.
                if (compiledStatement is ControlWithExpressionStmtCompiled || compiledStatement is ControlWithoutExpressionStmtCompiled) {
                    break
                }
            }

            return result
        }
    }
}
