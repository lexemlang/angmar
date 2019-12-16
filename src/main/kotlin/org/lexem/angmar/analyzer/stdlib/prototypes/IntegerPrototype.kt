package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in prototype of the Integer values.
 */
internal object IntegerPrototype {
    // Method arguments
    private val ToStringArgs = listOf("radix")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject(memory)

        // Methods
        prototype.setProperty(memory, AnalyzerCommons.Identifiers.ToString,
                memory.add(LxmFunction(memory, ::toStringFunction)), isConstant = true)

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticAffirmation,
                memory.add(LxmFunction(memory, ::affirmation)), true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticNegation,
                memory.add(LxmFunction(memory, ::negation)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, memory.add(LxmFunction(memory, ::add)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Sub, memory.add(LxmFunction(memory, ::sub)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Multiplication,
                memory.add(LxmFunction(memory, ::multiplication)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Division, memory.add(LxmFunction(memory, ::division)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.IntegerDivision,
                memory.add(LxmFunction(memory, ::integerDivision)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Reminder, memory.add(LxmFunction(memory, ::reminder)),
                isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Returns the textual representation of the 'this' value in the specified radix.
     */
    private fun toStringFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, ToStringArgs)

        val thisValue = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
        val radix = parserArguments[ToStringArgs[0]] ?: LxmNil

        if (thisValue !is LxmInteger) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<${IntegerType.TypeName} value>${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${IntegerType.TypeName}") {}
        }

        when (radix) {
            is LxmNil -> {
                analyzer.memory.addToStackAsLast(thisValue.toLexemString(analyzer.memory))
            }
            is LxmInteger -> {
                if (radix.primitive !in listOf(2, 8, 10, 16)) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '<${IntegerType.TypeName} value>${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}' method requires the parameter called '${ToStringArgs[0]}' be 2, 8, 10 or 16.") {}
                }

                analyzer.memory.addToStackAsLast(thisValue.toLexemString(analyzer.memory, radix.primitive))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '<${IntegerType.TypeName} value>${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}' method requires the parameter called '${ToStringArgs[0]}' be a ${IntegerType.TypeName}") {}
        }

        return true
    }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs an affirmation.
     */
    private fun affirmation(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) = BinaryAnalyzerCommons.executeUnitaryOperator(analyzer,
            argumentsReference.dereferenceAs(analyzer.memory, toWrite = false)!!,
            AnalyzerCommons.Operators.ArithmeticAffirmation, IntegerType.TypeName,
            toWrite = false) { _: LexemAnalyzer, thisValue: LxmInteger ->
        thisValue
    }

    /**
     * Performs a negation.
     */
    private fun negation(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) = BinaryAnalyzerCommons.executeUnitaryOperator(analyzer,
            argumentsReference.dereferenceAs(analyzer.memory, toWrite = false)!!,
            AnalyzerCommons.Operators.ArithmeticNegation, IntegerType.TypeName,
            toWrite = false) { _: LexemAnalyzer, thisValue: LxmInteger ->
        LxmInteger.from(-thisValue.primitive)
    }

    /**
     * Performs the addition of two values.
     */
    private fun add(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer,
                    argumentsReference.dereferenceAs(analyzer.memory, toWrite = false)!!, AnalyzerCommons.Operators.Add,
                    IntegerType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName, StringType.TypeName),
                    toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmInteger.from(leftValue + rightValue)
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
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer,
                    argumentsReference.dereferenceAs(analyzer.memory, toWrite = false)!!, AnalyzerCommons.Operators.Sub,
                    IntegerType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmInteger.from(leftValue - rightValue)
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
            signal: Int) = BinaryAnalyzerCommons.executeBinaryOperator(analyzer,
            argumentsReference.dereferenceAs(analyzer.memory, toWrite = false)!!,
            AnalyzerCommons.Operators.Multiplication, IntegerType.TypeName,
            listOf(IntegerType.TypeName, FloatType.TypeName), toWriteLeft = false,
            toWriteRight = false) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
        when (right) {
            is LxmInteger -> {
                val leftValue = left.primitive
                val rightValue = right.primitive

                LxmInteger.from(leftValue * rightValue)
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
            signal: Int) = BinaryAnalyzerCommons.executeBinaryOperator(analyzer,
            argumentsReference.dereferenceAs(analyzer.memory, toWrite = false)!!, AnalyzerCommons.Operators.Division,
            IntegerType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName), toWriteLeft = false,
            toWriteRight = false) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
        when (right) {
            is LxmInteger -> {
                val leftValue = left.primitive
                val rightValue = right.primitive

                LxmInteger.from(leftValue / rightValue)
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
            signal: Int) = BinaryAnalyzerCommons.executeBinaryOperator(analyzer,
            argumentsReference.dereferenceAs(analyzer.memory, toWrite = false)!!,
            AnalyzerCommons.Operators.IntegerDivision, IntegerType.TypeName,
            listOf(IntegerType.TypeName, FloatType.TypeName), toWriteLeft = false,
            toWriteRight = false) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
        when (right) {
            is LxmInteger -> {
                val leftValue = left.primitive
                val rightValue = right.primitive

                LxmInteger.from(leftValue / rightValue)
            }
            is LxmFloat -> {
                val leftValue = left.primitive
                val rightValue = right.primitive

                LxmInteger.from(kotlin.math.truncate(leftValue / rightValue).toInt())
            }
            else -> null
        }
    }

    /**
     * Performs the reminder of two values.
     */
    private fun reminder(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) = BinaryAnalyzerCommons.executeBinaryOperator(analyzer,
            argumentsReference.dereferenceAs(analyzer.memory, toWrite = false)!!, AnalyzerCommons.Operators.Reminder,
            IntegerType.TypeName, listOf(IntegerType.TypeName, FloatType.TypeName), toWriteLeft = false,
            toWriteRight = false) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
        when (right) {
            is LxmInteger -> {
                val leftValue = left.primitive
                val rightValue = right.primitive

                LxmInteger.from(leftValue % rightValue)
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
