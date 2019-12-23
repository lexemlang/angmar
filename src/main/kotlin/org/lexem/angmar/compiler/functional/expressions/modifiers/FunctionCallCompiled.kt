package org.lexem.angmar.compiler.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Compiler for [FunctionCallNode].
 */
internal class FunctionCallCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: FunctionCallNode) : CompiledNode(parent, parentSignal, parserNode) {
    val positionalArguments = mutableListOf<CompiledNode>()
    val namedArguments = mutableListOf<CompiledNode>()
    val spreadArguments = mutableListOf<CompiledNode>()
    var propertiesExpression: CompiledNode? = null


    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionCallAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: FunctionCallNode): FunctionCallCompiled {
            val result = FunctionCallCompiled(parent, parentSignal, node)
            result.propertiesExpression =
                    node.propertiesExpression?.compile(result, FunctionCallAnalyzer.signalEndPropertiesExpression)

            for (argument in node.positionalArguments) {
                val compiledArgument = argument.compile(result,
                        result.positionalArguments.size + FunctionCallAnalyzer.signalEndFirstArgument)
                result.positionalArguments.add(compiledArgument)
            }

            for (argument in node.namedArguments) {
                val compiledArgument = argument.compile(result,
                        result.positionalArguments.size + result.namedArguments.size + FunctionCallAnalyzer.signalEndFirstArgument)
                result.namedArguments.add(compiledArgument)
            }

            for (argument in node.spreadArguments) {
                val compiledArgument = argument.compile(result,
                        result.positionalArguments.size + result.namedArguments.size + result.spreadArguments.size + FunctionCallAnalyzer.signalEndFirstArgument)
                result.spreadArguments.add(compiledArgument)
            }

            return result
        }
    }
}
