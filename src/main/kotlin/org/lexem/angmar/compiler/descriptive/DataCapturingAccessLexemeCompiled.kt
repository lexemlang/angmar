package org.lexem.angmar.compiler.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.descriptive.*

/**
 * Compiler for [DataCapturingAccessLexemeNode].
 */
internal class DataCapturingAccessLexemeCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: DataCapturingAccessLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var element: CompiledNode
    var modifiers = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            DataCapturingAccessLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: DataCapturingAccessLexemeNode): CompiledNode {
            val result = DataCapturingAccessLexemeCompiled(parent, parentSignal, node)
            result.element = node.element.compile(result, DataCapturingAccessLexemeAnalyzer.signalEndElement)

            for (modifier in node.modifiers) {
                val compiledModifier = modifier.compile(result,
                        result.modifiers.size + DataCapturingAccessLexemeAnalyzer.signalEndFirstModifier)

                result.modifiers.add(compiledModifier)
            }

            return result
        }
    }
}
