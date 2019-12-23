package org.lexem.angmar.compiler.others

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.ParserNode

/**
 * Special node for calling internal functions. It cannot be parsed.
 */
internal object InternalFunctionCallCompiled : CompiledNode(null, 0, ParserNode.Companion.EmptyParserNode) {
    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            InternalFunctionCallAnalyzer.stateMachine(analyzer, signal)
}
