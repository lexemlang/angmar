package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [ObjectSimplificationNode].
 */
internal class ObjectSimplificationCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: ObjectSimplificationNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var identifier: CompiledNode
    var parameterList: FunctionParameterListCompiled? = null
    var block: CompiledNode? = null
    var isConstant = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ObjectSimplificationAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: ObjectSimplificationNode): ObjectSimplificationCompiled {
            val result = ObjectSimplificationCompiled(parent, parentSignal, node)
            result.identifier = node.identifier.compile(result, ObjectSimplificationAnalyzer.signalEndIdentifier)
            result.parameterList =
                    node.parameterList?.compile(result, ObjectSimplificationAnalyzer.signalEndParameterList)
            result.block = node.block?.compile(result, ObjectSimplificationAnalyzer.signalEndBlock)
            result.isConstant = node.isConstant

            return result
        }
    }
}
