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

    private constructor(memory: IMemory, oldVersion: LxmArguments) : super(memory, oldVersion = oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Adds the initial properties.
     */
    private fun init(memory: IMemory) {
        val positional = LxmList(memory)
        val named = LxmObject(memory)
        setProperty(memory, AnalyzerCommons.Identifiers.ArgumentsPositional, positional, isConstant = true)
        setProperty(memory, AnalyzerCommons.Identifiers.ArgumentsNamed, named, isConstant = true)
    }

    /**
     * Gets the positional list.
     */
    private fun getPositionalList(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.ArgumentsPositional, toWrite)!!

    /**
     * Gets the named object.
     */
    private fun getNamedObject(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmObject>(memory, AnalyzerCommons.Identifiers.ArgumentsNamed, toWrite)!!

    /**
     * Adds a named argument.
     */
    fun getNamedArgument(memory: IMemory, identifier: String) =
            getNamedObject(memory, toWrite = false).getPropertyValue(memory, identifier)

    /**
     * Adds a positional argument.
     */
    fun addPositionalArgument(memory: IMemory, value: LexemMemoryValue) {
        getPositionalList(memory, toWrite = true).addCell(memory, value)
    }

    /**
     * Adds a named argument.
     */
    fun addNamedArgument(memory: IMemory, identifier: String, value: LexemMemoryValue) {
        getNamedObject(memory, toWrite = true).setProperty(memory, identifier, value)
    }

    /**
     * Maps the [LxmArguments] to a [LxmBacktrackingData].
     */
    fun mapToBacktrackingData(memory: IMemory): LxmBacktrackingData {
        val positionalArguments = getPositionalList(memory, toWrite = false).getAllCells().toList()
        val namedArguments = getNamedObject(memory, toWrite = false).getAllProperties()

        val mappedArguments = hashMapOf<String, LexemPrimitive>()
        namedArguments.forEach { (key, property) ->
            val value = property.value.dereference(memory, toWrite = false) as? LexemPrimitive ?: LxmNil
            mappedArguments[key] = value
        }

        return LxmBacktrackingData(positionalArguments, mappedArguments)
    }

    /**
     * Maps all positional and named arguments into a map. Used for internal functions.
     */
    fun mapArguments(memory: IMemory, parameterNames: List<String>,
            spreadPositionalParameter: MutableList<LexemPrimitive>? = null,
            spreadNamedParameter: MutableMap<String, LexemPrimitive>? = null): Map<String, LexemPrimitive> {
        val result = hashMapOf<String, LexemPrimitive>()
        val positionalArguments = getPositionalList(memory, toWrite = false).getAllCells().toList()
        val namedArguments = getNamedObject(memory, toWrite = false).getAllProperties()

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
    fun mapArgumentsToContext(memory: IMemory, parameters: LxmParameters, context: LxmObject) {
        val parameterNames = parameters.getParameters()
        val result = mutableSetOf<String>()
        val positionalArguments = getPositionalList(memory, toWrite = false).getAllCells().toList()
        val namedArguments = getNamedObject(memory, toWrite = false).getAllProperties()

        // Map positional arguments.
        for ((index, parameterName) in parameterNames.withIndex()) {
            val value = positionalArguments.getOrNull(index)

            result.add(parameterName)

            if (value == null) {
                continue
            }

            context.setProperty(memory, parameterName, value)
        }

        // Add positional arguments to spread parameter.
        if (parameters.positionalSpread != null) {
            val list = LxmList(memory)

            for (i in positionalArguments.drop(parameterNames.size)) {
                list.addCell(memory, i)
            }

            context.setProperty(memory, parameters.positionalSpread!!, list)
        }

        // Map named arguments.
        var isThisAssigned = false
        if (parameters.namedSpread != null) {
            val obj = LxmObject(memory)

            for ((key, argument) in namedArguments) {
                val value = argument.value
                if (result.contains(key)) {
                    context.setProperty(memory, key, value)
                } else {
                    // Add named arguments to spread parameter.
                    if (key != AnalyzerCommons.Identifiers.This) {
                        obj.setProperty(memory, key, value)
                    }
                }

                // Always add the 'this' parameter.
                if (key == AnalyzerCommons.Identifiers.This) {
                    isThisAssigned = true
                    context.setProperty(memory, key, value)
                }
            }

            context.setProperty(memory, parameters.namedSpread!!, obj)
        } else {
            for ((key, argument) in namedArguments) {
                val value = argument.value
                if (result.contains(key)) {
                    context.setProperty(memory, key, value)
                }

                // Always add the 'this' parameter.
                if (key == AnalyzerCommons.Identifiers.This) {
                    isThisAssigned = true
                    context.setProperty(memory, key, value)
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

    override fun memoryClone(memory: IMemory) = LxmArguments(memory, this)

    override fun toString() = "[Arguments] ${super.toString()}"
}
