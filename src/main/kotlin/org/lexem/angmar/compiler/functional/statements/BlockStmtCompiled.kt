package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.controls.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [BlockStmtNode].
 */
internal class BlockStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: BlockStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    val statements = mutableListOf<CompiledNode>()
    var tag: CompiledNode? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = BlockStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: BlockStmtNode): CompiledNode {
            val result = BlockStmtCompiled(parent, parentSignal, node)
            result.tag = node.tag?.compile(result, BlockStmtAnalyzer.signalEndTag)

            for (statement in node.statements) {
                val compiledStatement =
                        statement.compile(result, result.statements.size + BlockStmtAnalyzer.signalEndFirstStatement)

                // Only add not constant statements.
                if (compiledStatement !is NoOperationCompiled) {
                    result.statements.add(compiledStatement)
                }

                // Stop adding the rest statements after the control statements because they will never be executed.
                if (compiledStatement is ControlWithExpressionStmtCompiled || compiledStatement is ControlWithoutExpressionStmtCompiled || compiledStatement is InitBacktrackingCompiled) {
                    break
                }
            }

            // No statements nor tag -> remove the statement.
            if (result.statements.isEmpty() && (result.tag == null || result.tag is ConstantCompiled)) {
                return NoOperationCompiled(parent, parentSignal, node)
            }

            // Init backtracking statement.
            if (result.statements.size == 1) {
                val statement = result.statements.first()
                if (statement is InitBacktrackingCompiled) {
                    return statement.linkTo(parent, parentSignal, node)
                }
            }

            return result
        }
    }
}
