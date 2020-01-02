package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in prototype of the Expression object.
 */
internal object ExpressionPrototype {
    // Methods
    const val Wrap = "wrap"

    // Method arguments
    private val WrapAuxArgs = listOf(AnalyzerCommons.Identifiers.HiddenFunction)

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmObject {
        val prototype = LxmObject(memory)

        // Methods
        prototype.setProperty(memory, Wrap, LxmFunction(memory, ::wrapFunction), isConstant = true)

        return prototype
    }

    /**
     * Returns a new [LxmFunction] that when invoked it will call the current [LxmExpression] with the specified arguments.
     */
    private fun wrapFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments = arguments.mapArguments(analyzer.memory, emptyList())

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val thisValue = parsedArguments[AnalyzerCommons.Identifiers.This]!!.dereference(analyzer.memory,
                        toWrite = false)

                if (thisValue !is LxmExpression) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                            "The '<${ExpressionType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Wrap' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ExpressionType.TypeName}") {}
                }

                val fn = LxmFunction(analyzer.memory, arguments, FunctionPrototype::wrapFunctionAux)

                // Add the function to be called.
                arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.HiddenFunction, thisValue)

                analyzer.memory.addToStackAsLast(fn)
            }
        }

        return true
    }
}
