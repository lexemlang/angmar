package org.lexem.angmar.compiler.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.functional.statements.selective.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.functional.statements.selective.*

/**
 * Compiler for [SelectiveCaseStmtNode].
 */
internal class SelectiveCaseStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: SelectiveCaseStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    var patterns = mutableListOf<CompiledNode>()
    lateinit var block: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            SelectiveCaseStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: SelectiveCaseStmtNode): CompiledNode {
            val result = SelectiveCaseStmtCompiled(parent, parentSignal, node)

            for (pattern in node.patterns) {
                val compiledPattern =
                        pattern.compile(result, result.patterns.size + SelectiveCaseStmtAnalyzer.signalEndFirstPattern)

                if (compiledPattern is ConstantCompiled) {
                    if (compiledPattern.value == LxmLogic.True) {
                        // Not save any more patterns because they will never be executed.
                        result.patterns.add(compiledPattern)
                        break
                    }

                    // Not save the pattern when the condition is constantly false.
                } else {
                    result.patterns.add(compiledPattern)
                }
            }

            // All patterns to false.
            if (result.patterns.isEmpty()) {
                return ConstantCompiled(parent, parentSignal, node, LxmNil)
            }

            result.block = node.block.compile(result, SelectiveCaseStmtAnalyzer.signalEndBlock)

            return result
        }
    }
}
