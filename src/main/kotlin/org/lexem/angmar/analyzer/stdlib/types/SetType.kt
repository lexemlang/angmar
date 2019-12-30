package org.lexem.angmar.analyzer.stdlib.types

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in Set type object.
 */
internal object SetType {
    const val TypeName = "Set"

    // Methods
    const val NewFrom = "newFrom"
    const val Join = "join"

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
        type.setProperty(NewFrom, LxmFunction(memory, ::newFromFunction), isConstant = true)
        type.setProperty(Join, LxmFunction(memory, ::joinFunction), isConstant = true)
    }

    /**
     * Creates a new set with the specified values.
     */
    private fun newFromFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadArguments = mutableListOf<LexemPrimitive>()
        arguments.mapArguments(emptyList(), spreadPositionalParameter = spreadArguments)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val newSet = LxmSet(analyzer.memory)
                for (i in spreadArguments) {
                    newSet.addValue(analyzer.memory, i)
                }

                analyzer.memory.addToStackAsLast(newSet)
            }
        }

        return true
    }

    /**
     * Creates a new set with the values of all the specified sets.
     */
    private fun joinFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadArguments = mutableListOf<LexemPrimitive>()
        arguments.mapArguments(emptyList(), spreadPositionalParameter = spreadArguments)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val newSet = LxmSet(analyzer.memory)
                for (set in spreadArguments.map { it.dereference(analyzer.memory, toWrite = false) }) {
                    if (set !is LxmSet) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                "The '$TypeName${AccessExplicitMemberNode.accessToken}$Join' method requires that all its parameters be a $TypeName") {}
                    }

                    for ((i, propList) in set.getAllValues()) {
                        for (prop in propList) {
                            newSet.addValue(analyzer.memory, prop.value)
                        }
                    }
                }

                analyzer.memory.addToStackAsLast(newSet)
            }
        }

        return true
    }
}
