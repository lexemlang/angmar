package org.lexem.angmar.compiler.descriptive.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.statements.*

/**
 * Compiler for [SetPropsMacroStmtNode].
 */
internal class SetPropsMacroStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: SetPropsMacroStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var properties: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            SetPropsMacroStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: SetPropsMacroStmtNode): SetPropsMacroStmtCompiled {
            val result = SetPropsMacroStmtCompiled(parent, parentSignal, node)
            result.properties = node.properties.compile(result, SetPropsMacroStmtAnalyzer.signalEndProperties)

            return result
        }
    }
}
