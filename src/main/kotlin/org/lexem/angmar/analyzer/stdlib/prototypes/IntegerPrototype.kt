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
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticAffirmation,
                LxmInternalFunction(::affirmation), true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticNegation, LxmInternalFunction(::negation),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Identifiers.ToString, LxmInternalFunction(::toString),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, LxmInternalFunction(::add), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Sub, LxmInternalFunction(::sub), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Multiplication, LxmInternalFunction(::multiplication),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Division, LxmInternalFunction(::division),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.IntegerDivision, LxmInternalFunction(::integerDivision),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Reminder, LxmInternalFunction(::reminder),
                isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Performs an affirmation.
     */
    private fun affirmation(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments,
                    AnalyzerCommons.Operators.ArithmeticAffirmation,
                    IntegerType.TypeName) { _: LexemAnalyzer, thisValue: LxmInteger ->
                thisValue
            }

    /**
     * Performs an negation.
     */
    private fun negation(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments,
                    AnalyzerCommons.Operators.ArithmeticNegation,
                    IntegerType.TypeName) { _: LexemAnalyzer, thisValue: LxmInteger ->
                LxmInteger.from(-thisValue.primitive)
            }

    /**
     * Performs the addition of two values.
     */
    private fun add(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Add,
                    IntegerType.TypeName, listOf(IntegerType.TypeName,
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
    private fun sub(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Sub,
                    IntegerType.TypeName, listOf(IntegerType.TypeName,
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
    private fun multiplication(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Multiplication,
                    IntegerType.TypeName, listOf(IntegerType.TypeName,
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
    private fun division(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Division,
                    IntegerType.TypeName, listOf(IntegerType.TypeName,
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
    private fun integerDivision(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.IntegerDivision,
                    IntegerType.TypeName, listOf(IntegerType.TypeName,
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
    private fun reminder(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Reminder,
                    IntegerType.TypeName, listOf(IntegerType.TypeName,
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

    /**
     * Returns the textual representation of the 'this' value.
     */
    private fun toString(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, AnalyzerCommons.Identifiers.ToString,
                    IntegerType.TypeName) { _: LexemAnalyzer, thisValue: LxmInteger ->
                LxmString.from(thisValue.primitive.toString())
            }
}
