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
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.data.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in prototype of the List object.
 */
internal object ListPrototype {
    // Methods
    const val Size = "size"
    const val Freeze = "freeze"
    const val IsFrozen = "isFrozen"
    const val Every = "every"
    const val Filter = "filter"
    const val ForEach = "forEach"
    const val Find = "find"
    const val FindLast = "findLast"
    const val IndexOf = "indexOf"
    const val LastIndexOf = "lastIndexOf"
    const val ContainsAny = "containsAny"
    const val ContainsAll = "containsAll"
    const val Map = "map"
    const val Reduce = "reduce"
    const val Any = "any"
    const val Remove = "remove"
    const val RemoveAt = "removeAt"
    const val JoinToString = "joinToString"
    const val CopyWithin = "copyWithin"
    const val Push = "push"
    const val Pop = "pop"
    const val Unshift = "unshift"
    const val Shift = "shift"
    const val Insert = "insert"
    const val Replace = "replace"
    const val Reverse = "reverse"
    const val Slice = "slice"
    const val Clear = "clear"

    // Method arguments
    val EveryArgs = listOf("fn")
    val FilterArgs = EveryArgs
    val ForEachArgs = EveryArgs
    val FindArgs = EveryArgs
    val FindLastArgs = EveryArgs
    val IndexOfArgs = listOf("value")
    val LastIndexOfArgs = IndexOfArgs
    val MapArgs = EveryArgs
    val ReduceArgs = listOf("default", "fn")
    val AnyArgs = EveryArgs
    val RemoveAtArgs = listOf("at", "count")
    val JoinToStringArgs = listOf("separator")
    val CopyWithinArgs = listOf("target", "at", "from", "count")
    val PopArgs = listOf("count")
    val ShiftArgs = PopArgs
    val InsertArgs = listOf("at")
    val ReplaceArgs = RemoveAtArgs
    val SliceArgs = listOf("from", "count")

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
        prototype.setProperty(Find, LxmFunction(memory, ::findFunction), isConstant = true)
        prototype.setProperty(FindLast, LxmFunction(memory, ::findLastFunction), isConstant = true)
        prototype.setProperty(IndexOf, LxmFunction(memory, ::indexOfFunction), isConstant = true)
        prototype.setProperty(LastIndexOf, LxmFunction(memory, ::lastIndexOfFunction), isConstant = true)
        prototype.setProperty(ContainsAny, LxmFunction(memory, ::containsAnyFunction), isConstant = true)
        prototype.setProperty(ContainsAll, LxmFunction(memory, ::containsAllFunction), isConstant = true)
        prototype.setProperty(Map, LxmFunction(memory, ::mapFunction), isConstant = true)
        prototype.setProperty(Reduce, LxmFunction(memory, ::reduceFunction), isConstant = true)
        prototype.setProperty(Any, LxmFunction(memory, ::anyFunction), isConstant = true)
        prototype.setProperty(Remove, LxmFunction(memory, ::removeFunction), isConstant = true)
        prototype.setProperty(RemoveAt, LxmFunction(memory, ::removeAtFunction), isConstant = true)
        prototype.setProperty(JoinToString, LxmFunction(memory, ::joinToStringFunction), isConstant = true)
        prototype.setProperty(CopyWithin, LxmFunction(memory, ::copyWithinFunction), isConstant = true)
        prototype.setProperty(Push, LxmFunction(memory, ::pushFunction), isConstant = true)
        prototype.setProperty(Pop, LxmFunction(memory, ::popFunction), isConstant = true)
        prototype.setProperty(Unshift, LxmFunction(memory, ::unshiftFunction), isConstant = true)
        prototype.setProperty(Shift, LxmFunction(memory, ::shiftFunction), isConstant = true)
        prototype.setProperty(Insert, LxmFunction(memory, ::insertFunction), isConstant = true)
        prototype.setProperty(Replace, LxmFunction(memory, ::replaceFunction), isConstant = true)
        prototype.setProperty(Reverse, LxmFunction(memory, ::reverseFunction), isConstant = true)
        prototype.setProperty(Slice, LxmFunction(memory, ::sliceFunction), isConstant = true)
        prototype.setProperty(Clear, LxmFunction(memory, ::clearFunction), isConstant = true)

