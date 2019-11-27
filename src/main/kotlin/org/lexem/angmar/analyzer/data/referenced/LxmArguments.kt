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

    constructor()
    constructor(oldArguments: LxmArguments) : super(oldArguments)

    constructor(memory: LexemMemory) {
        init(memory)
    }

    constructor(memory: LexemMemory, oldArguments: LxmArguments) : super(oldArguments) {
        init(memory)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Adds the initial properties.
     */
    private fun init(memory: LexemMemory) {
        val positional = LxmList()
        val named = LxmObject()
        val positionalReference = memory.add(positional)
        val namedReference = memory.add(named)
        setProperty(memory, AnalyzerCommons.Identifiers.ArgumentsPositional, positionalReference)
        setProperty(memory, AnalyzerCommons.Identifiers.ArgumentsNamed, namedReference)
    }

    /**
     * Gets the positional list.
     */
    private fun getPositionalList(memory: LexemMemory) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.ArgumentsPositional)!!

    /**
     * Gets the named object.
     */
    private fun getNamedObject(memory: LexemMemory) =
            getDereferencedProperty<LxmObject>(memory, AnalyzerCommons.Identifiers.ArgumentsNamed)!!

    /**
     * Adds a named argument.
     */
    fun getNamedArgument(memory: LexemMemory, identifier: String) =
            getNamedObject(memory).getPropertyValue(memory, identifier)

    /**
     * Adds a positional argument.
     */
    fun addPositionalArgument(memory: LexemMemory, value: LexemPrimitive) {
        getPositionalList(memory).addCell(memory, value)
    }

    /**
     * Adds a named argument.
     */
    fun addNamedArgument(memory: LexemMemory, identifier: String, value: LexemPrimitive) {
        getNamedObject(memory).setProperty(memory, identifier, value)
    }

    /**
     * Maps the [LxmArguments] to a [LxmBacktrackingData].
     */
    fun mapToBacktrackingData(memory: LexemMemory): LxmBacktrackingData {
        val positionalArguments = getPositionalList(memory).getAllCells()
        val namedArguments = getNamedObject(memory).getAllIterableProperties()

        val mappedArguments = namedArguments.mapValues { (_, property) ->
            val deref = property.value.dereference(memory)
            if (deref is LexemPrimitive) {
                deref
            } else {
                LxmNil
            }
        }

        return LxmBacktrackingData(positionalArguments, mappedArguments)
    }

    /**
     * Maps all positional and named arguments into a map. Used for internal functions.
     */
    fun mapArguments(memory: LexemMemory, parameterNames: List<String>,
            spreadPositionalParameter: MutableList<LexemPrimitive>? = null,
            spreadNamedParameter: MutableMap<String, LexemPrimitive>? = null): Map<String, LexemPrimitive> {
        val result = mutableMapOf<String, LexemPrimitive>()
        val positionalArguments = getPositionalList(memory).getAllCells()
        val namedArguments = getNamedObject(memory).getAllIterableProperties()

        // Map positional arguments.
        for (parameter in parameterNames.withIndex()) {
            val value = positionalArguments.getOrNull(parameter.index)
            if (value == null) {
                result[parameter.value] = LxmNil
                continue
            }

            result[parameter.value] = value
        }

        // Add positional arguments to spread parameter.
        spreadPositionalParameter?.addAll(positionalArguments.drop(parameterNames.size))

        // Map named arguments.
        for (argument in namedArguments) {
            val value = argument.value.value
            if (result.containsKey(argument.key)) {
                result[argument.key] = value
            } else {
                // Add named arguments to spread parameter.
                spreadNamedParameter?.put(argument.key, value)
            }
        }

        // Always add the 'this' parameter.
        val thisArgument = namedArguments[AnalyzerCommons.Identifiers.This] ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.FunctionCallWithoutThisArgument,
                "All function calls must include the '${AnalyzerCommons.Identifiers.This}' argument") {}
        result[AnalyzerCommons.Identifiers.This] = thisArgument.value

        return result
    }

    /**
     * Maps all positional and named arguments into a context.
     */
    fun mapArgumentsToContext(memory: LexemMemory, parameters: LxmParameters, context: LxmObject) {
        val parameterNames = parameters.getParameters()
        val result = mutableSetOf<String>()
        val positionalArguments = getPositionalList(memory).getAllCells()
        val namedArguments = getNamedObject(memory).getAllIterableProperties()

        // Map positional arguments.
        for (parameter in parameterNames.withIndex()) {
            val value = positionalArguments.getOrNull(parameter.index)

            result.add(parameter.value)

            if (value == null) {
                continue
            }

            context.setProperty(memory, parameter.value, value)
        }

        // Add positional arguments to spread parameter.
        if (parameters.positionalSpread != null) {
            val list = LxmList()
            val listRef = memory.add(list)

            for (i in positionalArguments.drop(parameterNames.size)) {
                list.addCell(memory, i)
            }

            context.setProperty(memory, parameters.positionalSpread!!, listRef)
        }

        // Map named arguments.
        if (parameters.namedSpread != null) {
            val obj = LxmObject()
            val objRef = memory.add(obj)

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
            }

            context.setProperty(memory, parameters.namedSpread!!, objRef)
        } else {
            for ((key, argument) in namedArguments) {
                val value = argument.value
                if (result.contains(key)) {
                    context.setProperty(memory, key, value)
                }
            }
        }

        // Always add the 'this' parameter.
        val thisArgument = namedArguments[AnalyzerCommons.Identifiers.This] ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.FunctionCallWithoutThisArgument,
                "All function calls must include the '${AnalyzerCommons.Identifiers.This}' argument") {}
        context.setProperty(memory, AnalyzerCommons.Identifiers.This, thisArgument.value)
    }

    override fun memoryDealloc(memory: LexemMemory) {
        val positional = getPropertyValue(memory, AnalyzerCommons.Identifiers.ArgumentsPositional) as LxmReference
        val named = getPropertyValue(memory, AnalyzerCommons.Identifiers.ArgumentsNamed) as LxmReference

        positional.decreaseReferences(memory)
        named.decreaseReferences(memory)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun clone() = LxmArguments(this)

    override fun toString() = "[Arguments] ${super.toString()}"
}
