package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * Built-in prototype of the Integer values.
 */
internal object IntegerPrototype {
    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()

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

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs an affirmation.
     */
    private fun affirmation(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.ArithmeticAffirmation,
                    IntegerType.TypeName) { _: LexemAnalyzer, thisValue: LxmInteger ->
                thisValue
            }

    /**
     * Performs a negation.
     */
    private fun negation(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.ArithmeticNegation,
                    IntegerType.TypeName) { _: LexemAnalyzer, thisValue: LxmInteger ->
                LxmInteger.from(-thisValue.primitive)
            }

    /**
     * Performs the addition of two values.
     */
    private fun add(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Add, IntegerType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
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
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Sub, IntegerType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
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
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Multiplication, IntegerType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
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
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Division, IntegerType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
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
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.IntegerDivision, IntegerType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmInteger.from(leftValue / rightValue)
                    }
                    is LxmFloat -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmFloat.from(kotlin.math.truncate(leftValue / rightValue))
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
                    AnalyzerCommons.Operators.Reminder, IntegerType.TypeName, listOf(IntegerType.TypeName,
                    FloatType.TypeName)) { _: LexemAnalyzer, left: LxmInteger, right: LexemMemoryValue ->
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
