package org.lexem.angmar.analyzer.stdlib.types

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in String type object.
 */
internal object StringType {
    const val TypeName = "String"

    // Methods
    const val Join = "join"
    const val JoinBy = "joinBy"
    const val FromUnicodePoints = "fromUnicodePoints"

    // Method arguments
    private val JoinByArgs = listOf("separator")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the type.
     */
    fun initType(memory: LexemMemory, prototype: LxmObject) {
        val type = LxmObject(memory)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true).setProperty(TypeName, type, isConstant = true)

        // Properties
        type.setProperty(AnalyzerCommons.Identifiers.Prototype, prototype, isConstant = true)

        // Methods
        type.setProperty(Join, LxmFunction(memory, ::joinFunction), isConstant = true)
        type.setProperty(JoinBy, LxmFunction(memory, ::joinByFunction), isConstant = true)
        type.setProperty(FromUnicodePoints, LxmFunction(memory, ::fromUnicodePointsFunction), isConstant = true)
    }

    /**
     * Joins all the specified values into a String.
     */
    private fun joinFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstParam = AnalyzerNodesCommons.signalCallFunction + 1
        val spreadArguments = mutableListOf<LexemPrimitive>()
        arguments.mapArguments(emptyList(), spreadPositionalParameter = spreadArguments)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (spreadArguments.isEmpty()) {
                    analyzer.memory.addToStackAsLast(LxmString.Empty)

                    return true
                }

                // Adds the base string.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmString.Empty)

                // Calls toString.
                StdlibCommons.callToString(analyzer, spreadArguments.first(), InternalFunctionCallCompiled,
                        signalEndFirstParam, Join)

                return false
            }
            in signalEndFirstParam..signalEndFirstParam + spreadArguments.size -> {
                val position = (signal - signalEndFirstParam) + 1

                // Combine the result.
                val accumulator = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as LxmString
                val currentString = analyzer.memory.getLastFromStack() as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.ToStringMethodNotReturningString,
                        "The ${AnalyzerCommons.Identifiers.ToString} method must always return a $TypeName") {}
                val result = LxmString.from(accumulator.primitive + currentString.primitive)

                if (position < spreadArguments.size) {
                    analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Accumulator, result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    // Calls toString.
                    StdlibCommons.callToString(analyzer, spreadArguments[position], InternalFunctionCallCompiled,
                            position + signalEndFirstParam, Join)

                    return false
                }

                // Remove Accumulator from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

                analyzer.memory.replaceLastStackCell(result)

                return true
            }
            else -> throw AngmarUnreachableException()
        }
    }

    /**
     * Joins all the specified values into a String separated by the specified separator.
     */
    private fun joinByFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstParam = AnalyzerNodesCommons.signalCallFunction + 1
        val spreadArguments = mutableListOf<LexemPrimitive>()
        val parsedArguments = arguments.mapArguments(JoinByArgs, spreadPositionalParameter = spreadArguments)
        val separator = parsedArguments[JoinByArgs[0]]!!

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (separator !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$TypeName${AccessExplicitMemberNode.accessToken}$JoinBy' method requires the parameter called '${JoinByArgs[0]}' be a $TypeName") {}
                }

                if (spreadArguments.isEmpty()) {
                    analyzer.memory.addToStackAsLast(LxmString.Empty)

                    return true
                }

                // Adds the base string.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmString.Empty)

                // Calls toString.
                StdlibCommons.callToString(analyzer, spreadArguments.first(), InternalFunctionCallCompiled,
                        signalEndFirstParam, JoinBy)

                return false
            }
            in signalEndFirstParam..signalEndFirstParam + spreadArguments.size -> {
                val position = (signal - signalEndFirstParam) + 1
                separator as LxmString

                // Combine the result.
                val accumulator = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as LxmString
                val currentString = analyzer.memory.getLastFromStack() as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.ToStringMethodNotReturningString,
                        "The ${AnalyzerCommons.Identifiers.ToString} method must always return a $TypeName") {}
                val result = LxmString.from(if (position == 1) {
                    currentString.primitive
                } else {
                    accumulator.primitive + separator.primitive + currentString.primitive
                })

                if (position < spreadArguments.size) {
                    analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Accumulator, result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    // Calls toString.
                    StdlibCommons.callToString(analyzer, spreadArguments[position], InternalFunctionCallCompiled,
                            position + signalEndFirstParam, JoinBy)

                    return false
                }

                // Remove Accumulator from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

                analyzer.memory.replaceLastStackCell(result)

                return true
            }
            else -> throw AngmarUnreachableException()
        }
    }

    /**
     * Creates a new string from a list of Unicode points.
     */
    @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
    private fun fromUnicodePointsFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadArguments = mutableListOf<LexemPrimitive>()
        arguments.mapArguments(emptyList(), spreadPositionalParameter = spreadArguments)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val builder = StringBuilder()

                for (i in spreadArguments) {
                    if (i !is LxmInteger) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                "The '$TypeName${AccessExplicitMemberNode.accessToken}$FromUnicodePoints' method requires all its parameters be ${IntegerType.TypeName}s") {}
                    }

                    builder.append(Character.toString(i.primitive))
                }

                analyzer.memory.addToStackAsLast(LxmString.from(builder.toString()))
            }
        }
        return true
    }
}
