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
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()

        // Methods
        prototype.setProperty(memory, Size, memory.add(LxmFunction(::sizeFunction)), isConstant = true)
        prototype.setProperty(memory, Freeze, memory.add(LxmFunction(::freezeFunction)), isConstant = true)
        prototype.setProperty(memory, IsFrozen, memory.add(LxmFunction(::isFrozenFunction)), isConstant = true)
        prototype.setProperty(memory, Every, memory.add(LxmFunction(::everyFunction)), isConstant = true)
        prototype.setProperty(memory, Filter, memory.add(LxmFunction(::filterFunction)), isConstant = true)
        prototype.setProperty(memory, ForEach, memory.add(LxmFunction(::forEachFunction)), isConstant = true)
        prototype.setProperty(memory, Find, memory.add(LxmFunction(::findFunction)), isConstant = true)
        prototype.setProperty(memory, FindLast, memory.add(LxmFunction(::findLastFunction)), isConstant = true)
        prototype.setProperty(memory, IndexOf, memory.add(LxmFunction(::indexOfFunction)), isConstant = true)
        prototype.setProperty(memory, LastIndexOf, memory.add(LxmFunction(::lastIndexOfFunction)), isConstant = true)
        prototype.setProperty(memory, ContainsAny, memory.add(LxmFunction(::containsAnyFunction)), isConstant = true)
        prototype.setProperty(memory, ContainsAll, memory.add(LxmFunction(::containsAllFunction)), isConstant = true)
        prototype.setProperty(memory, Map, memory.add(LxmFunction(::mapFunction)), isConstant = true)
        prototype.setProperty(memory, Reduce, memory.add(LxmFunction(::reduceFunction)), isConstant = true)
        prototype.setProperty(memory, Any, memory.add(LxmFunction(::anyFunction)), isConstant = true)
        prototype.setProperty(memory, Remove, memory.add(LxmFunction(::removeFunction)), isConstant = true)
        prototype.setProperty(memory, RemoveAt, memory.add(LxmFunction(::removeAtFunction)), isConstant = true)
        prototype.setProperty(memory, JoinToString, memory.add(LxmFunction(::joinToStringFunction)), isConstant = true)
        prototype.setProperty(memory, CopyWithin, memory.add(LxmFunction(::copyWithinFunction)), isConstant = true)
        prototype.setProperty(memory, Push, memory.add(LxmFunction(::pushFunction)), isConstant = true)
        prototype.setProperty(memory, Pop, memory.add(LxmFunction(::popFunction)), isConstant = true)
        prototype.setProperty(memory, Unshift, memory.add(LxmFunction(::unshiftFunction)), isConstant = true)
        prototype.setProperty(memory, Shift, memory.add(LxmFunction(::shiftFunction)), isConstant = true)
        prototype.setProperty(memory, Insert, memory.add(LxmFunction(::insertFunction)), isConstant = true)
        prototype.setProperty(memory, Replace, memory.add(LxmFunction(::replaceFunction)), isConstant = true)
        prototype.setProperty(memory, Reverse, memory.add(LxmFunction(::reverseFunction)), isConstant = true)
        prototype.setProperty(memory, Slice, memory.add(LxmFunction(::sliceFunction)), isConstant = true)
        prototype.setProperty(memory, Clear, memory.add(LxmFunction(::clearFunction)), isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Returns the size of the list.
     */
    private fun sizeFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    Size, ListType.TypeName) { _: LexemAnalyzer, thisValue: LxmList ->
                LxmInteger.from(thisValue.actualListSize)
            }

    /**
     * Makes the list constant.
     */
    private fun freezeFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    Freeze, ListType.TypeName) { _: LexemAnalyzer, thisValue: LxmList ->
                thisValue.makeConstant(analyzer.memory)
                LxmNil
            }

    /**
     * Returns whether the list is constant or not.
     */
    private fun isFrozenFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    IsFrozen, ListType.TypeName) { _: LexemAnalyzer, thisValue: LxmList ->
                LxmLogic.from(thisValue.isImmutable)
            }

    /**
     * Checks whether all the elements of the list match the condition.
     */
    private fun everyFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        EveryArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[EveryArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Every' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Every' method requires the parameter called '${EveryArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.listSize > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(analyzer.memory, 0)!!),
                            signalEndFirstElement, Every)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmLogic.True)
            }
            in signalEndFirstElement until signalEndFirstElement + thisValue.actualListSize -> {
                val position = (signal - signalEndFirstElement) + 1

                val result = analyzer.memory.getLastFromStack()
                val resultTruthy = RelationalFunctions.isTruthy(result)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (resultTruthy) {
                    if (position < thisValue.actualListSize) {
                        StdlibCommons.callMethod(analyzer, function,
                                listOf(thisValue.getCell(analyzer.memory, position)!!),
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
    private fun filterFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        FilterArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[FilterArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Filter' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Filter' method requires the parameter called '${FilterArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val list = LxmList()
                val listRef = analyzer.memory.add(list)

                if (thisValue.listSize > 0) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, listRef)

                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(analyzer.memory, 0)!!),
                            signalEndFirstElement, Filter)

                    return false
                }

                analyzer.memory.addToStackAsLast(listRef)
            }
            in signalEndFirstElement until signalEndFirstElement + thisValue.actualListSize -> {
                val position = (signal - signalEndFirstElement) + 1

                val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(
                        analyzer.memory) as LxmList
                val result = analyzer.memory.getLastFromStack()
                val resultTruthy = RelationalFunctions.isTruthy(result)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (resultTruthy) {
                    left.addCell(analyzer.memory, thisValue.getCell(analyzer.memory, position - 1)!!)
                }

                if (position < thisValue.actualListSize) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(analyzer.memory, position)!!),
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
    private fun forEachFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        ForEachArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[ForEachArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ForEach' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ForEach' method requires the parameter called '${ForEachArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.listSize > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(analyzer.memory, 0)!!),
                            signalEndFirstElement, ForEach)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            in signalEndFirstElement until signalEndFirstElement + thisValue.actualListSize -> {
                val position = (signal - signalEndFirstElement) + 1

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (position < thisValue.actualListSize) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(analyzer.memory, position)!!),
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
    private fun findFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        FindArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[FindArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Find' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Find' method requires the parameter called '${FindArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.listSize > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(analyzer.memory, 0)!!),
                            signalEndFirstElement, Find)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            in signalEndFirstElement until signalEndFirstElement + thisValue.actualListSize -> {
                val position = (signal - signalEndFirstElement) + 1

                val result = analyzer.memory.getLastFromStack()
                val resultTruthy = RelationalFunctions.isTruthy(result)

                if (resultTruthy) {
                    val obj = LxmObject()
                    val objRef = analyzer.memory.add(obj)

                    obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Index, LxmInteger.from(position - 1))
                    obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Value,
                            thisValue.getCell(analyzer.memory, position - 1)!!)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    analyzer.memory.addToStackAsLast(objRef)
                } else {
                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (position < thisValue.actualListSize) {
                        StdlibCommons.callMethod(analyzer, function,
                                listOf(thisValue.getCell(analyzer.memory, position)!!),
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
    private fun findLastFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        FindLastArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[FindLastArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$FindLast' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$FindLast' method requires the parameter called '${FindLastArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.listSize > 0) {
                    StdlibCommons.callMethod(analyzer, function,
                            listOf(thisValue.getCell(analyzer.memory, thisValue.actualListSize - 1)!!),
                            signalEndFirstElement, FindLast)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            in signalEndFirstElement until signalEndFirstElement + thisValue.actualListSize -> {
                val position = (signal - signalEndFirstElement) + 1
                val positionFromLast = thisValue.actualListSize - 1 - position

                val result = analyzer.memory.getLastFromStack()
                val resultTruthy = RelationalFunctions.isTruthy(result)

                if (resultTruthy) {
                    val obj = LxmObject()
                    val objRef = analyzer.memory.add(obj)

                    obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Index,
                            LxmInteger.from(positionFromLast + 1))
                    obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Value,
                            thisValue.getCell(analyzer.memory, positionFromLast + 1)!!)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    analyzer.memory.addToStackAsLast(objRef)
                } else {
                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (positionFromLast >= 0) {
                        StdlibCommons.callMethod(analyzer, function,
                                listOf(thisValue.getCell(analyzer.memory, positionFromLast)!!),
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
    private fun indexOfFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        IndexOfArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val value = parserArguments[IndexOfArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$IndexOf' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        for ((index, listValue) in (0 until thisValue.actualListSize).map {
            thisValue.getCell(analyzer.memory, it)!!
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
    private fun lastIndexOfFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        LastIndexOfArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val value = parserArguments[LastIndexOfArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$LastIndexOf' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        for ((index, listValue) in (thisValue.actualListSize - 1 downTo 0).map {
            thisValue.getCell(analyzer.memory, it)!!
        }.withIndex()) {
            if (RelationalFunctions.identityEquals(value, listValue)) {
                analyzer.memory.addToStackAsLast(LxmInteger.from(thisValue.actualListSize - index - 1))
                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmNil)
        return true
    }

    /**
     * Returns whether any of the specified values are contained in the list.
     */
    private fun containsAnyFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

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
    private fun containsAllFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

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
    private fun mapFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory, MapArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[MapArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Map' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Map' method requires the parameter called '${MapArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val list = LxmList()
                val listRef = analyzer.memory.add(list)

                if (thisValue.listSize > 0) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.List, listRef)

                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(analyzer.memory, 0)!!),
                            signalEndFirstElement, Map)

                    return false
                }

                analyzer.memory.addToStackAsLast(listRef)
            }
            in signalEndFirstElement until signalEndFirstElement + thisValue.actualListSize -> {
                val position = (signal - signalEndFirstElement) + 1

                val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.List).dereference(
                        analyzer.memory) as LxmList
                val result = analyzer.memory.getLastFromStack()

                left.addCell(analyzer.memory, result)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (position < thisValue.actualListSize) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(analyzer.memory, position)!!),
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
    private fun reduceFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        ReduceArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val default = parserArguments[ReduceArgs[0]] ?: LxmNil
        val function = parserArguments[ReduceArgs[1]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Reduce' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Reduce' method requires the parameter called '${ReduceArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.listSize > 0) {
                    StdlibCommons.callMethod(analyzer, function,
                            listOf(default, thisValue.getCell(analyzer.memory, 0)!!), signalEndFirstElement, Reduce)

                    return false
                }

                analyzer.memory.addToStackAsLast(default)
            }
            in signalEndFirstElement until signalEndFirstElement + thisValue.actualListSize -> {
                val position = (signal - signalEndFirstElement) + 1

                val result = analyzer.memory.getLastFromStack()

                if (position < thisValue.actualListSize) {

                    StdlibCommons.callMethod(analyzer, function,
                            listOf(result, thisValue.getCell(analyzer.memory, position)!!),
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
    private fun anyFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory, AnyArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val function = parserArguments[AnyArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Any' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        if (function.dereference(analyzer.memory) !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Any' method requires the parameter called '${AnyArgs[0]}' be a ${FunctionType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                if (thisValue.listSize > 0) {
                    StdlibCommons.callMethod(analyzer, function, listOf(thisValue.getCell(analyzer.memory, 0)!!),
                            signalEndFirstElement, Any)

                    return false
                }

                analyzer.memory.addToStackAsLast(LxmLogic.False)
            }
            in signalEndFirstElement until signalEndFirstElement + thisValue.actualListSize -> {
                val position = (signal - signalEndFirstElement) + 1

                val result = analyzer.memory.getLastFromStack()
                val resultTruthy = RelationalFunctions.isTruthy(result)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (!resultTruthy) {
                    if (position < thisValue.actualListSize) {
                        StdlibCommons.callMethod(analyzer, function,
                                listOf(thisValue.getCell(analyzer.memory, position)!!),
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
    private fun removeFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

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
    private fun removeAtFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        RemoveAtArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
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

                if (at.primitive < thisValue.actualListSize) {
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
    private fun joinToStringFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        JoinToStringArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val separator = parserArguments[JoinToStringArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$JoinToString' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                when (separator) {
                    is LxmNil -> {
                        if (thisValue.actualListSize == 0) {
                            analyzer.memory.addToStackAsLast(LxmString.Empty)
                        } else {
                            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, LxmString.Empty)

                            StdlibCommons.callToString(analyzer, thisValue.getCell(analyzer.memory, 0)!!,
                                    signalEndFirstElement, JoinToString)

                            return false
                        }
                    }
                    is LxmString -> {
                        if (thisValue.actualListSize == 0) {
                            analyzer.memory.addToStackAsLast(LxmString.Empty)
                        } else {
                            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, LxmString.Empty)

                            StdlibCommons.callToString(analyzer, thisValue.getCell(analyzer.memory, 0)!!,
                                    signalEndFirstElement, JoinToString)

                            return false
                        }
                    }
                    else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$JoinToString' method requires the parameter called '${JoinToStringArgs[0]}' be a ${StringType.TypeName}") {}
                }
            }
            in signalEndFirstElement until signalEndFirstElement + thisValue.actualListSize -> {
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

                if (position < thisValue.actualListSize) {
                    StdlibCommons.callToString(analyzer, thisValue.getCell(analyzer.memory, position)!!,
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
    private fun copyWithinFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        CopyWithinArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val target = parserArguments[CopyWithinArgs[0]]?.dereference(analyzer.memory) ?: LxmNil
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
                val indexCurrent = minOf(from.primitive, thisValue.actualListSize)
                val cells2Copy =
                        (indexCurrent until thisValue.actualListSize).map { thisValue.getCell(analyzer.memory, it)!! }

                if (at.primitive < target.actualListSize) {
                    target.removeCell(analyzer.memory, at.primitive, cells2Copy.size)
                }

                target.insertCell(analyzer.memory, minOf(at.primitive, target.actualListSize),
                        *cells2Copy.toTypedArray())

                analyzer.memory.addToStackAsLast(LxmNil)
            }
            is LxmInteger -> {
                if (count.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CopyWithin' method requires the parameter called '${CopyWithinArgs[3]}' be a positive ${IntegerType.TypeName}") {}
                }

                val indexCurrent = minOf(from.primitive, thisValue.actualListSize)
                val cells2Copy = (indexCurrent until thisValue.actualListSize).take(count.primitive)
                        .map { thisValue.getCell(analyzer.memory, it)!! }

                if (at.primitive < target.actualListSize) {
                    target.removeCell(analyzer.memory, at.primitive, cells2Copy.size)
                }

                target.insertCell(analyzer.memory, minOf(at.primitive, target.actualListSize),
                        *cells2Copy.toTypedArray())

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
    private fun pushFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Push' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        thisValue.addCell(analyzer.memory, *spreadPositional.toTypedArray())

        analyzer.memory.addToStackAsLast(LxmNil)

        return true
    }

    /**
     * Removes values from the end of the list.
     */
    private fun popFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory, PopArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val count = parserArguments[PopArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Pop' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        when (count) {
            is LxmNil -> {
                if (thisValue.actualListSize == 0) {
                    analyzer.memory.addToStackAsLast(LxmNil)
                }

                val value = thisValue.getCell(analyzer.memory, thisValue.actualListSize - 1)!!

                analyzer.memory.addToStackAsLast(value)
                thisValue.removeCell(analyzer.memory, thisValue.actualListSize - 1, count = 1)
            }
            is LxmInteger -> {
                if (count.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Pop' method requires the parameter called '${PopArgs[0]}' be a positive ${IntegerType.TypeName}") {}
                }

                val result = LxmList()
                val resultRef = analyzer.memory.add(result)
                val count2Pop = minOf(thisValue.actualListSize, count.primitive)
                val cellsToKeep = (thisValue.actualListSize - 1 downTo 0).take(count.primitive).map {
                    thisValue.getCell(analyzer.memory, it)!!
                }.toTypedArray()

                result.addCell(analyzer.memory, *cellsToKeep)
                thisValue.removeCell(analyzer.memory, thisValue.actualListSize - count2Pop, count2Pop)

                analyzer.memory.addToStackAsLast(resultRef)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Pop' method requires the parameter called '${PopArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Pushes new values at the beginning of the list.
     */
    private fun unshiftFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        emptyList(), spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil

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
    private fun shiftFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        ShiftArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
        val count = parserArguments[ShiftArgs[0]] ?: LxmNil

        if (thisValue !is LxmList) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Shift' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${ListType.TypeName}") {}
        }

        when (count) {
            is LxmNil -> {
                if (thisValue.actualListSize == 0) {
                    analyzer.memory.addToStackAsLast(LxmNil)
                }

                val value = thisValue.getCell(analyzer.memory, 0)!!

                analyzer.memory.addToStackAsLast(value)
                thisValue.removeCell(analyzer.memory, 0, count = 1)
            }
            is LxmInteger -> {
                if (count.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Shift' method requires the parameter called '${ShiftArgs[0]}' be a positive ${IntegerType.TypeName}") {}
                }

                val result = LxmList()
                val resultRef = analyzer.memory.add(result)
                val count2Shift = minOf(thisValue.actualListSize, count.primitive)
                val cellsToKeep = (0 until thisValue.actualListSize).take(count.primitive).map {
                    thisValue.getCell(analyzer.memory, it)!!
                }.toTypedArray()

                result.addCell(analyzer.memory, *cellsToKeep)
                thisValue.removeCell(analyzer.memory, 0, count2Shift)

                analyzer.memory.addToStackAsLast(resultRef)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Shift' method requires the parameter called '${ShiftArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Inserts new values at the specified position.
     */
    private fun insertFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        InsertArgs, spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
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

        val index = minOf(at.primitive, thisValue.actualListSize)

        thisValue.insertCell(analyzer.memory, index, *spreadPositional.toTypedArray())

        analyzer.memory.addToStackAsLast(LxmNil)

        return true
    }

    /**
     * Replaces some values of the list for the specified ones.
     */
    private fun replaceFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        ReplaceArgs, spreadPositional)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
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

        var index = minOf(at.primitive, thisValue.actualListSize)

        if (index < thisValue.actualListSize) {
            thisValue.removeCell(analyzer.memory, index, count.primitive)
        }

        index = minOf(at.primitive, thisValue.actualListSize)

        thisValue.insertCell(analyzer.memory, index, *spreadPositional.toTypedArray())

        analyzer.memory.addToStackAsLast(LxmNil)

        return true
    }

    /**
     * Reverses the list.
     */
    private fun reverseFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    Reverse, ListType.TypeName) { _: LexemAnalyzer, thisValue: LxmList ->
                if (thisValue.actualListSize < 2) {
                    return@executeUnitaryOperator LxmNil
                }

                // Reverse the list.
                for ((leftPos, rightPos) in (0 until thisValue.actualListSize / 2).map {
                    Pair(it, thisValue.actualListSize - 1 - it)
                }) {
                    val left = thisValue.getCell(analyzer.memory, leftPos)!!
                    val right = thisValue.getCell(analyzer.memory, rightPos)!!

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
    private fun sliceFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        SliceArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory) ?: LxmNil
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
                val result = LxmList()
                val resultRef = analyzer.memory.add(result)

                result.addCell(analyzer.memory, *(from.primitive until thisValue.actualListSize).map {
                    thisValue.getCell(analyzer.memory, it)!!
                }.toTypedArray())

                analyzer.memory.addToStackAsLast(resultRef)
            }
            is LxmInteger -> {
                if (count.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${SliceArgs[1]}' be a positive ${IntegerType.TypeName}") {}
                }

                val result = LxmList()
                val resultRef = analyzer.memory.add(result)

                result.addCell(analyzer.memory,
                        *(from.primitive until thisValue.actualListSize).take(count.primitive).map {
                            thisValue.getCell(analyzer.memory, it)!!
                        }.toTypedArray())

                analyzer.memory.addToStackAsLast(resultRef)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${ListType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${SliceArgs[1]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Removes all the elements of the list.
     */
    private fun clearFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    Clear, ListType.TypeName) { _: LexemAnalyzer, thisValue: LxmList ->
                for (i in thisValue.actualListSize - 1 downTo 0) {
                    thisValue.removeCell(analyzer.memory, i)
                }

                LxmNil
            }
}
