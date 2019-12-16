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
 * Built-in Map type object.
 */
internal object MapType {
    const val TypeName = "Map"

    // Methods
    const val Assign = "assign"

    // Method arguments
    private val AssignArgs = listOf("target")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the type.
     */
    fun initType(memory: LexemMemory, prototype: LxmReference) {
        val type = LxmObject(memory)
        val reference = memory.add(type)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true)
                .setProperty(memory, TypeName, reference, isConstant = true)

        // Properties
        type.setProperty(memory, AnalyzerCommons.Identifiers.Prototype, prototype, isConstant = true)

        // Methods
        type.setProperty(memory, Assign, memory.add(LxmFunction(memory, ::assignFunction)), isConstant = true)
    }


    /**
     * Assigns all the sources to the target map.
     */
    private fun assignFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadArguments = mutableListOf<LexemPrimitive>()
        val parsedArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, AssignArgs, spreadPositionalParameter = spreadArguments)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val targetRef = parsedArguments[AssignArgs[0]]!!
                val target = targetRef.dereference(analyzer.memory, toWrite = true)

                if (target !is LxmMap) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$TypeName${AccessExplicitMemberNode.accessToken}$Assign' method requires the parameter called '${AssignArgs[0]}' be a $TypeName") {}
                }

                for (source in spreadArguments.map { it.dereference(analyzer.memory, toWrite = false) }) {
                    if (source !is LxmMap) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                "The '$TypeName${AccessExplicitMemberNode.accessToken}$Assign' method requires that all its source parameters be a $TypeName") {}
                    }

                    for ((_, propList) in source.getAllProperties()) {
                        for (prop in propList) {
                            target.setProperty(analyzer.memory, prop.key, prop.value)
                        }
                    }
                }

                analyzer.memory.addToStackAsLast(targetRef)
            }
        }

        return true
    }
}
