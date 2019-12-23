package org.lexem.angmar.compiler.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Compiler for [AccessExplicitMemberNode].
 */
internal class AccessExplicitCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: AccessExplicitMemberNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var identifier: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AccessExplicitMemberAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: AccessExplicitMemberNode): AccessExplicitCompiled {
            val result = AccessExplicitCompiled(parent, parentSignal, node)
            result.identifier = node.identifier.compile(result, AccessExplicitMemberAnalyzer.signalEndIdentifier)

            return result
        }
    }
}
