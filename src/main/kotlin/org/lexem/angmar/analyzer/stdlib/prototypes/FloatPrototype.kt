package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import kotlin.math.*

/**
 * Built-in prototype of the Float values.
 */
internal object FloatPrototype {
    // Methods
    const val IntegerPart = "integerPart"
    const val DecimalPart = "decimalPart"
    const val ToExponential = "toExponential"
    const val ToFixed = "toFixed"

    // Method arguments
    private val ToExponentialArgs = listOf("precision")
    private val ToFixedArgs = ToExponentialArgs
    private val ToStringArgs = listOf("radix")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmObject {
        val prototype = LxmObject(memory)

        // Methods
        prototype.setProperty(memory, IntegerPart, LxmFunction(memory, ::integerPartFunction), isConstant = true)
        prototype.setProperty(memory, DecimalPart, LxmFunction(memory, ::decimalPartFunction), isConstant = true)
        prototype.setProperty(memory, ToExponential, LxmFunction(memory, ::toExponentialFunction), isConstant = true)
        prototype.setProperty(memory, ToFixed, LxmFunction(memory, ::toFixedFunction), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Identifiers.ToString, LxmFunction(memory, ::toStringFunction),
                isConstant = true)

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticAffirmation,
                LxmFunction(memory, ::affirmation), true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticNegation, LxmFunction(memory, ::negation),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, LxmFunction(memory, ::add), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Sub, LxmFunction(memory, ::sub), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Multiplication, LxmFunction(memory, ::multiplication),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Division, LxmFunction(memory, ::division),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.IntegerDivision, LxmFunction(memory, ::integerDivision),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Reminder, LxmFunction(memory, ::reminder),
                isConstant = true)

        return prototype
    }

    /**
     * Returns the integer part of the number.
     */
    private fun integerPartFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, IntegerPart, FloatType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmFloat ->
                LxmInteger.from(truncate(thisValue.primitive).toInt())
            }

    /**
     * Returns the decimal part of the number.
     */
    private fun decimalPartFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, DecimalPart, FloatType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmFloat ->
                LxmFloat.from(thisValue.primitive - truncate(thisValue.primitive))
            }

    /**
     * Returns the textual representation of the 'this' value with the exponential precision.
     */
    private fun toExponentialFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, ToExponentialArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val precision = parserArguments[ToExponentialArgs[0]] ?: LxmNil

        if (thisValue !is LxmFloat) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${FloatType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToExponential' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${FloatType.TypeName}") {}
        }

        when (precision) {
            is LxmNil -> {
                analyzer.memory.addToStackAsLast(
                        thisValue.toExponentialLexemString(analyzer.memory, Consts.Float.exponentialDefaultPrecision))
            }
            is LxmInteger -> {
                if (precision.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${FloatType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToExponential' method requires the parameter called '${ToExponentialArgs[0]}' be a positive ${IntegerType.TypeName}") {}
                }

                analyzer.memory.addToStackAsLast(
                        thisValue.toExponentialLexemString(analyzer.memory, precision.primitive))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${FloatType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToExponential' method requires the parameter called '${ToExponentialArgs[0]}' be an ${IntegerType.TypeName} or ${NilType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the textual representation of the 'this' value with the fixed precision.
     */
    private fun toFixedFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, ToFixedArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val precision = parserArguments[ToFixedArgs[0]] ?: LxmNil

        if (thisValue !is LxmFloat) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${FloatType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToFixed' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${FloatType.TypeName}") {}
        }

        when (precision) {
            is LxmInteger -> {
                if (precision.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${FloatType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToFixed' method requires the parameter called '${ToFixedArgs[0]}' be a positive ${IntegerType.TypeName}") {}
                }

                analyzer.memory.addToStackAsLast(thisValue.toFixedLexemString(analyzer.memory, precision.primitive))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${FloatType.TypeName} value>${AccessExplicitMemberNode.accessToken}$ToFixed' method requires the parameter called '${ToFixedArgs[0]}' be an ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the textual representation of the 'this' value in the specified radix.
     */
    private fun toStringFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, ToStringArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val radix = parserArguments[ToStringArgs[0]] ?: LxmNil

        if (thisValue !is LxmFloat) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${FloatType.TypeName} value>${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${FloatType.TypeName}") {}
        }

        when (radix) {
            is LxmNil -> {
                analyzer.memory.addToStackAsLast(thisValue.toLexemString(analyzer.memory))
            }
            is LxmInteger -> {
                if (radix.primitive !in listOf(2, 8, 10, 16)) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${FloatType.TypeName} value>${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}' method requires the parameter called '${ToStringArgs[0]}' be 2, 8, 10 or 16.") {}
                }

                analyzer.memory.addToStackAsLast(thisValue.toLexemString(analyzer.memory, radix.primitive))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${FloatType.TypeName} value>${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}' method requires the parameter called '${ToStringArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs an affirmation.
     */
    private fun affirmation(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments,
                    AnalyzerCommons.Operators.ArithmeticAffirmation, FloatType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmFloat ->
                thisValue
            }

    /**
     * Performs a negation.
     */
    private fun negation(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments,
                    AnalyzerCommons.Operators.ArithmeticNegation, FloatType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmFloat ->
                LxmFloat.from(-thisValue.primitive)
            }

    /**
     * Performs the addition of two values.
     */
    private fun add(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Add,
                    FloatType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName, StringType.TypeName),
                    toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(leftValue + rightValue)
                    }
                    is LxmFloat -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(leftValue + rightValue)
                    }
                    is LxmString -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmString.from("" + leftValue + rightValue)
                    }
                    else -> null
                }
            }

    /**
     * Performs the subtraction of two values.
     */
    private fun sub(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Sub,
                    FloatType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(leftValue - rightValue)
                    }
                    is LxmFloat -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(leftValue - rightValue)
                    }
                    else -> null
                }
            }

    /**
     * Performs the multiplication of two values.
     */
    private fun multiplication(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Multiplication,
                    FloatType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(leftValue * rightValue)
                    }
                    is LxmFloat -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(leftValue * rightValue)
                    }
                    else -> null
                }
            }

    /**
     * Performs the division of two values.
     */
    private fun division(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Division,
                    FloatType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(leftValue / rightValue)
                    }
                    is LxmFloat -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(leftValue / rightValue)
                    }
                    else -> null
                }
            }

    /**
     * Performs the integer division of two values.
     */
    private fun integerDivision(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.IntegerDivision,
                    FloatType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmInteger.from(truncate(leftValue / rightValue).toInt())
                    }
                    is LxmFloat -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmInteger.from(truncate(leftValue / rightValue).toInt())
                    }
                    else -> null
                }
            }

    /**
     * Performs the reminder of two values.
     */
    private fun reminder(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Reminder,
                    FloatType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(leftValue % rightValue)
                    }
                    is LxmFloat -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(leftValue % rightValue)
                    }
                    else -> null
                }
            }
}
