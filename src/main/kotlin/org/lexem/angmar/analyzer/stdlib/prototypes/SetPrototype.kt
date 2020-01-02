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
 * Built-in prototype of the Set object.
 */
internal object SetPrototype {
    // Methods
    const val Size = "size"
    const val Freeze = "freeze"
    const val IsFrozen = "isFrozen"
    const val Every = "every"
    const val Filter = "filter"
    const val ForEach = "forEach"
    const val Find = "find"
    const val ContainsAny = "containsAny"
    const val ContainsAll = "containsAll"
    const val Map = "map"
    const val Reduce = "reduce"
    const val Any = "any"
    const val Put = "put"
    const val Remove = "remove"
    const val ToList = "toList"
    const val Clear = "clear"

    // Method arguments

    val EveryArgs = listOf("fn")
    val FilterArgs = EveryArgs
    val ForEachArgs = EveryArgs
    val FindArgs = EveryArgs
    val MapArgs = EveryArgs
    val ReduceArgs = listOf("default", "fn")
    val AnyArgs = EveryArgs

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmObject {
        val prototype = LxmObject(memory)

        // Methods
        prototype.setProperty(memory, Size, LxmFunction(memory, ::sizeFunction), isConstant = true)
        prototype.setProperty(memory, Freeze, LxmFunction(memory, ::freezeFunction), isConstant = true)
        prototype.setProperty(memory, IsFrozen, LxmFunction(memory, ::isFrozenFunction), isConstant = true)
        prototype.setProperty(memory, Every, LxmFunction(memory, ::everyFunction), isConstant = true)
        prototype.setProperty(memory, Filter, LxmFunction(memory, ::filterFunction), isConstant = true)
        prototype.setProperty(memory, ForEach, LxmFunction(memory, ::forEachFunction), isConstant = true)
        prototype.setProperty(memory, Find, LxmFunction(memory, ::findFunction), isConstant = true)
        prototype.setProperty(memory, ContainsAny, LxmFunction(memory, ::containsAnyFunction), isConstant = true)
        prototype.setProperty(memory, ContainsAll, LxmFunction(memory, ::containsAllFunction), isConstant = true)
        prototype.setProperty(memory, Map, LxmFunction(memory, ::mapFunction), isConstant = true)
        prototype.setProperty(memory, Reduce, LxmFunction(memory, ::reduceFunction), isConstant = true)
        prototype.setProperty(memory, Any, LxmFunction(memory, ::anyFunction), isConstant = true)
        prototype.setProperty(memory, Put, LxmFunction(memory, ::putFunction), isConstant = true)
        prototype.setProperty(memory, Remove, LxmFunction(memory, ::removeFunction), isConstant = true)
        prototype.setProperty(memory, ToList, LxmFunction(memory, ::toListFunction), isConstant = true)
        prototype.setProperty(memory, Clear, LxmFunction(memory, ::clearFunction), isConstant = true)

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, LxmFunction(memory, ::add), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Sub, LxmFunction(memory, ::sub), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalAnd, LxmFunction(memory, ::logicalAnd),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalOr, LxmFunction(memory, ::logicalOr),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalXor, LxmFunction(memory, ::logicalXor),
                isConstant = true)

