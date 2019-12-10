package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
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
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()

        // Methods
        prototype.setProperty(memory, Wrap, memory.add(LxmFunction(::wrapFunction)), isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Returns a new [LxmFunction] that when invoked it will call the current [LxmFunction] with the specified arguments.
     */
    private fun wrapFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val arguments = argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!
        val parsedArguments = arguments.mapArguments(analyzer.memory, emptyList())

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val thisValueRef = parsedArguments[AnalyzerCommons.Identifiers.This]!!
                val thisValue = thisValueRef.dereference(analyzer.memory)

                if (thisValue !is LxmFunction) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                            "The '<${FunctionType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Wrap' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${FunctionType.TypeName}") {}
                }

                val fn = LxmFunction(analyzer.memory, argumentsReference, ::wrapFunctionAux)
                val fnRef = analyzer.memory.add(fn)

                // Add the function to be called.
                arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.HiddenFunction, thisValueRef)

                analyzer.memory.addToStackAsLast(fnRef)
            }
        }

        return true
    }

    fun wrapFunctionAux(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEnd = AnalyzerNodesCommons.signalStart + 1
        val arguments = function.contextReference!!.dereferenceAs<LxmArguments>(analyzer.memory)!!
        val parsedArguments = arguments.mapArguments(analyzer.memory, WrapAuxArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val fnValueRef = parsedArguments[AnalyzerCommons.Identifiers.HiddenFunction]!!

                AnalyzerNodesCommons.callFunction(analyzer, fnValueRef, function.contextReference,
                        InternalFunctionCallNode,
                        LxmCodePoint(InternalFunctionCallNode, signalEnd, callerNode = function.node,
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
