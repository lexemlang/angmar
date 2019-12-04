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
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()

        // Methods
        prototype.setProperty(memory, IntegerPart, memory.add(LxmFunction(::integerPartFunction)), isConstant = true)
        prototype.setProperty(memory, DecimalPart, memory.add(LxmFunction(::decimalPartFunction)), isConstant = true)
        prototype.setProperty(memory, ToExponential, memory.add(LxmFunction(::toExponentialFunction)),
                isConstant = true)
        prototype.setProperty(memory, ToFixed, memory.add(LxmFunction(::toFixedFunction)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Identifiers.ToString, memory.add(LxmFunction(::toStringFunction)),
                isConstant = true)

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticAffirmation,
                memory.add(LxmFunction(::affirmation)), true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticNegation, memory.add(LxmFunction(::negation)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, memory.add(LxmFunction(::add)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Sub, memory.add(LxmFunction(::sub)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Multiplication,
                memory.add(LxmFunction(::multiplication)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Division, memory.add(LxmFunction(::division)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.IntegerDivision,
                memory.add(LxmFunction(::integerDivision)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Reminder, memory.add(LxmFunction(::reminder)),
                isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Returns the integer part of the number.
     */
    private fun integerPartFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    IntegerPart, FloatType.TypeName) { _: LexemAnalyzer, thisValue: LxmFloat ->
                LxmInteger.from(truncate(thisValue.primitive).toInt())
            }

    /**
     * Returns the decimal part of the number.
     */
    private fun decimalPartFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    DecimalPart, FloatType.TypeName) { _: LexemAnalyzer, thisValue: LxmFloat ->
                LxmFloat.from(thisValue.primitive - truncate(thisValue.primitive))
            }

    /**
     * Returns the textual representation of the 'this' value with the exponential precision.
     */
    private fun toExponentialFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        ToExponentialArgs)

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
    private fun toFixedFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        ToFixedArgs)

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
    private fun toStringFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                        ToStringArgs)

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
    private fun affirmation(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.ArithmeticAffirmation,
                    FloatType.TypeName) { _: LexemAnalyzer, thisValue: LxmFloat ->
                thisValue
            }

    /**
     * Performs a negation.
     */
    private fun negation(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.ArithmeticNegation,
                    FloatType.TypeName) { _: LexemAnalyzer, thisValue: LxmFloat ->
                LxmFloat.from(-thisValue.primitive)
            }

    /**
     * Performs the addition of two values.
     */
    private fun add(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Add, FloatType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName,
                    StringType.TypeName)) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
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
    private fun sub(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Sub, FloatType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
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
    private fun multiplication(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Multiplication, FloatType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
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
    private fun division(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Division, FloatType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
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
    private fun integerDivision(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.IntegerDivision, FloatType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
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
    private fun reminder(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Reminder, FloatType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmFloat, right: LexemMemoryValue ->
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
