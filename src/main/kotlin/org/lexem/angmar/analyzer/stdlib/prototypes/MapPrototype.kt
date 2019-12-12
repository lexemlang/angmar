package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in prototype of the Map object.
 */
internal object MapPrototype {
    // Methods
    const val Size = "size"
    const val Freeze = "freeze"
    const val IsFrozen = "isFrozen"
    const val Every = "every"
    const val Filter = "filter"
    const val ForEach = "forEach"
    const val ContainsAnyKey = "containsAnyKey"
    const val ContainsAllKeys = "containsAllKeys"
    const val Map = "map"
    const val Reduce = "reduce"
    const val Any = "any"
    const val Put = "put"
    const val Remove = "remove"
    const val ToList = "toList"
    const val ToObject = "toObject"
    const val Keys = "keys"
    const val Values = "values"
    const val Clear = "clear"

    // Method arguments

    val EveryArgs = listOf("fn")
    val FilterArgs = EveryArgs
    val ForEachArgs = EveryArgs
    val MapArgs = EveryArgs
    val ReduceArgs = listOf("default", "fn")
    val AnyArgs = EveryArgs
    val PutArgs = listOf("key", "value")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()

        // Methods
        prototype.setProperty(memory, Size, memory.add(LxmFunction(::sizeFunction)), isConstant = true)
        prototype.setProperty(memory, Freeze, memory.add(LxmFunction(::freezeFunction)), isConstant = true)
        prototype.setProperty(memory, IsFrozen, memory.add(LxmFunction(::isFrozenFunction)), isConstant = true)
        prototype.setProperty(memory, Every, memory.add(LxmFunction(::everyFunction)), isConstant = true)
        prototype.setProperty(memory, Filter, memory.add(LxmFunction(::filterFunction)), isConstant = true)
        prototype.setProperty(memory, ForEach, memory.add(LxmFunction(::forEachFunction)), isConstant = true)
        prototype.setProperty(memory, ContainsAnyKey, memory.add(LxmFunction(::containsAnyKeyFunction)),
                isConstant = true)
        prototype.setProperty(memory, ContainsAllKeys, memory.add(LxmFunction(::containsAllKeysFunction)),
                isConstant = true)
        prototype.setProperty(memory, Map, memory.add(LxmFunction(::mapFunction)), isConstant = true)
        prototype.setProperty(memory, Reduce, memory.add(LxmFunction(::reduceFunction)), isConstant = true)
        prototype.setProperty(memory, Any, memory.add(LxmFunction(::anyFunction)), isConstant = true)
        prototype.setProperty(memory, Put, memory.add(LxmFunction(::putFunction)), isConstant = true)
        prototype.setProperty(memory, Remove, memory.add(LxmFunction(::removeFunction)), isConstant = true)
        prototype.setProperty(memory, ToList, memory.add(LxmFunction(::toListFunction)), isConstant = true)
        prototype.setProperty(memory, ToObject, memory.add(LxmFunction(::toObjectFunction)), isConstant = true)
        prototype.setProperty(memory, Keys, memory.add(LxmFunction(::keysFunction)), isConstant = true)
        prototype.setProperty(memory, Values, memory.add(LxmFunction(::valuesFunction)), isConstant = true)
        prototype.setProperty(memory, Clear, memory.add(LxmFunction(::clearFunction)), isConstant = true)

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, memory.add(LxmFunction(::add)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Sub, memory.add(LxmFunction(::sub)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalAnd, memory.add(LxmFunction(::logicalAnd)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalOr, memory.add(LxmFunction(::logicalOr)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalXor, memory.add(LxmFunction(::logicalXor)),
                isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Returns the size of the map.
     */
    private fun sizeFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    Size, MapType.TypeName) { _: LexemAnalyzer, thisValue: LxmMap ->
                LxmInteger.from(thisValue.getSize())
            }

    /**
     * Makes the map constant.
     */
    private fun freezeFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    Freeze, MapType.TypeName) { _: LexemAnalyzer, thisValue: LxmMap ->
                thisValue.makeConstant(analyzer.memory)
                LxmNil
            }

    /**
     * Returns whether the map is constant or not.
     */
    private fun isFrozenFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    IsFrozen, MapType.TypeName) { _: LexemAnalyzer, thisValue: LxmMap ->
                LxmLogic.from(thisValue.isImmutable)
            }

    /**
     * Checks whether all the elements of the map match the condition.
     */
    private fun everyFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        EveryArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[EveryArgs[0]] ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Every' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Every' method requires the parameter called '${EveryArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val (list, listReference) = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, listReference)

                if (list.actualListSize > 0) {
                    val cell = list.getCell(analyzer.memory, 0)!!.dereference(analyzer.memory) as LxmObject
                    val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                    val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                    StdlibCommons.callMethod(analyzer, function, listOf(value, key), signalEndFirstElement, Every)

                    return false
                }

                // Remove List from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                analyzer.memory.addToStackAsLast(LxmLogic.True)
            }
            else -> {
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(
                        analyzer.memory) as LxmList

                if (signal in signalEndFirstElement until signalEndFirstElement + list.actualListSize) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()
                    val resultTruthy = RelationalFunctions.isTruthy(result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (resultTruthy) {
                        if (position < list.actualListSize) {
                            val cell =
                                    list.getCell(analyzer.memory, position)!!.dereference(analyzer.memory) as LxmObject
                            val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                            val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                            StdlibCommons.callMethod(analyzer, function, listOf(value, key),
                                    signalEndFirstElement + position, Every)

                            return false
                        }

                        analyzer.memory.addToStackAsLast(LxmLogic.True)
                    } else {
                        analyzer.memory.addToStackAsLast(LxmLogic.False)
                    }

                    // Remove List from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)
                }
            }
        }

        return true
    }

    /**
     * Filters a map.
     */
    private fun filterFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        FilterArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[FilterArgs[0]] ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Filter' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Filter' method requires the parameter called '${FilterArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val (list, listReference) = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, listReference)

                // Generate the map.
                val map = LxmMap(null)
                val mapReference = analyzer.memory.add(map)

                if (list.actualListSize > 0) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, mapReference)

                    val cell = list.getCell(analyzer.memory, 0)!!.dereference(analyzer.memory) as LxmObject
                    val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                    val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                    StdlibCommons.callMethod(analyzer, function, listOf(value, key), signalEndFirstElement, Filter)

                    return false
                }

                // Remove List from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                analyzer.memory.addToStackAsLast(mapReference)
            }
            else -> {
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(
                        analyzer.memory) as LxmList
                val map = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory) as LxmMap

                if (signal in signalEndFirstElement until signalEndFirstElement + list.actualListSize) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()
                    val resultTruthy = RelationalFunctions.isTruthy(result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (resultTruthy) {
                        val cell =
                                list.getCell(analyzer.memory, position - 1)!!.dereference(analyzer.memory) as LxmObject
                        val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                        val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                        map.setProperty(analyzer.memory, key, value)
                    }

                    if (position < list.actualListSize) {
                        val cell = list.getCell(analyzer.memory, position)!!.dereference(analyzer.memory) as LxmObject
                        val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                        val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                        StdlibCommons.callMethod(analyzer, function, listOf(value, key),
                                signalEndFirstElement + position, Filter)

                        return false
                    }

                    // Remove List from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                    // Move Accumulator to Last in the stack.
                    analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
                }
            }
        }

        return true
    }

    /**
     * Iterates of the elements of a map.
     */
    private fun forEachFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        ForEachArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[ForEachArgs[0]] ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ForEach' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ForEach' method requires the parameter called '${ForEachArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val (list, listReference) = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, listReference)

                if (list.actualListSize > 0) {
                    val cell = list.getCell(analyzer.memory, 0)!!.dereference(analyzer.memory) as LxmObject
                    val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                    val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                    StdlibCommons.callMethod(analyzer, function, listOf(value, key), signalEndFirstElement, ForEach)

                    return false
                }

                // Remove List from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            else -> {
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(
                        analyzer.memory) as LxmList

                if (signal in signalEndFirstElement until signalEndFirstElement + list.actualListSize) {
                    val position = (signal - signalEndFirstElement) + 1

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (position < list.actualListSize) {
                        val cell = list.getCell(analyzer.memory, position)!!.dereference(analyzer.memory) as LxmObject
                        val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                        val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                        StdlibCommons.callMethod(analyzer, function, listOf(value, key),
                                signalEndFirstElement + position, ForEach)

                        return false
                    }

                    analyzer.memory.addToStackAsLast(LxmNil)

                    // Remove List from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)
                }
            }
        }

        return true
    }

    /**
     * Checks whether any of the specified keys are contained in the map.
     */
    private fun containsAnyKeyFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAnyKey' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val allProps = thisValue.getAllProperties().flatMap { it.value }
        for (key in spreadPositional) {
            val inside = allProps.any { RelationalFunctions.identityEquals(it.key, key) }
            if (inside) {
                analyzer.memory.addToStackAsLast(LxmLogic.True)
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.False)
        return true
    }

    /**
     * Checks whether all the specified keys are contained in the map.
     */
    private fun containsAllKeysFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference,
            function: LxmFunction, signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAllKeys' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val allProps = thisValue.getAllProperties().flatMap { it.value }
        for (key in spreadPositional) {
            val inside = allProps.any { RelationalFunctions.identityEquals(it.key, key) }
            if (!inside) {
                analyzer.memory.addToStackAsLast(LxmLogic.False)
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.True)
        return true
    }

    /**
     * Maps the elements of a map into another one.
     */
    private fun mapFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory, MapArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[MapArgs[0]] ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Map' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Map' method requires the parameter called '${MapArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val (list, listReference) = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, listReference)

                // Generate the map.
                val map = LxmMap(null)
                val mapReference = analyzer.memory.add(map)

                if (list.actualListSize > 0) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, mapReference)

                    val cell = list.getCell(analyzer.memory, 0)!!.dereference(analyzer.memory) as LxmObject
                    val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                    val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                    StdlibCommons.callMethod(analyzer, function, listOf(value, key), signalEndFirstElement, Map)

                    return false
                }

                // Remove List from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                analyzer.memory.addToStackAsLast(mapReference)
            }
            else -> {
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(
                        analyzer.memory) as LxmList
                val map = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory) as LxmMap

                if (signal in signalEndFirstElement until signalEndFirstElement + list.actualListSize) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()

                    // Add the value.
                    val cell = list.getCell(analyzer.memory, position - 1)!!.dereference(analyzer.memory) as LxmObject
                    val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                    map.setProperty(analyzer.memory, key, result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (position < list.actualListSize) {
                        val cell = list.getCell(analyzer.memory, position)!!.dereference(analyzer.memory) as LxmObject
                        val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                        val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                        StdlibCommons.callMethod(analyzer, function, listOf(value, key),
                                signalEndFirstElement + position, Map)

                        return false
                    }

                    // Remove List from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                    // Move Accumulator to Last in the stack.
                    analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
                }
            }
        }

        return true
    }

    /**
     * Reduces a map into a value.
     */
    private fun reduceFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        ReduceArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val default = parserArguments[ReduceArgs[0]] ?: LxmNil
        val function = parserArguments[ReduceArgs[1]] ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Reduce' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Reduce' method requires the parameter called '${ReduceArgs[1]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val (list, listReference) = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, listReference)

                if (list.actualListSize > 0) {
                    val cell = list.getCell(analyzer.memory, 0)!!.dereference(analyzer.memory) as LxmObject
                    val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                    val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                    StdlibCommons.callMethod(analyzer, function, listOf(default, value, key), signalEndFirstElement,
                            Reduce)

                    return false
                }

                // Remove List from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                analyzer.memory.addToStackAsLast(default)
            }
            else -> {
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(
                        analyzer.memory) as LxmList

                if (signal in signalEndFirstElement until signalEndFirstElement + list.actualListSize) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()

                    if (position < list.actualListSize) {
                        val cell = list.getCell(analyzer.memory, position)!!.dereference(analyzer.memory) as LxmObject
                        val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                        val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                        StdlibCommons.callMethod(analyzer, function, listOf(result, value, key),
                                signalEndFirstElement + position, Reduce)

                        // Remove Last from the stack.
                        analyzer.memory.removeLastFromStack()

                        return false
                    }

                    // Remove List from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)
                }
            }
        }

        return true
    }

    /**
     * Checks whether any of the elements of the map match the condition.
     */
    private fun anyFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory, AnyArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[AnyArgs[0]] ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Any' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Any' method requires the parameter called '${AnyArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val (list, listReference) = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, listReference)

                if (list.actualListSize > 0) {
                    val cell = list.getCell(analyzer.memory, 0)!!.dereference(analyzer.memory) as LxmObject
                    val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                    val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                    StdlibCommons.callMethod(analyzer, function, listOf(value, key), signalEndFirstElement, Any)

                    return false
                }

                // Remove List from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                analyzer.memory.addToStackAsLast(LxmLogic.False)
            }
            else -> {
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(
                        analyzer.memory) as LxmList

                if (signal in signalEndFirstElement until signalEndFirstElement + list.actualListSize) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()
                    val resultTruthy = RelationalFunctions.isTruthy(result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (!resultTruthy) {
                        if (position < list.actualListSize) {
                            val cell =
                                    list.getCell(analyzer.memory, position)!!.dereference(analyzer.memory) as LxmObject
                            val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                            val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                            StdlibCommons.callMethod(analyzer, function, listOf(value, key),
                                    signalEndFirstElement + position, Any)

                            return false
                        }

                        analyzer.memory.addToStackAsLast(LxmLogic.False)
                    } else {
                        analyzer.memory.addToStackAsLast(LxmLogic.True)
                    }

                    // Remove List from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)
                }
            }
        }

        return true
    }

    /**
     * Adds a new element to the map.
     */
    private fun putFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory, PutArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val key = parserArguments[PutArgs[0]] ?: LxmNil
        val value = parserArguments[PutArgs[1]] ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Put' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        thisValue.setProperty(analyzer.memory, key, value)

        analyzer.memory.addToStackAsLast(LxmNil)
        return true
    }

    /**
     * Remove the specified keys of the map.
     */
    private fun removeFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Remove' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        for (key in spreadPositional) {
            thisValue.removeProperty(analyzer.memory, key)
        }

        analyzer.memory.addToStackAsLast(LxmNil)
        return true
    }

    /**
     * Gets a list of key-value objects.
     */
    private fun toListFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToList' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val (_, listReference) = toList(analyzer, thisValue)

        analyzer.memory.addToStackAsLast(listReference)

        return true
    }

    /**
     * Gets an object with all the map properties whose keys are strings.
     */
    private fun toObjectFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToObject' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val obj = LxmObject()
        val objReference = analyzer.memory.add(obj)
        for ((i, propList) in thisValue.getAllProperties()) {
            for (prop in propList) {
                val key = prop.key
                if (key is LxmString) {
                    obj.setProperty(analyzer.memory, key.primitive, prop.value)
                }
            }
        }

        analyzer.memory.addToStackAsLast(objReference)

        return true
    }

    /**
     * Gets a list with all the keys.
     */
    private fun keysFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Keys' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val list = LxmList()
        val listReference = analyzer.memory.add(list)
        for ((i, propList) in thisValue.getAllProperties()) {
            for (prop in propList) {
                list.addCell(analyzer.memory, prop.key)
            }
        }

        analyzer.memory.addToStackAsLast(listReference)

        return true
    }

    /**
     * Gets a list with all the values.
     */
    private fun valuesFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Keys' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val list = LxmList()
        val listReference = analyzer.memory.add(list)
        for ((i, propList) in thisValue.getAllProperties()) {
            for (prop in propList) {
                list.addCell(analyzer.memory, prop.value)
            }
        }

        analyzer.memory.addToStackAsLast(listReference)

        return true
    }

    /**
     * Removes all the elements of the map.
     */
    private fun clearFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Clear' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        for ((i, list) in thisValue.getAllProperties()) {
            for (prop in list) {
                thisValue.removeProperty(analyzer.memory, prop.key)
            }
        }

        analyzer.memory.addToStackAsLast(LxmNil)

        return true
    }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs the addition of two values.
     */
    private fun add(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Add, MapType.TypeName,
                    listOf(MapType.TypeName)) { _: LexemAnalyzer, left: LxmMap, right: LexemMemoryValue ->
                when (right) {
                    is LxmMap -> {
                        val newMap = LxmMap(null)
                        val newMapReference = analyzer.memory.add(newMap)

                        for ((_, list) in left.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(analyzer.memory, prop.key, prop.value)
                            }
                        }

                        for ((_, list) in right.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(analyzer.memory, prop.key, prop.value)
                            }
                        }

                        newMapReference
                    }
                    else -> null
                }
            }

    /**
     * Performs the subtraction of two values.
     */
    private fun sub(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Sub, MapType.TypeName,
                    listOf(MapType.TypeName)) { _: LexemAnalyzer, left: LxmMap, right: LexemMemoryValue ->
                when (right) {
                    is LxmMap -> {
                        val newMap = LxmMap(null)
                        val newMapReference = analyzer.memory.add(newMap)

                        for ((_, list) in left.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(analyzer.memory, prop.key, prop.value)
                            }
                        }

                        for ((_, list) in right.getAllProperties()) {
                            for (prop in list) {
                                newMap.removeProperty(analyzer.memory, prop.key)
                            }
                        }

                        newMapReference
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical AND between two logical values.
     */
    private fun logicalAnd(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalAnd, MapType.TypeName,
                    listOf(MapType.TypeName)) { _: LexemAnalyzer, left: LxmMap, right: LexemMemoryValue ->
                when (right) {
                    is LxmMap -> {
                        val newMap = LxmMap(null)
                        val newMapReference = analyzer.memory.add(newMap)

                        for ((_, list) in left.getAllProperties()) {
                            for (prop in list) {
                                val value = right.getPropertyValue(analyzer.memory, prop.key)

                                if (value != null) {
                                    newMap.setProperty(analyzer.memory, prop.key, value)
                                }
                            }
                        }

                        newMapReference
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical OR between two logical values.
     */
    private fun logicalOr(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalOr, MapType.TypeName,
                    listOf(MapType.TypeName)) { _: LexemAnalyzer, left: LxmMap, right: LexemMemoryValue ->
                when (right) {
                    is LxmMap -> {
                        val newMap = LxmMap(null)
                        val newMapReference = analyzer.memory.add(newMap)

                        for ((_, list) in left.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(analyzer.memory, prop.key, prop.value)
                            }
                        }

                        for ((_, list) in right.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(analyzer.memory, prop.key, prop.value)
                            }
                        }

                        newMapReference
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical XOR between two logical values.
     */
    private fun logicalXor(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalXor, LogicType.TypeName,
                    listOf(LogicType.TypeName)) { _: LexemAnalyzer, left: LxmMap, right: LexemMemoryValue ->
                when (right) {
                    is LxmMap -> {
                        val newMap = LxmMap(null)
                        val newMapReference = analyzer.memory.add(newMap)

                        for ((_, list) in left.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(analyzer.memory, prop.key, prop.value)
                            }
                        }

                        for ((_, list) in right.getAllProperties()) {
                            for (prop in list) {
                                val value = newMap.getPropertyValue(analyzer.memory, prop.key)

                                if (value != null) {
                                    newMap.removeProperty(analyzer.memory, prop.key)
                                } else {
                                    newMap.setProperty(analyzer.memory, prop.key, prop.value)
                                }
                            }
                        }

                        newMapReference
                    }
                    else -> null
                }
            }

    // AUXILIARY METHODS ------------------------------------------------------

    private fun toList(analyzer: LexemAnalyzer, map: LxmMap): Pair<LxmList, LxmReference> {
        val list = LxmList()
        val listReference = analyzer.memory.add(list)
        for ((i, propList) in map.getAllProperties()) {
            for (prop in propList) {
                val obj = LxmObject()
                val objReference = analyzer.memory.add(obj)

                obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Key, prop.key)
                obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Value, prop.value)

                list.addCell(analyzer.memory, objReference)
            }
        }

        return Pair(list, listReference)
    }
}
