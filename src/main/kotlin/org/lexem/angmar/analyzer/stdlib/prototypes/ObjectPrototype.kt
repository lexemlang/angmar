package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in prototype of the Object object.
 */
internal object ObjectPrototype {
    // Methods
    const val Freeze = "freeze"
    const val IsFrozen = "isFrozen"
    const val IsPropertyFrozen = "isPropertyFrozen"
    const val FreezeProperties = "freezeProperties"
    const val FreezeAllProperties = "freezeAllProperties"
    const val RemoveProperties = "removeProperties"
    const val GetOwnPropertyKeys = "getOwnPropertyKeys"
    const val GetOwnPropertyValues = "getOwnPropertyValues"
    const val GetOwnProperties = "getOwnProperties"
    const val OwnPropertiesToMap = "ownPropertiesToMap"
    const val ContainsAnyOwnProperty = "containsAnyOwnProperty"
    const val ContainsAllOwnProperties = "containsAllOwnProperties"

    // Method arguments
    val IsPropertyFrozenArgs = listOf("property")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory, anyPrototype: LxmObject): LxmObject {
        val prototype = LxmObject(memory, anyPrototype)

        // Methods
        prototype.setProperty(memory, Freeze, LxmFunction(memory, ::freezeFunction), isConstant = true)
        prototype.setProperty(memory, IsFrozen, LxmFunction(memory, ::isFrozenFunction), isConstant = true)
        prototype.setProperty(memory, IsPropertyFrozen, LxmFunction(memory, ::isPropertyFrozenFunction),
                isConstant = true)
        prototype.setProperty(memory, FreezeProperties, LxmFunction(memory, ::freezePropertiesFunction),
                isConstant = true)
        prototype.setProperty(memory, FreezeAllProperties, LxmFunction(memory, ::freezeAllPropertiesFunction),
                isConstant = true)
        prototype.setProperty(memory, RemoveProperties, LxmFunction(memory, ::removePropertiesFunction),
                isConstant = true)
        prototype.setProperty(memory, GetOwnPropertyKeys, LxmFunction(memory, ::getOwnPropertyKeysFunction),
                isConstant = true)
        prototype.setProperty(memory, GetOwnPropertyValues, LxmFunction(memory, ::getOwnPropertyValuesFunction),
                isConstant = true)
        prototype.setProperty(memory, GetOwnProperties, LxmFunction(memory, ::getOwnPropertiesFunction),
                isConstant = true)
        prototype.setProperty(memory, OwnPropertiesToMap, LxmFunction(memory, ::ownPropertiesToMapFunction),
                isConstant = true)
        prototype.setProperty(memory, ContainsAnyOwnProperty, LxmFunction(memory, ::containsAnyOwnPropertyFunction),
                isConstant = true)
        prototype.setProperty(memory, ContainsAllOwnProperties, LxmFunction(memory, ::containsAllOwnPropertiesFunction),
                isConstant = true)

        return prototype
    }

    /**
     * Makes the object constant.
     */
    private fun freezeFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Freeze, ObjectType.TypeName,
                    toWrite = true) { _: LexemAnalyzer, thisValue: LxmObject ->
                thisValue.makeConstant(analyzer.memory)
                LxmNil
            }

    /**
     * Returns whether the object is constant or not.
     */
    private fun isFrozenFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, IsFrozen, ObjectType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmObject ->
                LxmLogic.from(thisValue.isConstant)
            }

    /**
     * Returns whether the object property is constant or not.
     */
    private fun isPropertyFrozenFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, IsPropertyFrozenArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val property = parserArguments[IsPropertyFrozenArgs[0]] ?: LxmNil

        if (thisValue !is LxmObject) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ObjectType.TypeName} value>${AccessExplicitMemberNode.accessToken}$IsPropertyFrozen' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ObjectType.TypeName}") {}
        }

        if (property !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ObjectType.TypeName} value>${AccessExplicitMemberNode.accessToken}$IsPropertyFrozen' method requires the parameter called '${IsPropertyFrozenArgs[0]}' be a ${StringType.TypeName}") {}
        }

        val descriptor = thisValue.getPropertyDescriptor(analyzer.memory, property.primitive)
        analyzer.memory.addToStackAsLast(LxmLogic.from(descriptor?.isConstant ?: false))
        return true
    }

    /**
     * Freezes the specified properties.
     */
    private fun freezePropertiesFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil

        if (thisValue !is LxmObject) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ObjectType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAllOwnProperties' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ObjectType.TypeName}") {}
        }

        for (key in spreadPositional) {
            if (key !is LxmString) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '<${ObjectType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAllOwnProperties' method requires all its parameters be a ${StringType.TypeName}") {}
            }

            thisValue.makePropertyConstant(analyzer.memory, key.primitive)
        }

        analyzer.memory.addToStackAsLast(LxmNil)
        return true
    }

    /**
     * Freezes all properties of the object.
     */
    private fun freezeAllPropertiesFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, FreezeAllProperties, ObjectType.TypeName,
                    toWrite = true) { _: LexemAnalyzer, thisValue: LxmObject ->
                val allProperties = thisValue.getAllIterableProperties()
                for ((key, _) in allProperties) {
                    thisValue.makePropertyConstant(analyzer.memory, key)
                }

                LxmNil
            }

    /**
     * Remove the specified keys of the object.
     */
    private fun removePropertiesFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil

        if (thisValue !is LxmObject) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ObjectType.TypeName} value>${AccessExplicitMemberNode.accessToken}$RemoveProperties' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ObjectType.TypeName}") {}
        }

        for (key in spreadPositional) {
            if (key !is LxmString) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '<${ObjectType.TypeName} value>${AccessExplicitMemberNode.accessToken}$RemoveProperties' method requires all its parameters be a ${StringType.TypeName}") {}
            }

            thisValue.removeProperty(analyzer.memory, key.primitive)
        }

        analyzer.memory.addToStackAsLast(LxmNil)
        return true
    }

    /**
     * Gets the property keys of this object.
     */
    private fun getOwnPropertyKeysFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, GetOwnPropertyKeys, ObjectType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmObject ->
                val allProperties = thisValue.getAllIterableProperties()
                val list = LxmList(analyzer.memory)

                for ((key, _) in allProperties) {
                    list.addCell(analyzer.memory, LxmString.from(key))
                }

                list
            }

    /**
     * Gets the property values of this object.
     */
    private fun getOwnPropertyValuesFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, GetOwnPropertyValues, ObjectType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmObject ->
                val allProperties = thisValue.getAllIterableProperties()
                val list = LxmList(analyzer.memory)

                for ((_, value) in allProperties) {
                    list.addCell(analyzer.memory, value.value)
                }

                list
            }

    /**
     * Gets the properties of this object.
     */
    private fun getOwnPropertiesFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, GetOwnProperties, ObjectType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmObject ->
                val allProperties = thisValue.getAllIterableProperties()
                val list = LxmList(analyzer.memory)

                for ((key, value) in allProperties) {
                    val obj = LxmObject(analyzer.memory)

                    obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Key, LxmString.from(key))
                    obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Value, value.value)

                    list.addCell(analyzer.memory, obj)
                }

                list
            }

    /**
     * Returns the properties of the object as a map.
     */
    private fun ownPropertiesToMapFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, IsFrozen, ObjectType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmObject ->
                val allProperties = thisValue.getAllIterableProperties()
                val map = LxmMap(analyzer.memory)

                for ((key, value) in allProperties) {
                    map.setProperty(analyzer.memory, LxmString.from(key), value.value)
                }

                map
            }

    /**
     * Checks whether any of the specified properties are contained in the object.
     */
    private fun containsAnyOwnPropertyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmObject) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ObjectType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAnyOwnProperty' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ObjectType.TypeName}") {}
        }

        for (key in spreadPositional) {
            if (key !is LxmString) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '<${ObjectType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAnyOwnProperty' method requires all its parameters be a ${StringType.TypeName}") {}
            }

            val prop = thisValue.getPropertyDescriptor(analyzer.memory, key.primitive)
            if (prop != null) {
                analyzer.memory.addToStackAsLast(LxmLogic.True)
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.False)
        return true
    }

    /**
     * Checks whether all the specified properties are contained in the object.
     */
    private fun containsAllOwnPropertiesFunction(analyzer: LexemAnalyzer, arguments: LxmArguments,
            function: LxmFunction, signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmObject) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ObjectType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAllOwnProperties' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ObjectType.TypeName}") {}
        }

        for (key in spreadPositional) {
            if (key !is LxmString) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '<${ObjectType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAllOwnProperties' method requires all its parameters be a ${StringType.TypeName}") {}
            }

            val prop = thisValue.getPropertyDescriptor(analyzer.memory, key.primitive)
            if (prop == null) {
                analyzer.memory.addToStackAsLast(LxmLogic.False)
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.True)
        return true
    }
}
