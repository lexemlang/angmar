package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * The Lexem value for the arguments of a function.
 */
internal class LxmArguments : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory) {
        init(memory)
    }

    private constructor(bigNode: BigNode, oldVersion: LxmArguments) : super(bigNode, oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Adds the initial properties.
     */
    private fun init(memory: LexemMemory) {
        val positional = LxmList(memory)
        val named = LxmObject(memory)
        setProperty(AnalyzerCommons.Identifiers.ArgumentsPositional, positional, isConstant = true)
        setProperty(AnalyzerCommons.Identifiers.ArgumentsNamed, named, isConstant = true)
    }

    /**
     * Gets the positional list.
     */
    private fun getPositionalList(toWrite: Boolean) =
            getDereferencedProperty<LxmList>(AnalyzerCommons.Identifiers.ArgumentsPositional, toWrite)!!

    /**
     * Gets the named object.
     */
    private fun getNamedObject(toWrite: Boolean) =
            getDereferencedProperty<LxmObject>(AnalyzerCommons.Identifiers.ArgumentsNamed, toWrite)!!

    /**
     * Adds a named argument.
     */
    fun getNamedArgument(identifier: String) = getNamedObject(toWrite = false).getPropertyValue(identifier)

    /**
     * Adds a positional argument.
     */
    fun addPositionalArgument(value: LexemMemoryValue) {
        getPositionalList(toWrite = true).addCell(value)
    }

    /**
     * Adds a named argument.
     */
    fun addNamedArgument(identifier: String, value: LexemMemoryValue) {
        getNamedObject(toWrite = true).setProperty(identifier, value)
    }

    /**
     * Maps the [LxmArguments] to a [LxmBacktrackingData].
     */
    fun mapToBacktrackingData(): LxmBacktrackingData {
        val positionalArguments = getPositionalList(toWrite = false).getAllCells().toList()
        val namedArguments = getNamedObject(toWrite = false).getAllIterableProperties()

        val mappedArguments = hashMapOf<String, LexemPrimitive>()
        namedArguments.forEach { (key, property) ->
            val value = property.value.dereference(bigNode, toWrite = false) as? LexemPrimitive ?: LxmNil
            mappedArguments[key] = value
        }

        return LxmBacktrackingData(positionalArguments, mappedArguments)
    }

    /**
     * Maps all positional and named arguments into a map. Used for internal functions.
     */
    fun mapArguments(parameterNames: List<String>, spreadPositionalParameter: MutableList<LexemPrimitive>? = null,
            spreadNamedParameter: MutableMap<String, LexemPrimitive>? = null): Map<String, LexemPrimitive> {
        val result = hashMapOf<String, LexemPrimitive>()
        val positionalArguments = getPositionalList(toWrite = false).getAllCells().toList()
        val namedArguments = getNamedObject(toWrite = false).getAllIterableProperties()

        // Map positional arguments.
        for ((index, parameterName) in parameterNames.withIndex()) {
            val value = positionalArguments.getOrNull(index)
            if (value == null) {
                result[parameterName] = LxmNil
                continue
            }

            result[parameterName] = value
        }

        // Add positional arguments to spread parameter.
        spreadPositionalParameter?.addAll(positionalArguments.drop(parameterNames.size))

        // Map named arguments.
        var isThisAssigned = false
        for ((key, argument) in namedArguments) {
            val value = argument.value
            if (result.containsKey(key)) {
                result[key] = value
            } else {
                // Add named arguments to spread parameter.
                spreadNamedParameter?.put(key, value)
            }

            // Always add the 'this' parameter.
            if (key == AnalyzerCommons.Identifiers.This) {
                isThisAssigned = true
                result[key] = value
            }
        }

        // Ensure the 'this' parameter is always added.
        if (!isThisAssigned) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FunctionCallWithoutThisArgument,
                    "All function calls must include the '${AnalyzerCommons.Identifiers.This}' argument") {}
        }

        return result
    }

    /**
     * Maps all positional and named arguments into a context.
     */
    fun mapArgumentsToContext(memory: LexemMemory, parameters: LxmParameters, context: LxmObject) {
        val parameterNames = parameters.getParameters()
        val result = mutableSetOf<String>()
        val positionalArguments = getPositionalList(toWrite = false).getAllCells().toList()
        val namedArguments = getNamedObject(toWrite = false).getAllIterableProperties()

        // Map positional arguments.
        for ((index, parameterName) in parameterNames.withIndex()) {
            val value = positionalArguments.getOrNull(index)

            result.add(parameterName)

            if (value == null) {
                continue
            }

            context.setProperty(parameterName, value)
        }

        // Add positional arguments to spread parameter.
        if (parameters.positionalSpread != null) {
            val list = LxmList(memory)

            for (i in positionalArguments.drop(parameterNames.size)) {
                list.addCell(i)
            }

            context.setProperty(parameters.positionalSpread!!, list)
        }

        // Map named arguments.
        var isThisAssigned = false
        if (parameters.namedSpread != null) {
            val obj = LxmObject(memory)

            for ((key, argument) in namedArguments) {
                val value = argument.value
                if (result.contains(key)) {
                    context.setProperty(key, value)
                } else {
                    // Add named arguments to spread parameter.
                    if (key != AnalyzerCommons.Identifiers.This) {
                        obj.setProperty(key, value)
                    }
                }

                // Always add the 'this' parameter.
                if (key == AnalyzerCommons.Identifiers.This) {
                    isThisAssigned = true
                    context.setProperty(key, value)
                }
            }

            context.setProperty(parameters.namedSpread!!, obj)
        } else {
            for ((key, argument) in namedArguments) {
                val value = argument.value
                if (result.contains(key)) {
                    context.setProperty(key, value)
                }

                // Always add the 'this' parameter.
                if (key == AnalyzerCommons.Identifiers.This) {
                    isThisAssigned = true
                    context.setProperty(key, value)
                }
            }
        }

        // Ensure the 'this' parameter is always added.
        if (!isThisAssigned) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FunctionCallWithoutThisArgument,
                    "All function calls must include the '${AnalyzerCommons.Identifiers.This}' argument") {}
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(bigNode: BigNode) = LxmArguments(bigNode, this)

    override fun toString() = "[Arguments] ${super.toString()}"
}
