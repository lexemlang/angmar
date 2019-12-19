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
import org.lexem.angmar.utils.*

/**
 * Built-in prototype of the String object.
 */
internal object StringPrototype {
    // Methods
    const val Length = "length"
    const val CharsAt = "charsAt"
    const val UnicodePointsAt = "unicodePointsAt"
    const val EndsWithAny = "endsWithAny"
    const val StartsWithAny = "startsWithAny"
    const val ContainsAny = "containsAny"
    const val ContainsAll = "containsAll"
    const val IndexOf = "indexOf"
    const val LastIndexOf = "lastIndexOf"
    const val PadStart = "padStart"
    const val PadEnd = "padEnd"
    const val Repeat = "repeat"
    const val Replace = "replace"
    const val Slice = "slice"
    const val Split = "split"
    const val ToLowercase = "toLowercase"
    const val ToUppercase = "toUppercase"
    const val TrimStart = "trimStart"
    const val TrimEnd = "trimEnd"
    const val Trim = "trim"

    // Method arguments

    val IndexOfArgs = listOf("substring")
    val LastIndexOfArgs = IndexOfArgs
    val PadStartArgs = listOf("length", "padString")
    val PadEndArgs = PadStartArgs
    val RepeatArgs = listOf("count", "separator")
    val ReplaceArgs = listOf("original", "replace", "insensible")
    val SliceArgs = listOf("from", "count")
    val SplitArgs = IndexOfArgs

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmObject {
        val prototype = LxmObject(memory)

        // Methods
        prototype.setProperty(memory, Length, LxmFunction(memory, ::lengthFunction), isConstant = true)
        prototype.setProperty(memory, CharsAt, LxmFunction(memory, ::charsAtFunction), isConstant = true)
        prototype.setProperty(memory, UnicodePointsAt, LxmFunction(memory, ::unicodePointsAtFunction),
                isConstant = true)
        prototype.setProperty(memory, EndsWithAny, LxmFunction(memory, ::endsWithAnyFunction), isConstant = true)
        prototype.setProperty(memory, StartsWithAny, LxmFunction(memory, ::startsWithAnyFunction), isConstant = true)
        prototype.setProperty(memory, ContainsAny, LxmFunction(memory, ::containsAnyFunction), isConstant = true)
        prototype.setProperty(memory, ContainsAll, LxmFunction(memory, ::containsAllFunction), isConstant = true)
        prototype.setProperty(memory, IndexOf, LxmFunction(memory, ::indexOfFunction), isConstant = true)
        prototype.setProperty(memory, LastIndexOf, LxmFunction(memory, ::lastIndexOfFunction), isConstant = true)
        prototype.setProperty(memory, PadStart, LxmFunction(memory, ::padStartFunction), isConstant = true)
        prototype.setProperty(memory, PadEnd, LxmFunction(memory, ::padEndFunction), isConstant = true)
        prototype.setProperty(memory, Repeat, LxmFunction(memory, ::repeatFunction), isConstant = true)
        prototype.setProperty(memory, Replace, LxmFunction(memory, ::replaceFunction), isConstant = true)
        prototype.setProperty(memory, Slice, LxmFunction(memory, ::sliceFunction), isConstant = true)
        prototype.setProperty(memory, Split, LxmFunction(memory, ::splitFunction), isConstant = true)
        prototype.setProperty(memory, ToLowercase, LxmFunction(memory, ::toLowercaseFunction), isConstant = true)
        prototype.setProperty(memory, ToUppercase, LxmFunction(memory, ::toUppercaseFunction), isConstant = true)
        prototype.setProperty(memory, TrimStart, LxmFunction(memory, ::trimStartFunction), isConstant = true)
        prototype.setProperty(memory, TrimEnd, LxmFunction(memory, ::trimEndFunction), isConstant = true)
        prototype.setProperty(memory, Trim, LxmFunction(memory, ::trimFunction), isConstant = true)

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, LxmFunction(memory, ::add), isConstant = true)

