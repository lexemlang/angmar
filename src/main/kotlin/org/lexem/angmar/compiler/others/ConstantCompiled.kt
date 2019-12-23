package org.lexem.angmar.compiler.others

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.nodes.others.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.*

/**
 * Compiled node that holds a [LexemPrimitive].
 */
internal class ConstantCompiled(parent: CompiledNode?, parentSignal: Int, parserNode: ParserNode,
        val value: LexemPrimitive) : CompiledNode(parent, parentSignal, parserNode) {

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = ConstantAnalyzer.stateMachine(analyzer, signal, this)
}