        return prototype
    }

    /**
     * Returns the size of the list.
     */
    private fun sizeFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Size, ListType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmList ->
                LxmInteger.from(thisValue.size)
            }

    /**
     * Makes the list constant.
     */
    private fun freezeFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Freeze, ListType.TypeName,
                    toWrite = true) { _: LexemAnalyzer, thisValue: LxmList ->
                thisValue.makeConstantAndNotWritable(analyzer.memory)
                LxmNil
            }

    /**
     * Returns whether the list is constant or not.
     */
    private fun isFrozenFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, IsFrozen, ListType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmList ->
                LxmLogic.from(thisValue.isConstant)
            }

    /**
     * Checks whether all the elements of the list match the condition.
     */
    private fun everyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(EveryArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[EveryArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Every' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Every' method requires the parameter called '${EveryArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(0)!!), signalEndFirstElement,
                            Every)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmLogic.True)
            }
            in signalEndFirstElement..signalEndFirstElement + thisValue.size -> {
                val position = (signal - signalEndFirstElement) + 1

                val result = analyzer.memory.getLastFromStack()
                val resultTruthy = RelationalFunctions.isTruthy(result)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (resultTruthy) {
                    if (position < thisValue.size) {
                        StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(position)!!),
                                signalEndFirstElement + position, Every)

                        return false
                    }


                    analyzer.memory.addToStackAsLast(LxmLogic.True)
                } else {
                    analyzer.memory.addToStackAsLast(LxmLogic.False)
                }
            }
        }

        return true
    }

    /**
     * Filters a list.
     */
    private fun filterFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(FilterArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[FilterArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Filter' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Filter' method requires the parameter called '${FilterArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val list = LxmList(analyzer.memory)

                if (thisValue.size > 0) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(0)!!), signalEndFirstElement,
                            Filter)

                    return false
                }

                analyzer.memory.addToStackAsLast(list)
            }
            in signalEndFirstElement..signalEndFirstElement + thisValue.size -> {
                val position = (signal - signalEndFirstElement) + 1

                val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(analyzer.memory,
                        toWrite = true) as LxmList
                val result = analyzer.memory.getLastFromStack()
                val resultTruthy = RelationalFunctions.isTruthy(result)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (resultTruthy) {
                    left.addCell(thisValue.getCell(position - 1)!!)
                }

                if (position < thisValue.size) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(position)!!),
                            signalEndFirstElement + position, Filter)

                    return false
                }

                // Move List to Last in the stack.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.List)
            }
        }

        return true
    }

    /**
     * Iterates of the elements of a list.
     */
    private fun forEachFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(ForEachArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[ForEachArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ForEach' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ForEach' method requires the parameter called '${ForEachArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(0)!!), signalEndFirstElement,
                            ForEach)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            in signalEndFirstElement..signalEndFirstElement + thisValue.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (position < thisValue.size) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(position)!!),
                            signalEndFirstElement + position, ForEach)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmNil)
            }
        }

        return true
    }

    /**
     * Returns the first index-value that match the condition.
     */
    private fun findFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(FindArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[FindArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Find' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Find' method requires the parameter called '${FindArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(0)!!), signalEndFirstElement,
                            Find)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            in signalEndFirstElement..signalEndFirstElement + thisValue.size -> {
                val position = (signal - signalEndFirstElement) + 1

                val result = analyzer.memory.getLastFromStack()
                val resultTruthy = RelationalFunctions.isTruthy(result)

                if (resultTruthy) {
                    val obj = LxmObject(analyzer.memory)

                    obj.setProperty(AnalyzerCommons.Identifiers.Index, LxmInteger.from(position - 1))
                    obj.setProperty(AnalyzerCommons.Identifiers.Value, thisValue.getCell(position - 1)!!)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    analyzer.memory.addToStackAsLast(obj)
                } else {
                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (position < thisValue.size) {
                        StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(position)!!),
                                signalEndFirstElement + position, Find)

                        return false
                    }

                    analyzer.memory.addToStackAsLast(LxmNil)
                }
            }
        }

        return true
    }

    /**
     * Returns the last index-value that match the condition.
     */
    private fun findLastFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(FindLastArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[FindLastArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$FindLast' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$FindLast' method requires the parameter called '${FindLastArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(thisValue.size - 1)!!),
                            signalEndFirstElement, FindLast)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            in signalEndFirstElement..signalEndFirstElement + thisValue.size -> {
                val position = (signal - signalEndFirstElement) + 1
                val positionFromLast = thisValue.size - 1 - position

                val result = analyzer.memory.getLastFromStack()
                val resultTruthy = RelationalFunctions.isTruthy(result)

                if (resultTruthy) {
                    val obj = LxmObject(analyzer.memory)

                    obj.setProperty(AnalyzerCommons.Identifiers.Index, LxmInteger.from(positionFromLast + 1))
                    obj.setProperty(AnalyzerCommons.Identifiers.Value, thisValue.getCell(positionFromLast + 1)!!)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    analyzer.memory.addToStackAsLast(obj)
                } else {
                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (positionFromLast >= 0) {
                        StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(positionFromLast)!!),
                                signalEndFirstElement + position, FindLast)

                        return false
                    }

                    analyzer.memory.addToStackAsLast(LxmNil)
                }
            }
        }

        return true
    }

    /**
     * Returns the first index of the specified value in the list.
     */
    private fun indexOfFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(IndexOfArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val value = parserArguments[IndexOfArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$IndexOf' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        for ((index, listValue) in (0 until thisValue.size).map {
            thisValue.getCell(it)!!
        }.withIndex()) {
            if (RelationalFunctions.identityEquals(value, listValue)) {
                analyzer.memory.addToStackAsLast(LxmInteger.from(index))
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmNil)
        return true
    }

    /**
     * Returns the last index of the specified value in the list.
     */
    private fun lastIndexOfFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(LastIndexOfArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val value = parserArguments[LastIndexOfArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$LastIndexOf' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        for ((index, listValue) in (thisValue.size - 1 downTo 0).map {
            thisValue.getCell(it)!!
        }.withIndex()) {
            if (RelationalFunctions.identityEquals(value, listValue)) {
                analyzer.memory.addToStackAsLast(LxmInteger.from(thisValue.size - index - 1))
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmNil)
        return true
    }

    /**
     * Returns whether any of the specified values are contained in the list.
     */
    private fun containsAnyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAny' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        for (value in thisValue.getAllCells()) {
            val inside = spreadPositional.any { RelationalFunctions.identityEquals(it, value) }
            if (inside) {
                analyzer.memory.addToStackAsLast(LxmLogic.True)
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.False)
        return true
    }

    /**
     * Returns whether all the specified values are contained in the list.
     */
    private fun containsAllFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAll' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        val listCells = thisValue.getAllCells()
        for (value in spreadPositional) {
            val inside = listCells.any { RelationalFunctions.identityEquals(it, value) }
            if (!inside) {
                analyzer.memory.addToStackAsLast(LxmLogic.False)
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.True)
        return true
    }

    /**
     * Maps every element of the list into another one.
     */
    private fun mapFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(MapArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[MapArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Map' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Map' method requires the parameter called '${MapArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val list = LxmList(analyzer.memory)

                if (thisValue.size > 0) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, list)

                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(0)!!), signalEndFirstElement,
                            Map)

                    return false
                }

                analyzer.memory.addToStackAsLast(list)
            }
            in signalEndFirstElement..signalEndFirstElement + thisValue.size -> {
                val position = (signal - signalEndFirstElement) + 1

                val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(analyzer.memory,
                        toWrite = true) as LxmList
                val result = analyzer.memory.getLastFromStack()

                left.addCell(result)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (position < thisValue.size) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(position)!!),
                            signalEndFirstElement + position, Map)

                    return false
                }

                // Move List to Last in the stack.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.List)
            }
        }

        return true
    }

    /**
     * Reduces a list of elements to an accumulator.
     */
    private fun reduceFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(ReduceArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val default = parserArguments[ReduceArgs[0]] ?: LxmNil
        val function = parserArguments[ReduceArgs[1]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Reduce' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Reduce' method requires the parameter called '${ReduceArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(default, thisValue.getCell(0)!!),
                            signalEndFirstElement, Reduce)

                    return false
                }

                analyzer.memory.addToStackAsLast(default)
            }
            in signalEndFirstElement..signalEndFirstElement + thisValue.size -> {
                val position = (signal - signalEndFirstElement) + 1

                val result = analyzer.memory.getLastFromStack()

                if (position < thisValue.size) {
                    StdlibCommons.callMethod(analyzer, function, listOf(result, thisValue.getCell(position)!!),
                            signalEndFirstElement + position, Reduce)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    return false
                }
            }
        }

        return true
    }

    /**
     * Checks whether any of the elements of the list match the condition.
     */
    private fun anyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(AnyArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val function = parserArguments[AnyArgs[0]]?.dereference(analyzer.memory, toWrite = false)

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Any' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Any' method requires the parameter called '${AnyArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.size > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(0)!!), signalEndFirstElement,
                            Any)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmLogic.False)
            }
            in signalEndFirstElement..signalEndFirstElement + thisValue.size -> {
                val position = (signal - signalEndFirstElement) + 1

                val result = analyzer.memory.getLastFromStack()
                val resultTruthy = RelationalFunctions.isTruthy(result)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (!resultTruthy) {
                    if (position < thisValue.size) {
                        StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(position)!!),
                                signalEndFirstElement + position, Any)

                        return false
                    }


                    analyzer.memory.addToStackAsLast(LxmLogic.False)
                } else {
                    analyzer.memory.addToStackAsLast(LxmLogic.True)
                }
            }
        }

        return true
    }

    /**
     * Removes some values from the list.
     */
    private fun removeFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Remove' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        var interval = IntegerInterval.Empty
        for ((index, value) in thisValue.getAllCells().withIndex()) {
            val inside = spreadPositional.any { RelationalFunctions.identityEquals(it, value) }
            if (inside) {
                interval = interval.plus(index)
            }
        }

        // Perform the least possible removals.
        for (range in interval.rangeIterator().asSequence().toList().asReversed()) {
            thisValue.removeCell(analyzer.memory, range.from, range.to - range.from + 1)
        }

        analyzer.memory.addToStackAsLast(LxmNil)

        return true
    }

    /**
     * Removes elements of the list at the specified position.
     */
    private fun removeAtFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(RemoveAtArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil
        val at = parserArguments[RemoveAtArgs[0]] ?: LxmNil
        val count = parserArguments[RemoveAtArgs[1]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$RemoveAt' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (at !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$RemoveAt' method requires the parameter called '${RemoveAtArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        if (at.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$RemoveAt' method requires the parameter called '${RemoveAtArgs[0]}' be a positive ${IntegerType.TypeName}") {}
        }

        when (count) {
            is LxmNil -> {
                thisValue.removeCell(analyzer.memory, at.primitive, count = 1)

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            is LxmInteger -> {
                if (count.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$RemoveAt' method requires the parameter called '${RemoveAtArgs[1]}' be a positive ${IntegerType.TypeName}") {}
                }

                if (at.primitive < thisValue.size) {
                    thisValue.removeCell(analyzer.memory, at.primitive, count.primitive)
                }

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$RemoveAt' method requires the parameter called '${RemoveAtArgs[1]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Joins all the elements of the list.
     */
    private fun joinToStringFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments = arguments.mapArguments(JoinToStringArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val separator = parserArguments[JoinToStringArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$JoinToString' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                when (separator) {
                    is LxmNil -> {
                        if (thisValue.size == 0) {
                            analyzer.memory.addToStackAsLast(LxmString.Empty)
                        } else {
                            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, LxmString.Empty)

                            StdlibCommons.callToString(analyzer, thisValue.getCell(0)!!, InternalFunctionCallCompiled,
                                    signalEndFirstElement, JoinToString)

                            return false
                        }
                    }
                    is LxmString -> {
                        if (thisValue.size == 0) {
                            analyzer.memory.addToStackAsLast(LxmString.Empty)
                        } else {
                            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, LxmString.Empty)

                            StdlibCommons.callToString(analyzer, thisValue.getCell(0)!!, InternalFunctionCallCompiled,
                                    signalEndFirstElement, JoinToString)

                            return false
                        }
                    }
                    else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$JoinToString' method requires the parameter called '${JoinToStringArgs[0]}' be a ${StringType.TypeName}") {}
                }
            }
            in signalEndFirstElement..signalEndFirstElement + thisValue.size -> {
                val position = (signal - signalEndFirstElement) + 1

                val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left) as LxmString
                val result = analyzer.memory.getLastFromStack() as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.ToStringMethodNotReturningString,
                        "The ${AnalyzerCommons.Identifiers.ToString} method must always return a ${StringType.TypeName}") {}

                if (separator is LxmString && position != 1) {
                    analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Left,
                            LxmString.from(left.primitive + separator.primitive + result.primitive))
                } else {
                    analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Left,
                            LxmString.from(left.primitive + result.primitive))
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (position < thisValue.size) {
                    StdlibCommons.callToString(analyzer, thisValue.getCell(position)!!, InternalFunctionCallCompiled,
                            signalEndFirstElement + position, JoinToString)

                    return false
                }

                // Move Left to Last in the stack.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Left)
            }
        }

        return true
    }

    /**
     * Copies the content of this list inside another one.
     */
    private fun copyWithinFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(CopyWithinArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val target = parserArguments[CopyWithinArgs[0]]?.dereference(analyzer.memory, toWrite = true) ?: LxmNil
        val at = parserArguments[CopyWithinArgs[1]] ?: LxmNil
        val from = parserArguments[CopyWithinArgs[2]] ?: LxmNil
        val count = parserArguments[CopyWithinArgs[3]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CopyWithin' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (target !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CopyWithin' method requires the parameter called '${CopyWithinArgs[0]}' be a ${ListType.TypeName}") {}
        }

        if (at !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CopyWithin' method requires the parameter called '${CopyWithinArgs[1]}' be a ${IntegerType.TypeName}") {}
        }

        if (at.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CopyWithin' method requires the parameter called '${CopyWithinArgs[1]}' be a positive ${IntegerType.TypeName}") {}
        }

        if (from !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CopyWithin' method requires the parameter called '${CopyWithinArgs[2]}' be a ${IntegerType.TypeName}") {}
        }

        if (from.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CopyWithin' method requires the parameter called '${CopyWithinArgs[2]}' be a positive ${IntegerType.TypeName}") {}
        }

        when (count) {
            is LxmNil -> {
                val indexCurrent = minOf(from.primitive, thisValue.size)
                val cells2Copy = (indexCurrent until thisValue.size).map { thisValue.getCell(it)!! }

                if (at.primitive < target.size) {
                    target.removeCell(analyzer.memory, at.primitive, cells2Copy.size)
                }

                target.insertCell(analyzer.memory, minOf(at.primitive, target.size), *cells2Copy.toTypedArray())

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            is LxmInteger -> {
                if (count.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CopyWithin' method requires the parameter called '${CopyWithinArgs[3]}' be a positive ${IntegerType.TypeName}") {}
                }

                val indexCurrent = minOf(from.primitive, thisValue.size)
                val cells2Copy =
                        (indexCurrent until thisValue.size).take(count.primitive).map { thisValue.getCell(it)!! }

                if (at.primitive < target.size) {
                    target.removeCell(analyzer.memory, at.primitive, cells2Copy.size)
                }

                target.insertCell(analyzer.memory, minOf(at.primitive, target.size), *cells2Copy.toTypedArray())

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CopyWithin' method requires the parameter called '${CopyWithinArgs[3]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Pushes new values to the end of the list.
     */
    private fun pushFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Push' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        thisValue.addCell(*spreadPositional.toTypedArray())

        analyzer.memory.addToStackAsLast(LxmNil)

        return true
    }

    /**
     * Removes values from the end of the list.
     */
    private fun popFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(PopArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil
        val count = parserArguments[PopArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Pop' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        when (count) {
            is LxmNil -> {
                if (thisValue.size == 0) {
                    analyzer.memory.addToStackAsLast(LxmNil)
                }

                val value = thisValue.getCell(thisValue.size - 1)!!

                analyzer.memory.addToStackAsLast(value)
                thisValue.removeCell(analyzer.memory, thisValue.size - 1, count = 1)
            }
            is LxmInteger -> {
                if (count.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Pop' method requires the parameter called '${PopArgs[0]}' be a positive ${IntegerType.TypeName}") {}
                }

                val result = LxmList(analyzer.memory)
                val count2Pop = minOf(thisValue.size, count.primitive)
                val cellsToKeep = (thisValue.size - 1 downTo 0).take(count.primitive).map {
                    thisValue.getCell(it)!!
                }.toTypedArray()

                result.addCell(*cellsToKeep)
                thisValue.removeCell(analyzer.memory, thisValue.size - count2Pop, count2Pop)

                analyzer.memory.addToStackAsLast(result)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Pop' method requires the parameter called '${PopArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Pushes new values at the beginning of the list.
     */
    private fun unshiftFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Unshift' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        thisValue.insertCell(analyzer.memory, 0, *spreadPositional.toTypedArray())

        analyzer.memory.addToStackAsLast(LxmNil)

        return true
    }

    /**
     * Removes values from the start of the list.
     */
    private fun shiftFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(ShiftArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil
        val count = parserArguments[ShiftArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Shift' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        when (count) {
            is LxmNil -> {
                if (thisValue.size == 0) {
                    analyzer.memory.addToStackAsLast(LxmNil)
                }

                val value = thisValue.getCell(0)!!

                analyzer.memory.addToStackAsLast(value)
                thisValue.removeCell(analyzer.memory, 0, count = 1)
            }
            is LxmInteger -> {
                if (count.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Shift' method requires the parameter called '${ShiftArgs[0]}' be a positive ${IntegerType.TypeName}") {}
                }

                val result = LxmList(analyzer.memory)
                val count2Shift = minOf(thisValue.size, count.primitive)
                val cellsToKeep = (0 until thisValue.size).take(count.primitive).map {
                    thisValue.getCell(it)!!
                }.toTypedArray()

                result.addCell(*cellsToKeep)
                thisValue.removeCell(analyzer.memory, 0, count2Shift)

                analyzer.memory.addToStackAsLast(result)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Shift' method requires the parameter called '${ShiftArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Inserts new values at the specified position.
     */
    private fun insertFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(InsertArgs, spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil
        val at = parserArguments[InsertArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Insert' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (at !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Insert' method requires the parameter called '${InsertArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        if (at.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Insert' method requires the parameter called '${InsertArgs[0]}' be a positive ${IntegerType.TypeName}") {}
        }

        val index = minOf(at.primitive, thisValue.size)

        thisValue.insertCell(analyzer.memory, index, *spreadPositional.toTypedArray())

        analyzer.memory.addToStackAsLast(LxmNil)

        return true
    }

    /**
     * Replaces some values of the list for the specified ones.
     */
    private fun replaceFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(ReplaceArgs, spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = true)
                ?: LxmNil
        val at = parserArguments[ReplaceArgs[0]] ?: LxmNil
        val count = parserArguments[ReplaceArgs[1]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Replace' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (at !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Replace' method requires the parameter called '${ReplaceArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        if (at.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Replace' method requires the parameter called '${ReplaceArgs[0]}' be a positive ${IntegerType.TypeName}") {}
        }

        if (count !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Replace' method requires the parameter called '${ReplaceArgs[1]}' be a ${IntegerType.TypeName}") {}
        }

        if (count.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Replace' method requires the parameter called '${ReplaceArgs[1]}' be a positive ${IntegerType.TypeName}") {}
        }

        var index = minOf(at.primitive, thisValue.size)

        if (index < thisValue.size) {
            thisValue.removeCell(analyzer.memory, index, count.primitive)
        }

        index = minOf(at.primitive, thisValue.size)

        thisValue.insertCell(analyzer.memory, index, *spreadPositional.toTypedArray())

        analyzer.memory.addToStackAsLast(LxmNil)

        return true
    }

    /**
     * Reverses the list.
     */
    private fun reverseFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Reverse, ListType.TypeName,
                    toWrite = true) { _: LexemAnalyzer, thisValue: LxmList ->
                if (thisValue.size < 2) {
                    return@executeUnitaryOperator LxmNil
                }

                // Reverse the list.
                for ((leftPos, rightPos) in (0 until thisValue.size / 2).map {
                    Pair(it, thisValue.size - 1 - it)
                }) {
                    val left = thisValue.getCell(leftPos)!!
                    val right = thisValue.getCell(rightPos)!!

                    // Add to the stack to not loose the reference.
                    analyzer.memory.addToStackAsLast(left)

                    thisValue.setCell(analyzer.memory, leftPos, right)
                    thisValue.setCell(analyzer.memory, rightPos, left)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()
                }

                LxmNil
            }

    /**
     * Returns a new list with all the elements between the specified bounds.
     */
    private fun sliceFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(SliceArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite = false)
                ?: LxmNil
        val from = parserArguments[SliceArgs[0]] ?: LxmNil
        val count = parserArguments[SliceArgs[1]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (from !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${SliceArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        if (from.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${SliceArgs[0]}' be a positive ${IntegerType.TypeName}") {}
        }

        when (count) {
            is LxmNil -> {
                val result = LxmList(analyzer.memory)

                result.addCell(*(from.primitive until thisValue.size).map {
                    thisValue.getCell(it)!!
                }.toTypedArray())

                analyzer.memory.addToStackAsLast(result)
            }
            is LxmInteger -> {
                if (count.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${SliceArgs[1]}' be a positive ${IntegerType.TypeName}") {}
                }

                val result = LxmList(analyzer.memory)

                result.addCell(*(from.primitive until thisValue.size).take(count.primitive).map {
                    thisValue.getCell(it)!!
                }.toTypedArray())

                analyzer.memory.addToStackAsLast(result)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${SliceArgs[1]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Removes all the elements of the list.
     */
    private fun clearFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Clear, ListType.TypeName,
                    toWrite = true) { _: LexemAnalyzer, thisValue: LxmList ->
                for (i in thisValue.size - 1 downTo 0) {
                    thisValue.removeCell(analyzer.memory, i)
                }

                LxmNil
            }
}
