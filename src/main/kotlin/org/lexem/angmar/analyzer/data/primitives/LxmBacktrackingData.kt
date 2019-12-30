package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * The Lexem value for backtracking data.
 */
internal class LxmBacktrackingData(val positional: List<LexemPrimitive>, val named: Map<String, LexemPrimitive>) :
        LexemPrimitive {

    // METHODS ----------------------------------------------------------------

    /**
     * Maps all positional and named arguments into a context.
     */
    fun mapBacktrackingDataToContext(memory: LexemMemory, parameters: LxmParameters, context: LxmObject) {
        val result = mutableSetOf<String>()

        // Map positional arguments.
        for ((index, key) in parameters.getParameters().withIndex()) {
            val value = positional.getOrNull(index)

            result.add(key)

            if (value == null) {
                continue
            }

            context.setProperty(memory, key, value)
        }

        // Add positional arguments to spread parameter.
        if (parameters.positionalSpread != null) {
            val list = LxmList(memory)

            for (i in positional.drop(parameters.getParameters().size)) {
                list.addCell(memory, i)
            }

            context.setProperty(memory, parameters.positionalSpread!!, list)
        }

        // Map named arguments.
        if (parameters.namedSpread != null) {
            val obj = LxmObject(memory)

            for ((key, value) in named) {
                if (result.contains(key)) {
                    context.setProperty(memory, key, value)
                } else {
                    // Add named arguments to spread parameter.
                    if (key != AnalyzerCommons.Identifiers.This) {
                        obj.setProperty(memory, key, value)
                    }
                }
            }

            context.setProperty(memory, parameters.namedSpread!!, obj)
        } else {
            for ((key, value) in named) {
                if (result.contains(key)) {
                    context.setProperty(memory, key, value)
                }
            }
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode() = throw AngmarUnreachableException()

    override fun toString() = "[BacktrackingData] (positional: $positional, named: $named)"
}
