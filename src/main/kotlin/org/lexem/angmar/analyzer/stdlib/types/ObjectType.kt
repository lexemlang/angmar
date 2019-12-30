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
 * Built-in Object type object.
 */
internal object ObjectType {
    const val TypeName = "Object"

    // Methods
    const val Assign = "assign"
    const val NewFrom = "newFrom"

    // Method arguments
    private val NewFromArgs = listOf("prototype")
    private val AssignArgs = listOf("target")

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
        type.setProperty(Assign, LxmFunction(memory, ::assignFunction), isConstant = true)
    }

    /**
     * Creates a new object with a specified prototype.
     */
    private fun newFromFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments = arguments.mapArguments(AssignArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val prototypeRef = parsedArguments[AssignArgs[0]]!!
                val prototype = prototypeRef.dereference(analyzer.memory, toWrite = false)

                if (prototype !is LxmObject) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$TypeName${AccessExplicitMemberNode.accessToken}$NewFrom' method requires the parameter called '${NewFromArgs[0]}' be an $TypeName") {}
                }

                val newObject = LxmObject(analyzer.memory, prototype)

                analyzer.memory.addToStackAsLast(newObject)
            }
        }

        return true
    }

    /**
     * Assigns all the sources to the target object.
     */
    private fun assignFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadArguments = mutableListOf<LexemPrimitive>()
        val parsedArguments = arguments.mapArguments(AssignArgs, spreadPositionalParameter = spreadArguments)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val targetRef = parsedArguments[AssignArgs[0]]!!
                val target = targetRef.dereference(analyzer.memory, toWrite = true)

                if (target !is LxmObject) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$TypeName${AccessExplicitMemberNode.accessToken}$Assign' method requires the parameter called '${AssignArgs[0]}' be an $TypeName") {}
                }

                for (source in spreadArguments.map { it.dereference(analyzer.memory, toWrite = false) }) {
                    if (source !is LxmObject) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                "The '$TypeName${AccessExplicitMemberNode.accessToken}$Assign' method requires that all its source parameters be an $TypeName") {}
                    }

                    for ((key, prop) in source.getAllIterableProperties()) {
                        target.setProperty(key, prop.value)
                    }
                }

                analyzer.memory.addToStackAsLast(targetRef)
            }
        }

        return true
    }
}
