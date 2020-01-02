package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in prototype of the Any object.
 */
internal object AnyPrototype {
    // Methods
    const val Is = "is"
    const val IsAny = "isAny"

    // Method arguments
    private val IsArgs = listOf("type")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmAnyPrototype {
        val prototype = LxmAnyPrototype(memory)

        // Methods
        prototype.setProperty(memory, Is, LxmFunction(memory, ::isFunction), isConstant = true)
        prototype.setProperty(memory, IsAny, LxmFunction(memory, ::isAnyFunction), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Identifiers.ToString, LxmFunction(memory, ::toStringFunction),
                isConstant = true)

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalNot, LxmFunction(memory, ::logicalNot),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, LxmFunction(memory, ::add), isConstant = true)

        return prototype
    }

    /**
     * Checks whether the This element is of the specified type.
     */
    private fun isFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments = arguments.mapArguments(analyzer.memory, IsArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val thisValue = parsedArguments[AnalyzerCommons.Identifiers.This]!!.dereference(analyzer.memory,
                        toWrite = false)
                val type = parsedArguments[IsArgs[0]]!!

                val thisValueType = thisValue.getType(analyzer.memory)

                analyzer.memory.addToStackAsLast(LxmLogic.from(RelationalFunctions.identityEquals(thisValueType, type)))
            }
        }

        return true
    }

    /**
     * Checks whether the This element is of any of the specified types.
     */
    private fun isAnyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadArguments = mutableListOf<LexemPrimitive>()
        val parsedArguments =
                arguments.mapArguments(analyzer.memory, emptyList(), spreadPositionalParameter = spreadArguments)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val thisValue = parsedArguments[AnalyzerCommons.Identifiers.This]!!.dereference(analyzer.memory,
                        toWrite = false)

                val thisValueType = thisValue.getType(analyzer.memory)

                for (type in spreadArguments) {
                    if (RelationalFunctions.identityEquals(thisValueType, type)) {
                        analyzer.memory.addToStackAsLast(LxmLogic.True)
                        return true
                    }
                }

                analyzer.memory.addToStackAsLast(LxmLogic.False)
            }
        }

        return true
    }

    /**
     * Returns the textual representation of the 'this' value.
     */
    private fun toStringFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments = arguments.mapArguments(analyzer.memory, emptyList())

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val thisValue = parsedArguments[AnalyzerCommons.Identifiers.This]!!.dereference(analyzer.memory,
                        toWrite = false)

                analyzer.memory.addToStackAsLast(thisValue.toLexemString(analyzer.memory))
            }
        }

        return true
    }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs a logical NOT of the 'this' value.
     */
    private fun logicalNot(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil

        val result = if (RelationalFunctions.isTruthy(thisValue)) {
            LxmLogic.False
        } else {
            LxmLogic.True
        }

        analyzer.memory.addToStackAsLast(result)
        return true
    }

    /**
     * Performs the addition of this value and a String.
     */
    private fun add(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, AnalyzerCommons.Operators.ParameterList)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val left =
                        parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                                ?: LxmNil
                val right = parserArguments[AnalyzerCommons.Operators.RightParameterName] ?: LxmNil

                if (right !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${AnyType.TypeName} value>${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Operators.Add}' method requires the parameter called '${AnalyzerCommons.Operators.RightParameterName}' be a ${StringType.TypeName}") {}
                }

                // Calls toString.
                StdlibCommons.callToString(analyzer, left, InternalFunctionCallCompiled,
                        AnalyzerNodesCommons.signalStart + 1, AnalyzerCommons.Operators.Add)

                return false
            }
            else -> {
                val right = parserArguments[AnalyzerCommons.Operators.RightParameterName] as LxmString
                val left = analyzer.memory.getLastFromStack() as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.ToStringMethodNotReturningString,
                        "The ${AnalyzerCommons.Identifiers.ToString} method must always return a ${StringType.TypeName}") {}

                val result = LxmString.from(left.primitive + right.primitive)
                analyzer.memory.replaceLastStackCell(result)

                return true
            }
        }
    }
}
