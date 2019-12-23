package org.lexem.angmar.compiler.functional.expressions.macros

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.macros.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.functional.expressions.macros.*

/**
 * Compiler for [MacroBacktrackNode].
 */
internal class MacroBacktrackCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: MacroBacktrackNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var arguments: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            MacroBacktrackAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: MacroBacktrackNode): CompiledNode {
            val result = MacroBacktrackCompiled(parent, parentSignal, node)
            result.arguments = node.arguments?.compile(result, MacroBacktrackAnalyzer.signalEndArguments)
                    ?: return InitBacktrackingCompiled(parent, parentSignal, node)

            return result
        }
    }
}
