package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in prototype of the Function object.
 */
internal object FunctionPrototype {
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
     * Returns a new [LxmFunction] that when invoked it will call the current [LxmFunction] with the specified arguments.
     */
    private fun wrapFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments = arguments.mapArguments(analyzer.memory, emptyList())

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val thisValue = parsedArguments[AnalyzerCommons.Identifiers.This]!!.dereference(analyzer.memory,
                        toWrite = false)

                if (thisValue !is LxmFunction) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                            "The '<${FunctionType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Wrap' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${FunctionType.TypeName}") {}
                }

                val fn = LxmFunction(analyzer.memory, arguments, ::wrapFunctionAux)

                // Add the function to be called.
                arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.HiddenFunction, thisValue)

                analyzer.memory.addToStackAsLast(fn)
            }
        }

        return true
    }

    fun wrapFunctionAux(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int): Boolean {
        val signalEnd = AnalyzerNodesCommons.signalStart + 1
        val wrappedArguments =
                function.contextReference!!.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!
        val parsedArguments = wrappedArguments.mapArguments(analyzer.memory, WrapAuxArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val fnValue = parsedArguments[AnalyzerCommons.Identifiers.HiddenFunction]!!.dereference(analyzer.memory,
                        toWrite = false) as LxmFunction

                AnalyzerNodesCommons.callFunction(analyzer, fnValue, wrappedArguments,
                        LxmCodePoint(InternalFunctionCallCompiled, signalEnd, callerNode = function.node,
                                callerContextName = "<${FunctionType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Wrap"))

                return false
            }
            signalEnd -> {
                // Skip
            }
        }

        return true
    }
}
