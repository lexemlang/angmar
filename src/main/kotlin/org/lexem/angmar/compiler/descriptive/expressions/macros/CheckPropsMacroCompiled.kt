package org.lexem.angmar.compiler.descriptive.expressions.macros

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.expressions.macros.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.expressions.macros.*

/**
 * Compiler for [CheckPropsMacroNode].
 */
internal class CheckPropsMacroCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: CheckPropsMacroNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var properties: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            CheckPropsMacroAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: CheckPropsMacroNode): CheckPropsMacroCompiled {
            val result = CheckPropsMacroCompiled(parent, parentSignal, node)
            result.properties = node.properties.compile(result, CheckPropsMacroAnalyzer.signalEndProperties)

            return result
        }
    }
}
