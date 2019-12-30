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
    fun initPrototype(memory: LexemMemory): LxmObject {
        val prototype = LxmObject(memory)

        // Methods
        prototype.setProperty(Size, LxmFunction(memory, ::sizeFunction), isConstant = true)
        prototype.setProperty(Freeze, LxmFunction(memory, ::freezeFunction), isConstant = true)
        prototype.setProperty(IsFrozen, LxmFunction(memory, ::isFrozenFunction), isConstant = true)
        prototype.setProperty(Every, LxmFunction(memory, ::everyFunction), isConstant = true)
        prototype.setProperty(Filter, LxmFunction(memory, ::filterFunction), isConstant = true)
        prototype.setProperty(ForEach, LxmFunction(memory, ::forEachFunction), isConstant = true)
        prototype.setProperty(ContainsAnyKey, LxmFunction(memory, ::containsAnyKeyFunction), isConstant = true)
        prototype.setProperty(ContainsAllKeys, LxmFunction(memory, ::containsAllKeysFunction), isConstant = true)
        prototype.setProperty(Map, LxmFunction(memory, ::mapFunction), isConstant = true)
        prototype.setProperty(Reduce, LxmFunction(memory, ::reduceFunction), isConstant = true)
        prototype.setProperty(Any, LxmFunction(memory, ::anyFunction), isConstant = true)
        prototype.setProperty(Put, LxmFunction(memory, ::putFunction), isConstant = true)
        prototype.setProperty(Remove, LxmFunction(memory, ::removeFunction), isConstant = true)
        prototype.setProperty(ToList, LxmFunction(memory, ::toListFunction), isConstant = true)
        prototype.setProperty(ToObject, LxmFunction(memory, ::toObjectFunction), isConstant = true)
        prototype.setProperty(Keys, LxmFunction(memory, ::keysFunction), isConstant = true)
        prototype.setProperty(Values, LxmFunction(memory, ::valuesFunction), isConstant = true)
        prototype.setProperty(Clear, LxmFunction(memory, ::clearFunction), isConstant = true)

        // Operators
        prototype.setProperty(AnalyzerCommons.Operators.Add, LxmFunction(memory, ::add), isConstant = true)
        prototype.setProperty(AnalyzerCommons.Operators.Sub, LxmFunction(memory, ::sub), isConstant = true)
        prototype.setProperty(AnalyzerCommons.Operators.LogicalAnd, LxmFunction(memory, ::logicalAnd),
                isConstant = true)
        prototype.setProperty(AnalyzerCommons.Operators.LogicalOr, LxmFunction(memory, ::logicalOr), isConstant = true)
        prototype.setProperty(AnalyzerCommons.Operators.LogicalXor, LxmFunction(memory, ::logicalXor),
                isConstant = true)

        return prototype
    }

    /**
     * Returns the size of the map.
     */
    private fun sizeFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Size, MapType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmMap ->
                LxmInteger.from(thisValue.getSize())
            }

    /**
     * Makes the map constant.
     */
    private fun freezeFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Freeze, MapType.TypeName,
                    toWrite = true) { _: LexemAnalyzer, thisValue: LxmMap ->
                thisValue.makeConstant(analyzer.memory)
                LxmNil
            }

    /**
     * Returns whether the map is constant or not.
     */
    private fun isFrozenFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, IsFrozen, MapType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmMap ->
                LxmLogic.from(thisValue.isConstant)
            }

    /**
     * Checks whether all the elements of the map match the condition.
     */
    private fun everyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(EveryArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[EveryArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Every' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Every' method requires the parameter called '${EveryArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                if (list.size > 0) {
                    val cell = list.getCell(0)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
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
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(analyzer.memory,
                        toWrite = false) as LxmList

                if (signal in signalEndFirstElement..signalEndFirstElement + list.size) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()
                    val resultTruthy = RelationalFunctions.isTruthy(result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (resultTruthy) {
                        if (position < list.size) {
                            val cell =
                                    list.getCell(position)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
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
    private fun filterFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(FilterArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[FilterArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Filter' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Filter' method requires the parameter called '${FilterArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                // Generate the map.
                val map = LxmMap(analyzer.memory)

                if (list.size > 0) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, map)

                    val cell = list.getCell(0)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
                    val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                    val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                    StdlibCommons.callMethod(analyzer, function, listOf(value, key), signalEndFirstElement, Filter)

                    return false
                }

                // Remove List from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                analyzer.memory.addToStackAsLast(map)
            }
            else -> {
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(analyzer.memory,
                        toWrite = false) as LxmList
                val map = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory, toWrite = true) as LxmMap

                if (signal in signalEndFirstElement..signalEndFirstElement + list.size) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()
                    val resultTruthy = RelationalFunctions.isTruthy(result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (resultTruthy) {
                        val cell =
                                list.getCell(position - 1)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
                        val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                        val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                        map.setProperty(key, value)
                    }

                    if (position < list.size) {
                        val cell = list.getCell(position)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
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
    private fun forEachFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(ForEachArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[ForEachArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ForEach' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ForEach' method requires the parameter called '${ForEachArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                if (list.size > 0) {
                    val cell = list.getCell(0)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
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
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(analyzer.memory,
                        toWrite = false) as LxmList

                if (signal in signalEndFirstElement..signalEndFirstElement + list.size) {
                    val position = (signal - signalEndFirstElement) + 1

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (position < list.size) {
                        val cell = list.getCell(position)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
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
    private fun containsAnyKeyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

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
    private fun containsAllKeysFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

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
    private fun mapFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(MapArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[MapArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Map' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Map' method requires the parameter called '${MapArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                // Generate the map.
                val map = LxmMap(analyzer.memory)

                if (list.size > 0) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, map)

                    val cell = list.getCell(0)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
                    val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                    val value = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)!!
                    StdlibCommons.callMethod(analyzer, function, listOf(value, key), signalEndFirstElement, Map)

                    return false
                }

                // Remove List from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                analyzer.memory.addToStackAsLast(map)
            }
            else -> {
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(analyzer.memory,
                        toWrite = false) as LxmList
                val map = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory, toWrite = true) as LxmMap

                if (signal in signalEndFirstElement..signalEndFirstElement + list.size) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()

                    // Add the value.
                    val cell = list.getCell(position - 1)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
                    val key = cell.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)!!
                    map.setProperty(key, result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (position < list.size) {
                        val cell = list.getCell(position)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
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
    private fun reduceFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(ReduceArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val default = parserArguments[ReduceArgs[0]] ?: LxmNil
        val function = parserArguments[ReduceArgs[1]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Reduce' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Reduce' method requires the parameter called '${ReduceArgs[1]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                if (list.size > 0) {
                    val cell = list.getCell(0)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
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
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(analyzer.memory,
                        toWrite = false) as LxmList

                if (signal in signalEndFirstElement..signalEndFirstElement + list.size) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()

                    if (position < list.size) {
                        val cell = list.getCell(position)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
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
    private fun anyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(AnyArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[AnyArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Any' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Any' method requires the parameter called '${AnyArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                if (list.size > 0) {
                    val cell = list.getCell(0)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
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
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(analyzer.memory,
                        toWrite = false) as LxmList

                if (signal in signalEndFirstElement..signalEndFirstElement + list.size) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()
                    val resultTruthy = RelationalFunctions.isTruthy(result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (!resultTruthy) {
                        if (position < list.size) {
                            val cell =
                                    list.getCell(position)!!.dereference(analyzer.memory, toWrite = false) as LxmObject
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
    private fun putFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(PutArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil
        val key = parserArguments[PutArgs[0]] ?: LxmNil
        val value = parserArguments[PutArgs[1]] ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Put' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        thisValue.setProperty(key, value)

        analyzer.memory.addToStackAsLast(LxmNil)
        return true
    }

    /**
     * Remove the specified keys of the map.
     */
    private fun removeFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil

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
    private fun toListFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToList' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val list = toList(analyzer, thisValue)

        analyzer.memory.addToStackAsLast(list)

        return true
    }

    /**
     * Gets an object with all the map properties whose keys are strings.
     */
    private fun toObjectFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToObject' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val obj = LxmObject(analyzer.memory)
        for ((i, propList) in thisValue.getAllProperties()) {
            for (prop in propList) {
                val key = prop.key
                if (key is LxmString) {
                    obj.setProperty(key.primitive, prop.value)
                }
            }
        }

        analyzer.memory.addToStackAsLast(obj)

        return true
    }

    /**
     * Gets a list with all the keys.
     */
    private fun keysFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Keys' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val list = LxmList(analyzer.memory)
        for ((i, propList) in thisValue.getAllProperties()) {
            for (prop in propList) {
                list.addCell(prop.key)
            }
        }

        analyzer.memory.addToStackAsLast(list)

        return true
    }

    /**
     * Gets a list with all the values.
     */
    private fun valuesFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Keys' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val list = LxmList(analyzer.memory)
        for ((i, propList) in thisValue.getAllProperties()) {
            for (prop in propList) {
                list.addCell(prop.value)
            }
        }

        analyzer.memory.addToStackAsLast(list)

        return true
    }

    /**
     * Removes all the elements of the map.
     */
    private fun clearFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil

        if (thisValue !is LxmMap) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${MapType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Clear' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${MapType.TypeName}") {}
        }

        val keys = thisValue.getAllProperties().values.flatten().map { it.key }
        for (i in keys) {
            thisValue.removeProperty(analyzer.memory, i)
        }

        analyzer.memory.addToStackAsLast(LxmNil)

        return true
    }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs the addition of two values.
     */
    private fun add(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Add,
                    MapType.TypeName, listOf(MapType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmMap, right: LexemMemoryValue ->
                when (right) {
                    is LxmMap -> {
                        val newMap = LxmMap(analyzer.memory)

                        for ((_, list) in left.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(prop.key, prop.value)
                            }
                        }

                        for ((_, list) in right.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(prop.key, prop.value)
                            }
                        }

                        newMap
                    }
                    else -> null
                }
            }

    /**
     * Performs the subtraction of two values.
     */
    private fun sub(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Sub,
                    MapType.TypeName, listOf(MapType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmMap, right: LexemMemoryValue ->
                when (right) {
                    is LxmMap -> {
                        val newMap = LxmMap(analyzer.memory)

                        for ((_, list) in left.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(prop.key, prop.value)
                            }
                        }

                        for ((_, list) in right.getAllProperties()) {
                            for (prop in list) {
                                newMap.removeProperty(analyzer.memory, prop.key)
                            }
                        }

                        newMap
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical AND between two logical values.
     */
    private fun logicalAnd(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalAnd,
                    MapType.TypeName, listOf(MapType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmMap, right: LexemMemoryValue ->
                when (right) {
                    is LxmMap -> {
                        val newMap = LxmMap(analyzer.memory)

                        for ((_, list) in left.getAllProperties()) {
                            for (prop in list) {
                                val value = right.getPropertyValue(analyzer.memory, prop.key)

                                if (value != null) {
                                    newMap.setProperty(prop.key, value)
                                }
                            }
                        }

                        newMap
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical OR between two logical values.
     */
    private fun logicalOr(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalOr,
                    MapType.TypeName, listOf(MapType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmMap, right: LexemMemoryValue ->
                when (right) {
                    is LxmMap -> {
                        val newMap = LxmMap(analyzer.memory)

                        for ((_, list) in left.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(prop.key, prop.value)
                            }
                        }

                        for ((_, list) in right.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(prop.key, prop.value)
                            }
                        }

                        newMap
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical XOR between two logical values.
     */
    private fun logicalXor(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalXor,
                    LogicType.TypeName, listOf(LogicType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmMap, right: LexemMemoryValue ->
                when (right) {
                    is LxmMap -> {
                        val newMap = LxmMap(analyzer.memory)

                        for ((_, list) in left.getAllProperties()) {
                            for (prop in list) {
                                newMap.setProperty(prop.key, prop.value)
                            }
                        }

                        for ((_, list) in right.getAllProperties()) {
                            for (prop in list) {
                                val value = newMap.getPropertyValue(analyzer.memory, prop.key)

                                if (value != null) {
                                    newMap.removeProperty(analyzer.memory, prop.key)
                                } else {
                                    newMap.setProperty(prop.key, prop.value)
                                }
                            }
                        }

                        newMap
                    }
                    else -> null
                }
            }

    // AUXILIARY METHODS ------------------------------------------------------

    private fun toList(analyzer: LexemAnalyzer, map: LxmMap): LxmList {
        val list = LxmList(analyzer.memory)
        for ((i, propList) in map.getAllProperties()) {
            for (prop in propList) {
                val obj = LxmObject(analyzer.memory)

                obj.setProperty(AnalyzerCommons.Identifiers.Key, prop.key)
                obj.setProperty(AnalyzerCommons.Identifiers.Value, prop.value)

                list.addCell(obj)
            }
        }

        return list
    }
}
