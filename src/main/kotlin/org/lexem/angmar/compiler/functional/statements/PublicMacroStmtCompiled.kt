package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [PublicMacroStmtNode].
 */
internal class PublicMacroStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: PublicMacroStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var element: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PublicMacroStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: PublicMacroStmtNode): CompiledNode {
            val result = PublicMacroStmtCompiled(parent, parentSignal, node)
            result.element = node.element.compile(result, PublicMacroStmtAnalyzer.signalEndElement)

            return result
        }
    }
}