        return prototype
    }

    /**
     * Returns the size of the set.
     */
    private fun sizeFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Size, SetType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmSet ->
                LxmInteger.from(thisValue.size)
            }

    /**
     * Makes the set constant.
     */
    private fun freezeFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Freeze, SetType.TypeName,
                    toWrite = true) { _: LexemAnalyzer, thisValue: LxmSet ->
                thisValue.makeConstant(analyzer.memory)
                LxmNil
            }

    /**
     * Returns whether the set is constant or not.
     */
    private fun isFrozenFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, IsFrozen, SetType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmSet ->
                LxmLogic.from(thisValue.isConstant)
            }

    /**
     * Checks whether all the elements of the set match the condition.
     */
    private fun everyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(analyzer.memory, EveryArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[EveryArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Every' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Every' method requires the parameter called '${EveryArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                if (list.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(0)!!), signalEndFirstElement,
                            Every)

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
                            StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(position)!!),
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
        val parserArguments = arguments.mapArguments(analyzer.memory, FilterArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[FilterArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Filter' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Filter' method requires the parameter called '${FilterArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                // Generate the map.
                val set = LxmSet(analyzer.memory)

                if (list.size > 0) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, set)

                    StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(0)!!), signalEndFirstElement,
                            Filter)

                    return false
                }

                // Remove List from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                analyzer.memory.addToStackAsLast(set)
            }
            else -> {
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(analyzer.memory,
                        toWrite = false) as LxmList
                val set = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory, toWrite = true) as LxmSet

                if (signal in signalEndFirstElement..signalEndFirstElement + list.size) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()
                    val resultTruthy = RelationalFunctions.isTruthy(result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (resultTruthy) {
                        set.addValue(analyzer.memory, list.getCell(position - 1)!!)
                    }

                    if (position < list.size) {
                        StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(position)!!),
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
     * Iterates of the elements of a set.
     */
    private fun forEachFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(analyzer.memory, ForEachArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[ForEachArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ForEach' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ForEach' method requires the parameter called '${ForEachArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                if (list.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(0)!!), signalEndFirstElement,
                            ForEach)

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
                        StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(position)!!),
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
     * Finds an element in the set.
     */
    private fun findFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(analyzer.memory, FindArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[FindArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Find' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Find' method requires the parameter called '${FindArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                if (list.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(0)!!), signalEndFirstElement, Find)

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

                    val result = analyzer.memory.getLastFromStack()
                    val resultTruthy = RelationalFunctions.isTruthy(result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (!resultTruthy) {

                        if (position < list.size) {
                            StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(position)!!),
                                    signalEndFirstElement + position, Find)

                            return false
                        }

                        analyzer.memory.addToStackAsLast(LxmNil)
                    } else {
                        analyzer.memory.addToStackAsLast(list.getCell(position - 1)!!)
                    }

                    // Remove List from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)
                }
            }
        }

        return true
    }

    /**
     * Checks whether any of the specified keys are contained in the set.
     */
    private fun containsAnyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAny' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        for (prop in thisValue.getAllValues()) {
            val inside = spreadPositional.any { RelationalFunctions.identityEquals(it, prop) }
            if (inside) {
                analyzer.memory.addToStackAsLast(LxmLogic.True)
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.False)
        return true
    }

    /**
     * Checks whether all the specified keys are contained in the set.
     */
    private fun containsAllFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAll' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        val allProps = thisValue.getAllValues()
        for (value in spreadPositional) {
            val inside = allProps.any { RelationalFunctions.identityEquals(it, value) }
            if (!inside) {
                analyzer.memory.addToStackAsLast(LxmLogic.False)
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.True)
        return true
    }

    /**
     * Maps the elements of a set into another one.
     */
    private fun mapFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(analyzer.memory, MapArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[MapArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Map' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Map' method requires the parameter called '${MapArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                // Generate the map.
                val set = LxmSet(analyzer.memory)

                if (list.size > 0) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, set)

                    StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(0)!!), signalEndFirstElement, Map)

                    return false
                }

                // Remove List from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.List)

                analyzer.memory.addToStackAsLast(set)
            }
            else -> {
                val list = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(analyzer.memory,
                        toWrite = false) as LxmList
                val set = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory, toWrite = true) as LxmSet

                if (signal in signalEndFirstElement..signalEndFirstElement + list.size) {
                    val position = (signal - signalEndFirstElement) + 1

                    val result = analyzer.memory.getLastFromStack()

                    // Add the value.
                    set.addValue(analyzer.memory, result)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (position < list.size) {
                        StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(position)!!),
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
     * Reduces a set into a value.
     */
    private fun reduceFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(analyzer.memory, ReduceArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val default = parserArguments[ReduceArgs[0]] ?: LxmNil
        val function = parserArguments[ReduceArgs[1]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Reduce' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Reduce' method requires the parameter called '${ReduceArgs[1]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                if (list.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(default, list.getCell(0)!!),
                            signalEndFirstElement, Reduce)

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
                        StdlibCommons.callMethod(analyzer, function, listOf(result, list.getCell(position)!!),
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
     * Checks whether any of the elements of the set match the condition.
     */
    private fun anyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(analyzer.memory, AnyArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[AnyArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Any' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Any' method requires the parameter called '${AnyArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                // Generate the list.
                val list = toList(analyzer, thisValue)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                if (list.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(0)!!), signalEndFirstElement, Any)

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
                            StdlibCommons.callMethod(analyzer, function, listOf(list.getCell(position)!!),
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
     * Adds a new element to the set.
     */
    private fun putFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Put' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        for (i in spreadPositional) {
            thisValue.addValue(analyzer.memory, i)
        }

        analyzer.memory.addToStackAsLast(LxmNil)
        return true
    }

    /**
     * Remove the specified keys of the set.
     */
    private fun removeFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Remove' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        for (key in spreadPositional) {
            thisValue.removeValue(analyzer.memory, key)
        }

        analyzer.memory.addToStackAsLast(LxmNil)
        return true
    }

    /**
     * Gets the set as a list.
     */
    private fun toListFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToList' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        val list = toList(analyzer, thisValue)

        analyzer.memory.addToStackAsLast(list)

        return true
    }

    /**
     * Removes all the elements of the set.
     */
    private fun clearFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList())

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil

        if (thisValue !is LxmSet) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${SetType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Clear' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${SetType.TypeName}") {}
        }

        for (i in thisValue.getAllValues()) {
            thisValue.removeValue(analyzer.memory, i)
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
                    SetType.TypeName, listOf(SetType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmSet, right: LexemMemoryValue ->
                when (right) {
                    is LxmSet -> {
                        val newSet = LxmSet(analyzer.memory)

                        for (prop in left.getAllValues()) {
                            newSet.addValue(analyzer.memory, prop)
                        }

                        for (prop in right.getAllValues()) {
                            newSet.addValue(analyzer.memory, prop)
                        }

                        newSet
                    }
                    else -> null
                }
            }

    /**
     * Performs the subtraction of two values.
     */
    private fun sub(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Sub,
                    SetType.TypeName, listOf(SetType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmSet, right: LexemMemoryValue ->
                when (right) {
                    is LxmSet -> {
                        val newSet = LxmSet(analyzer.memory)

                        for (prop in left.getAllValues()) {
                            newSet.addValue(analyzer.memory, prop)
                        }

                        for (prop in right.getAllValues()) {
                            newSet.removeValue(analyzer.memory, prop)
                        }

                        newSet
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical AND between two logical values.
     */
    private fun logicalAnd(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalAnd,
                    SetType.TypeName, listOf(SetType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmSet, right: LexemMemoryValue ->
                when (right) {
                    is LxmSet -> {
                        val newSet = LxmSet(analyzer.memory)

                        for (prop in left.getAllValues()) {
                            if (right.containsValue(prop)) {
                                newSet.addValue(analyzer.memory, prop)
                            }
                        }

                        newSet
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical OR between two logical values.
     */
    private fun logicalOr(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalOr,
                    SetType.TypeName, listOf(SetType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmSet, right: LexemMemoryValue ->
                when (right) {
                    is LxmSet -> {
                        val newSet = LxmSet(analyzer.memory)

                        for (prop in left.getAllValues()) {
                            newSet.addValue(analyzer.memory, prop)
                        }

                        for (prop in right.getAllValues()) {
                            newSet.addValue(analyzer.memory, prop)
                        }

                        newSet
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
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmSet, right: LexemMemoryValue ->
                when (right) {
                    is LxmSet -> {
                        val newSet = LxmSet(analyzer.memory)

                        for (prop in left.getAllValues()) {
                            newSet.addValue(analyzer.memory, prop)
                        }

                        for (prop in right.getAllValues()) {
                            if (newSet.containsValue(prop)) {
                                newSet.removeValue(analyzer.memory, prop)
                            } else {
                                newSet.addValue(analyzer.memory, prop)
                            }
                        }

                        newSet
                    }
                    else -> null
                }
            }

    // AUXILIARY METHODS ------------------------------------------------------

    private fun toList(analyzer: LexemAnalyzer, set: LxmSet): LxmList {
        val list = LxmList(analyzer.memory)
        for (prop in set.getAllValues()) {
            list.addCell(analyzer.memory, prop)
        }

        return list
    }
}
