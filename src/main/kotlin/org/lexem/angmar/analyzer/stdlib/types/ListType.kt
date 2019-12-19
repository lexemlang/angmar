package org.lexem.angmar.analyzer.stdlib.types

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in List type object.
 */
internal object ListType {
    const val TypeName = "List"

    // Methods
    const val New = "new"
    const val NewFrom = "newFrom"
    const val Concat = "concat"

    // Method arguments
    private val NewArgs = listOf("size", "value")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the type.
     */
    fun initType(memory: LexemMemory, prototype: LxmObject) {
        val type = LxmObject(memory)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true).setProperty(memory, TypeName, type, isConstant = true)

        // Properties
        type.setProperty(memory, AnalyzerCommons.Identifiers.Prototype, prototype, isConstant = true)

        // Methods
        type.setProperty(memory, New, LxmFunction(memory, ::newFunction), isConstant = true)
        type.setProperty(memory, NewFrom, LxmFunction(memory, ::newFromFunction), isConstant = true)
        type.setProperty(memory, Concat, LxmFunction(memory, ::concatFunction), isConstant = true)
    }

    /**
     * Creates a new list with an initial size filled with the specified value.
     */
    private fun newFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments = arguments.mapArguments(analyzer.memory, NewArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val size = parsedArguments[NewArgs[0]]!!
                val value = parsedArguments[NewArgs[1]]!!

                if (size !is LxmInteger) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$TypeName${AccessExplicitMemberNode.accessToken}$New' method requires the parameter called '${NewArgs[0]}' be an ${IntegerType.TypeName}") {}
                }

                if (size.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$TypeName${AccessExplicitMemberNode.accessToken}$New' method requires the parameter called '${NewArgs[0]}' be a positive ${IntegerType.TypeName}. Actual: ${size.primitive}") {}
                }

                val newList = LxmList(analyzer.memory)
                for (i in 0 until size.primitive) {
                    newList.addCell(analyzer.memory, value)
                }

                analyzer.memory.addToStackAsLast(newList)
            }
        }

        return true
    }

    /**
     * Creates a new list with the specified values.
     */
    private fun newFromFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadArguments = mutableListOf<LexemPrimitive>()
        arguments.mapArguments(analyzer.memory, emptyList(), spreadPositionalParameter = spreadArguments)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val newList = LxmList(analyzer.memory)
                for (i in spreadArguments) {
                    newList.addCell(analyzer.memory, i)
                }

                analyzer.memory.addToStackAsLast(newList)
            }
        }

        return true
    }

    /**
     * Creates a new list with the values of all the specified lists.
     */
    private fun concatFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadArguments = mutableListOf<LexemPrimitive>()
        arguments.mapArguments(analyzer.memory, emptyList(), spreadPositionalParameter = spreadArguments)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val newList = LxmList(analyzer.memory)
                for (list in spreadArguments.map { it.dereference(analyzer.memory, toWrite = false) }) {
                    if (list !is LxmList) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                "The '$TypeName${AccessExplicitMemberNode.accessToken}$Concat' method requires that all its parameters be a $TypeName") {}
                    }

                    for (i in list.getAllCells()) {
                        newList.addCell(analyzer.memory, i)
                    }
                }

                analyzer.memory.addToStackAsLast(newList)
            }
        }

        return true
    }
}