        return prototype
    }

    /**
     * Returns the length of the string.
     */
    private fun lengthFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Length, StringType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmString ->
                LxmInteger.from(thisValue.primitive.length)
            }

    /**
     * Return a string with the specified unicode points.
     */
    private fun charsAtFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositionalParameter = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositionalParameter)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CharsAt' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        val result = StringBuilder().apply {
            loop@ for (i in spreadPositionalParameter) {
                when (i) {
                    is LxmInteger -> {
                        if (i.primitive < 0 || i.primitive >= thisValue.primitive.length) {
                            continue@loop
                        }

                        val charInt = Character.codePointAt(thisValue.primitive, i.primitive)
                        val char = Character.toString(charInt)
                        append(char)
                    }
                    is LxmInterval -> {
                        for (j in i.primitive) {
                            if (j < 0 || j >= thisValue.primitive.length) {
                                continue
                            }

                            val charInt = Character.codePointAt(thisValue.primitive, j)
                            val char = Character.toString(charInt)
                            append(char)
                        }
                    }
                    else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$CharsAt' method requires that all its parameters be of type ${IntegerType.TypeName} or ${IntervalType.TypeName}") {}
                }
            }
        }

        analyzer.memory.addToStackAsLast(LxmString.from(result.toString()))

        return true
    }

    /**
     * Return a list with the specified unicode points.
     */
    private fun unicodePointsAtFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositionalParameter = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositionalParameter)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$UnicodePointsAt' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        val list = LxmList(analyzer.memory)

        loop@ for (i in spreadPositionalParameter) {
            when (i) {
                is LxmInteger -> {
                    if (i.primitive < 0 || i.primitive >= thisValue.primitive.length) {
                        continue@loop
                    }

                    val char = Character.codePointAt(thisValue.primitive, i.primitive)
                    list.addCell(analyzer.memory, LxmInteger.from(char))
                }
                is LxmInterval -> {
                    for (j in i.primitive) {
                        if (j < 0 || j >= thisValue.primitive.length) {
                            continue
                        }

                        val char = Character.codePointAt(thisValue.primitive, j)
                        list.addCell(analyzer.memory, LxmInteger.from(char))
                    }
                }
                else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$UnicodePointsAt' method requires that all its parameters be of type ${IntegerType.TypeName} or ${IntervalType.TypeName}") {}
            }
        }

        analyzer.memory.addToStackAsLast(list)

        return true
    }

    /**
     * Return whether the string ends with any of the substrings.
     */
    private fun endsWithAnyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositionalParameter = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositionalParameter)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$EndsWithAny' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        for (i in spreadPositionalParameter) {
            if (i !is LxmString) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$EndsWithAny' method requires that all its parameters be of type ${StringType.TypeName}") {}
            }

            if (thisValue.primitive.endsWith(i.primitive)) {
                analyzer.memory.addToStackAsLast(LxmLogic.True)

                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.False)

        return true
    }

    /**
     * Return whether the string starts with any of the substrings.
     */
    private fun startsWithAnyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositionalParameter = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositionalParameter)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$StartsWithAny' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        for (i in spreadPositionalParameter) {
            if (i !is LxmString) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$StartsWithAny' method requires that all its parameters be of type ${StringType.TypeName}") {}
            }

            if (thisValue.primitive.startsWith(i.primitive)) {
                analyzer.memory.addToStackAsLast(LxmLogic.True)

                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.False)

        return true
    }

    /**
     * Return whether the string contains any of the substrings.
     */
    private fun containsAnyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositionalParameter = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositionalParameter)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAny' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        for (i in spreadPositionalParameter) {
            if (i !is LxmString) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAny' method requires that all its parameters be of type ${StringType.TypeName}") {}
            }

            if (thisValue.primitive.contains(i.primitive)) {
                analyzer.memory.addToStackAsLast(LxmLogic.True)

                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.False)

        return true
    }

    /**
     * Return whether the string contains all the substrings.
     */
    private fun containsAllFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositionalParameter = mutableListOf<LexemPrimitive>()
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList(), spreadPositionalParameter)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAll' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        for (i in spreadPositionalParameter) {
            if (i !is LxmString) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ContainsAll' method requires that all its parameters be of type ${StringType.TypeName}") {}
            }

            if (!thisValue.primitive.contains(i.primitive)) {
                analyzer.memory.addToStackAsLast(LxmLogic.False)

                return true
            }
        }

        analyzer.memory.addToStackAsLast(LxmLogic.True)

        return true
    }

    /**
     * Finds the first position of a substring.
     */
    private fun indexOfFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, IndexOfArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val substring = parserArguments[IndexOfArgs[0]] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$IndexOf' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        if (substring !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$IndexOf' method requires the parameter called '${IndexOfArgs[0]}' be a ${StringType.TypeName}") {}
        }

        val index = thisValue.primitive.indexOf(substring.primitive)
        if (index < 0) {
            analyzer.memory.addToStackAsLast(LxmNil)
        } else {
            analyzer.memory.addToStackAsLast(LxmInteger.from(index))
        }

        return true
    }

    /**
     * Finds the last position of a substring.
     */
    private fun lastIndexOfFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, LastIndexOfArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val substring = parserArguments[LastIndexOfArgs[0]] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$LastIndexOf' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        if (substring !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$LastIndexOf' method requires the parameter called '${LastIndexOfArgs[0]}' be a ${StringType.TypeName}") {}
        }

        val index = thisValue.primitive.lastIndexOf(substring.primitive)
        if (index < 0) {
            analyzer.memory.addToStackAsLast(LxmNil)
        } else {
            analyzer.memory.addToStackAsLast(LxmInteger.from(index))
        }

        return true
    }

    /**
     * Pads the string at the start to reach the specified length.
     */
    private fun padStartFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, PadStartArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val length = parserArguments[PadStartArgs[0]] ?: LxmNil
        val padString = parserArguments[PadStartArgs[1]] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$PadStart' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        if (length !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$PadStart' method requires the parameter called '${PadStartArgs[0]}' be an ${IntegerType.TypeName}") {}
        }

        if (length.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$PadStart' method requires the parameter called '${PadStartArgs[0]}' be a positive ${IntegerType.TypeName}") {}
        }

        when (padString) {
            is LxmNil -> {
                val result = LxmString.from(thisValue.primitive.padStart(length.primitive, ' '))
                analyzer.memory.addToStackAsLast(result)
            }
            is LxmString -> {
                if (padString.primitive.isEmpty()) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$PadStart' method requires the parameter called '${PadStartArgs[1]}' not be an empty ${StringType.TypeName}") {}
                }

                // Perform the result.
                val result = if (thisValue.primitive.length >= length.primitive) {
                    thisValue
                } else {
                    val restLength = length.primitive - thisValue.primitive.length
                    val fullCount = restLength / padString.primitive.length
                    val restCount = restLength % padString.primitive.length
                    val result = padString.primitive.repeat(fullCount) + padString.primitive.substring(0,
                            restCount) + thisValue.primitive
                    LxmString.from(result)
                }

                analyzer.memory.addToStackAsLast(result)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$PadStart' method requires the parameter called '${PadStartArgs[1]}' be a ${StringType.TypeName}") {}
        }

        return true
    }

    /**
     * Pads the string at the end to reach the specified length.
     */
    private fun padEndFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, PadEndArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val length = parserArguments[PadEndArgs[0]] ?: LxmNil
        val padString = parserArguments[PadEndArgs[1]] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$PadEnd' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        if (length !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$PadEnd' method requires the parameter called '${PadEndArgs[0]}' be an ${IntegerType.TypeName}") {}
        }

        if (length.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$PadEnd' method requires the parameter called '${PadEndArgs[0]}' be a positive ${IntegerType.TypeName}") {}
        }

        when (padString) {
            is LxmNil -> {
                val result = LxmString.from(thisValue.primitive.padEnd(length.primitive, ' '))
                analyzer.memory.addToStackAsLast(result)
            }
            is LxmString -> {
                if (padString.primitive.isEmpty()) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$PadEnd' method requires the parameter called '${PadEndArgs[1]}' not be an empty ${StringType.TypeName}") {}
                }

                // Perform the result.
                val result = if (thisValue.primitive.length >= length.primitive) {
                    thisValue
                } else {
                    val restLength = length.primitive - thisValue.primitive.length
                    val fullCount = restLength / padString.primitive.length
                    val restCount = restLength % padString.primitive.length
                    val result =
                            thisValue.primitive + padString.primitive.repeat(fullCount) + padString.primitive.substring(
                                    0, restCount)
                    LxmString.from(result)
                }

                analyzer.memory.addToStackAsLast(result)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$PadEnd' method requires the parameter called '${PadEndArgs[1]}' be a ${StringType.TypeName}") {}
        }

        return true
    }

    /**
     * Repeats a string a number of times.
     */
    private fun repeatFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, RepeatArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val count = parserArguments[RepeatArgs[0]] ?: LxmNil
        val separator = parserArguments[RepeatArgs[1]] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Repeat' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        if (count !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Repeat' method requires the parameter called '${RepeatArgs[0]}' be an ${IntegerType.TypeName}") {}
        }

        if (count.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Repeat' method requires the parameter called '${RepeatArgs[0]}' be a positive ${IntegerType.TypeName}") {}
        }

        when (separator) {
            is LxmNil -> {
                val result = LxmString.from(thisValue.primitive.repeat(count.primitive))
                analyzer.memory.addToStackAsLast(result)
            }
            is LxmString -> {
                val result = LxmString.from(List(count.primitive) { thisValue }.joinToString(separator.primitive))
                analyzer.memory.addToStackAsLast(result)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Repeat' method requires the parameter called '${RepeatArgs[1]}' be a ${StringType.TypeName}") {}
        }

        return true
    }

    /**
     * Replaces a substring by another string.
     */
    private fun replaceFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, ReplaceArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val original = parserArguments[ReplaceArgs[0]] ?: LxmNil
        val replace = parserArguments[ReplaceArgs[1]] ?: LxmNil
        val insensible = parserArguments[ReplaceArgs[2]] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Replace' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        if (original !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Replace' method requires the parameter called '${ReplaceArgs[0]}' be a ${StringType.TypeName}") {}
        }

        if (replace !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Replace' method requires the parameter called '${ReplaceArgs[1]}' be a ${StringType.TypeName}") {}
        }

        when (insensible) {
            is LxmNil -> {
                val result = LxmString.from(thisValue.primitive.replace(original.primitive, replace.primitive))
                analyzer.memory.addToStackAsLast(result)
            }
            is LxmLogic -> {
                val result = LxmString.from(
                        thisValue.primitive.replace(original.primitive, replace.primitive, insensible.primitive))
                analyzer.memory.addToStackAsLast(result)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Replace' method requires the parameter called '${ReplaceArgs[2]}' be a ${LogicType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns a substring between the specified bounds.
     */
    private fun sliceFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, SliceArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val from = parserArguments[SliceArgs[0]] ?: LxmNil
        val count = parserArguments[SliceArgs[1]] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        if (from !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${SliceArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        if (from.primitive < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${SliceArgs[0]}' be a positive ${IntegerType.TypeName}") {}
        }

        when (count) {
            is LxmNil -> {
                val result = LxmString.from(thisValue.primitive.substring(from.primitive))
                analyzer.memory.addToStackAsLast(result)
            }
            is LxmInteger -> {
                if (count.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${SliceArgs[1]}' be a positive ${IntegerType.TypeName}") {}
                }

                val result =
                        LxmString.from(thisValue.primitive.substring(from.primitive, from.primitive + count.primitive))
                analyzer.memory.addToStackAsLast(result)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Slice' method requires the parameter called '${SliceArgs[1]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Splits the string by the specified substring.
     */
    private fun splitFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, SplitArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val substring = parserArguments[SplitArgs[0]] ?: LxmNil

        if (thisValue !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Split' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
        }

        if (substring !is LxmString) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}$Split' method requires the parameter called '${SplitArgs[0]}' be a ${StringType.TypeName}") {}
        }

        val values = thisValue.primitive.split(substring.primitive)
        val list = LxmList(analyzer.memory)

        for (str in values) {
            list.addCell(analyzer.memory, LxmString.from(str))
        }

        analyzer.memory.addToStackAsLast(list)

        return true
    }

    /**
     * Converts the string to lowercase.
     */
    private fun toLowercaseFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, TrimStart, StringType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmString ->
                LxmString.from(thisValue.primitive.toUnicodeLowercase())
            }

    /**
     * Converts the string to uppercase.
     */
    private fun toUppercaseFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, TrimStart, StringType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmString ->
                LxmString.from(thisValue.primitive.toUnicodeUppercase())
            }

    /**
     * Trims the string in the start.
     */
    private fun trimStartFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, TrimStart, StringType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmString ->
                LxmString.from(thisValue.primitive.trimStart())
            }

    /**
     * Trims the string in the end.
     */
    private fun trimEndFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, TrimEnd, StringType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmString ->
                LxmString.from(thisValue.primitive.trimEnd())
            }

    /**
     * Trims the string in both ends.
     */
    private fun trimFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, Trim, StringType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmString ->
                LxmString.from(thisValue.primitive.trim())
            }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs the addition of two values.
     */
    private fun add(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, AnalyzerCommons.Operators.ParameterList)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val left = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
                val right = parserArguments[AnalyzerCommons.Operators.RightParameterName] ?: LxmNil

                if (left !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${StringType.TypeName} value>${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Operators.Add}' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
                }

                // Calls toString.
                StdlibCommons.callToString(analyzer, right, AnalyzerNodesCommons.signalStart + 1,
                        AnalyzerCommons.Operators.Add)

                return false
            }
            else -> {
                val left = parserArguments[AnalyzerCommons.Identifiers.This] as LxmString
                val right = analyzer.memory.getLastFromStack() as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.ToStringMethodNotReturningString,
                        "The ${AnalyzerCommons.Identifiers.ToString} method must always return a ${StringType.TypeName}") {}

                val result = LxmString.from(left.primitive + right.primitive)
                analyzer.memory.replaceLastStackCell(result)

                return true
            }
        }
    }
}
